#pragma once
#include <string>
#include <cstdint>
#include <cstdio>
inline bool astral_prompt_contains(const std::string& prompt,const std::string& text) {
 return !text.empty() && prompt.find(text)!=std::string::npos;
}
inline std::string astral_diagnostic_text(const char* text) {
 std::string value=text;
 for(char& c:value) if(static_cast<unsigned char>(c)<32 || c==127) c=' ';
 return value.empty()?"unavailable":value;
}
// Stable identifier, not a cryptographic integrity check.
inline std::string astral_template_id(const char* text) {
 uint64_t hash=14695981039346656037ULL;
 for(const unsigned char* p=reinterpret_cast<const unsigned char*>(text);*p;++p) {
  hash^=*p;hash*=1099511628211ULL;
 }
 char out[17];std::snprintf(out,sizeof(out),"%016llx",static_cast<unsigned long long>(hash));
 return out;
}
