#include <jni.h>
#include "llama.h"
#include <atomic>
#include <chrono>
#include <cstdio>
#include <unistd.h>
#include <algorithm>
#include <codecvt>
#include <locale>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <vector>
#include <android/log.h>

static std::atomic<bool> stopped{false};
static std::mutex engine_mutex;
static std::mutex progress_mutex;
using Clock=std::chrono::steady_clock;
static double seconds(Clock::time_point start) { return std::chrono::duration<double>(Clock::now()-start).count(); }
static std::string phase="Ready", partial, metrics;
static std::unique_ptr<llama_model,decltype(&llama_model_free)> cached_model(nullptr,llama_model_free);
static std::string cached_path;
// Context must be destroyed before its owning model.
static std::unique_ptr<llama_context,decltype(&llama_free)> cached_context(nullptr,llama_free);
static std::vector<llama_token> cached_prompt;
static uint32_t cached_context_size=0;
static unsigned long run_number=0;
extern "C" JNIEXPORT void JNICALL Java_org_bl3_astral_Engine_release(JNIEnv*,jobject) {
 std::lock_guard<std::mutex> lock(engine_mutex);
 cached_context.reset();cached_prompt.clear();cached_context_size=0;
 cached_model.reset();cached_path.clear();
}
extern "C" JNIEXPORT jbyteArray JNICALL Java_org_bl3_astral_Engine_metrics(JNIEnv* e,jobject) {
 std::lock_guard<std::mutex> lock(progress_mutex);
 auto out=e->NewByteArray(metrics.size());
 if(out) e->SetByteArrayRegion(out,0,metrics.size(),reinterpret_cast<const jbyte*>(metrics.data()));
 return out;
}
static void report(const std::string& value) {
 std::lock_guard<std::mutex> lock(progress_mutex); phase=value;
 __android_log_print(ANDROID_LOG_INFO,"AstralEngine","%s",value.c_str());
}
extern "C" JNIEXPORT jbyteArray JNICALL Java_org_bl3_astral_Engine_snapshot(JNIEnv* e,jobject) {
 std::lock_guard<std::mutex> lock(progress_mutex);
 auto value=phase+"\n"+partial;
 auto out=e->NewByteArray(value.size());
 if(out) e->SetByteArrayRegion(out,0,value.size(),reinterpret_cast<const jbyte*>(value.data()));
 return out;
}
static bool abort_eval(void*) { return stopped.load(); }
static std::string utf8(JNIEnv* e, jstring s) {
 const jchar* p=e->GetStringChars(s,nullptr);
 if(!p) throw std::runtime_error("Cannot read Java string");
 std::u16string u(reinterpret_cast<const char16_t*>(p),e->GetStringLength(s));
 e->ReleaseStringChars(s,p);
 return std::wstring_convert<std::codecvt_utf8_utf16<char16_t>,char16_t>{}.to_bytes(u);
}
extern "C" JNIEXPORT void JNICALL Java_org_bl3_astral_Engine_cancel(JNIEnv*,jobject) { stopped=true; }
extern "C" JNIEXPORT jbyteArray JNICALL Java_org_bl3_astral_Engine_answer(JNIEnv* e,jobject,jstring file,jstring sys,jstring question,jint max_tokens) {
 std::lock_guard<std::mutex> lock(engine_mutex); stopped=false;
 try {
  { std::lock_guard<std::mutex> progress_lock(progress_mutex); partial.clear(); metrics.clear(); }
  const auto started=Clock::now();
  const auto run=++run_number;
  static std::once_flag initialized;
  std::call_once(initialized,[]{llama_backend_init();});
  auto path=utf8(e,file), system=utf8(e,sys), user=utf8(e,question);
  const bool reused=cached_model && cached_path==path;
  report(reused?"Reusing loaded model":"Loading model");
  if(!reused) {
   cached_context.reset();cached_prompt.clear();cached_context_size=0;
   cached_model.reset();cached_path.clear();
   auto mp=llama_model_default_params();mp.n_gpu_layers=0;
   cached_model.reset(llama_model_load_from_file(path.c_str(),mp));
   if(!cached_model) throw std::runtime_error("Model cannot be loaded. Check architecture, file integrity and available RAM.");
   cached_path=path;
  }
  auto* model=cached_model.get();
  const double load_time=seconds(started);
  const auto prompt_started=Clock::now();
  if(stopped) throw std::runtime_error("Cancelled");
  report("Preparing prompt");
  llama_chat_message messages[]={{"system",system.c_str()},{"user",user.c_str()}};
  const char* tmpl=llama_model_chat_template(model,nullptr);
  if(!tmpl) throw std::runtime_error("Model has no chat template. Import an instruct GGUF with a supported template.");
  std::vector<char> prompt(system.size()+user.size()+4096);
  int size=llama_chat_apply_template(tmpl,messages,2,true,prompt.data(),prompt.size());
  if(size<0) throw std::runtime_error("Unsupported model chat template");
  if(size>(int)prompt.size()) {
   prompt.resize(size+1);size=llama_chat_apply_template(tmpl,messages,2,true,prompt.data(),prompt.size());
   if(size<0) throw std::runtime_error("Chat template failed");
  }
  auto vocab=llama_model_get_vocab(model);
  int nt=llama_tokenize(vocab,prompt.data(),size,nullptr,0,true,true);
  if(nt>=0) throw std::runtime_error("Unexpected tokenizer result");
  std::vector<llama_token> tokens(-nt);
  nt=llama_tokenize(vocab,prompt.data(),size,tokens.data(),tokens.size(),true,true);
  if(nt<=0) throw std::runtime_error("Tokenization failed");
  int budget=std::clamp((int)max_tokens,1,512);
  if(nt+budget>4096) throw std::runtime_error("Evidence exceeds 4096-token context. Use shorter documents or a shorter question.");
  const double format_time=seconds(prompt_started);
  const auto context_started=Clock::now();
  report("Allocating context");
  auto cp=llama_context_default_params();cp.n_ctx=std::min(4096,((nt+budget+255)/256)*256);cp.n_batch=256;cp.n_ubatch=128;
  cp.n_threads=4;cp.n_threads_batch=4;cp.abort_callback=abort_eval;
  if(!cached_context || cached_context_size!=cp.n_ctx) {
   cached_context.reset();cached_prompt.clear();cached_context_size=0;
   cached_context.reset(llama_init_from_model(model,cp));
   if(!cached_context) throw std::runtime_error("Insufficient resources for model context");
   cached_context_size=cp.n_ctx;
  }
  auto* ctx=cached_context.get();
  // Always evaluate at least the final input token to obtain fresh logits.
  int prefix=0;
  while(prefix<nt-1 && prefix<(int)cached_prompt.size() && tokens[prefix]==cached_prompt[prefix]) ++prefix;
  if(!llama_memory_seq_rm(llama_get_memory(ctx),0,prefix,-1)) {
   llama_memory_clear(llama_get_memory(ctx),true);prefix=0;
  }
  const double context_time=seconds(context_started);
  const auto evaluation_started=Clock::now();
  for(int i=prefix;i<nt;i+=256) {
   if(stopped) throw std::runtime_error("Cancelled");
   report("Reading evidence: "+std::to_string(i)+" / "+std::to_string(nt)+" tokens");
   auto batch=llama_batch_get_one(tokens.data()+i,std::min(256,nt-i));
   if(llama_decode(ctx,batch)!=0) throw std::runtime_error(stopped?"Cancelled":"Prompt evaluation failed");
  }
  cached_prompt=tokens;
  std::unique_ptr<llama_sampler,decltype(&llama_sampler_free)> sampler(llama_sampler_init_greedy(),llama_sampler_free);
  const double evaluation_time=seconds(evaluation_started);
  const auto generation_started=Clock::now();
  double first_token_time=0; int generated=0;
  bool reached_end=false;
  std::string answer;
  report("Generating answer");
  for(int i=0;i<budget;i++) {
   if(stopped) throw std::runtime_error("Cancelled");
   auto token=llama_sampler_sample(sampler.get(),ctx,-1);
   if(llama_vocab_is_eog(vocab,token)) { reached_end=true;break; }
   llama_sampler_accept(sampler.get(),token);
   std::vector<char> piece(256);
   int len=llama_token_to_piece(vocab,token,piece.data(),piece.size(),0,false);
   if(len<0) {piece.resize(-len);len=llama_token_to_piece(vocab,token,piece.data(),piece.size(),0,false);}
   if(len<0) throw std::runtime_error("Token decoding failed");
   if(generated==0) first_token_time=seconds(started);
   ++generated;
   answer.append(piece.data(),len);
   { std::lock_guard<std::mutex> progress_lock(progress_mutex); partial=answer; }
   if(i==0 || (i+1)%8==0) report("Generating: "+std::to_string(i+1)+" tokens");
   if(i+1<budget && llama_decode(ctx,llama_batch_get_one(&token,1))!=0)
    throw std::runtime_error(stopped?"Cancelled":"Generation failed");
  }
  const double generation_time=seconds(generation_started);
  char timing[1024];
  std::snprintf(timing,sizeof(timing),"Process: %d · run %lu\nModel: %s\nModel setup: %.1f s\nPrompt formatting: %.2f s\nContext setup: %.1f s · %u slots\nInput: %d tokens · cached %d · evaluated %d\nInput evaluation: %.1f s · %.2f tok/s\nFirst token: %.1f s\nGeneration: %.1f s · %d tokens · %.2f tok/s\nFinish: %s",
   static_cast<int>(getpid()),run,reused?"reused":"loaded",load_time,format_time,context_time,cp.n_ctx,
   nt,prefix,nt-prefix,evaluation_time,evaluation_time>0?(nt-prefix)/evaluation_time:0,first_token_time,generation_time,generated,
   generation_time>0?generated/generation_time:0,reached_end?"end of answer":"token limit (answer may be incomplete)");
  { std::lock_guard<std::mutex> progress_lock(progress_mutex); metrics=timing; }
  report("Complete");
  auto out=e->NewByteArray(answer.size());
  if(out) e->SetByteArrayRegion(out,0,answer.size(),reinterpret_cast<const jbyte*>(answer.data()));
  return out;
 } catch(const std::exception& error) {
  cached_context.reset();cached_prompt.clear();cached_context_size=0;
  report(std::string("Stopped: ")+error.what());
  e->ThrowNew(e->FindClass("java/lang/RuntimeException"),error.what());return nullptr;
 }
}
