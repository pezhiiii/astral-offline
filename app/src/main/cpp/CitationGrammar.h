#pragma once
#include <stdexcept>
#include <string>

// Constrains syntax only: the model chooses references; no entailment is implied.
inline std::string astral_citation_grammar(int source_count) {
 if(source_count<1 || source_count>4) throw std::runtime_error("Invalid citation source count");
 std::string refs;
 for(int i=1;i<=source_count;++i) {
  if(i>1) refs+=" | ";
  refs+="\"["+std::to_string(i)+"]\"";
 }
 // Bounded words and references prevent an unfinished, unbounded first line.
 // Bounds apply to grammar characters/words, not tokenizer tokens.
 return "root ::= line (\"\\n\" line (\"\\n\" line)?)? \"\\n\"?\n"
        "line ::= body \" \" cite (\" \" cite)? \".\"\n"
        "body ::= word (\" \" word){0,17}\n"
        "word ::= char{1,24} | [0-9]{1,6} \".\" [0-9]{1,6}\n"
        "char ::= [a-zA-Z0-9,;:'()/+%−–—-] | [\\u00C0-\\uFFFF]\n"
        "cite ::= "+refs+"\n";
}
