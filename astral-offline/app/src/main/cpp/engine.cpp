#include <jni.h>
#include "llama.h"
#include <atomic>
#include <algorithm>
#include <codecvt>
#include <locale>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <vector>

static std::atomic<bool> stopped{false};
static std::mutex engine_mutex;
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
  static std::once_flag initialized;
  std::call_once(initialized,[]{llama_backend_init();});
  auto path=utf8(e,file), system=utf8(e,sys), user=utf8(e,question);
  auto mp=llama_model_default_params();mp.n_gpu_layers=0;
  std::unique_ptr<llama_model,decltype(&llama_model_free)> model(llama_model_load_from_file(path.c_str(),mp),llama_model_free);
  if(!model) throw std::runtime_error("Model cannot be loaded. Check architecture, file integrity and available RAM.");
  if(stopped) throw std::runtime_error("Cancelled");
  llama_chat_message messages[]={{"system",system.c_str()},{"user",user.c_str()}};
  const char* tmpl=llama_model_chat_template(model.get(),nullptr);
  if(!tmpl) throw std::runtime_error("Model has no chat template. Import an instruct GGUF with a supported template.");
  std::vector<char> prompt(system.size()+user.size()+4096);
  int size=llama_chat_apply_template(tmpl,messages,2,true,prompt.data(),prompt.size());
  if(size<0) throw std::runtime_error("Unsupported model chat template");
  if(size>(int)prompt.size()) {
   prompt.resize(size+1);size=llama_chat_apply_template(tmpl,messages,2,true,prompt.data(),prompt.size());
   if(size<0) throw std::runtime_error("Chat template failed");
  }
  auto vocab=llama_model_get_vocab(model.get());
  int nt=llama_tokenize(vocab,prompt.data(),size,nullptr,0,true,true);
  if(nt>=0) throw std::runtime_error("Unexpected tokenizer result");
  std::vector<llama_token> tokens(-nt);
  nt=llama_tokenize(vocab,prompt.data(),size,tokens.data(),tokens.size(),true,true);
  if(nt<=0) throw std::runtime_error("Tokenization failed");
  int budget=std::clamp((int)max_tokens,1,512);
  if(nt+budget>4096) throw std::runtime_error("Evidence exceeds 4096-token context. Use shorter documents or a shorter question.");
  auto cp=llama_context_default_params();cp.n_ctx=4096;cp.n_batch=256;cp.n_ubatch=128;
  cp.n_threads=4;cp.n_threads_batch=4;cp.abort_callback=abort_eval;
  std::unique_ptr<llama_context,decltype(&llama_free)> ctx(llama_init_from_model(model.get(),cp),llama_free);
  if(!ctx) throw std::runtime_error("Insufficient resources for model context");
  for(int i=0;i<nt;i+=256) {
   if(stopped) throw std::runtime_error("Cancelled");
   auto batch=llama_batch_get_one(tokens.data()+i,std::min(256,nt-i));
   if(llama_decode(ctx.get(),batch)!=0) throw std::runtime_error(stopped?"Cancelled":"Prompt evaluation failed");
  }
  std::unique_ptr<llama_sampler,decltype(&llama_sampler_free)> sampler(llama_sampler_init_greedy(),llama_sampler_free);
  std::string answer;
  for(int i=0;i<budget;i++) {
   if(stopped) throw std::runtime_error("Cancelled");
   auto token=llama_sampler_sample(sampler.get(),ctx.get(),-1);
   if(llama_vocab_is_eog(vocab,token)) break;
   llama_sampler_accept(sampler.get(),token);
   std::vector<char> piece(256);
   int len=llama_token_to_piece(vocab,token,piece.data(),piece.size(),0,false);
   if(len<0) {piece.resize(-len);len=llama_token_to_piece(vocab,token,piece.data(),piece.size(),0,false);}
   if(len<0) throw std::runtime_error("Token decoding failed");
   answer.append(piece.data(),len);
   if(i+1<budget && llama_decode(ctx.get(),llama_batch_get_one(&token,1))!=0)
    throw std::runtime_error(stopped?"Cancelled":"Generation failed");
  }
  auto out=e->NewByteArray(answer.size());
  if(out) e->SetByteArrayRegion(out,0,answer.size(),reinterpret_cast<const jbyte*>(answer.data()));
  return out;
 } catch(const std::exception& error) {
  e->ThrowNew(e->FindClass("java/lang/RuntimeException"),error.what());return nullptr;
 }
}
