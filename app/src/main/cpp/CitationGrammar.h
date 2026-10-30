#pragma once
#include <stdexcept>
#include <string>

// Constrains syntax only: the model chooses references; no entailment is implied.
inline std::string astral_citation_grammar(int source_count, bool english_only = false) {
 if(source_count<1 || source_count>4) throw std::runtime_error("Invalid citation source count");
 std::string refs;
 for(int i=1;i<=source_count;++i) {
  if(i>1) refs+=" | ";
  refs+="\"["+std::to_string(i)+"]\"";
 }
 // Bounded syntax only; the bound does not guarantee a complete sentence.
 // Bounds apply to grammar characters/words, not tokenizer tokens.
 const std::string letters=english_only ? "[a-zA-Z0-9]" : "[a-zA-Z0-9] | [\\u00C0-\\uFFFF]";
 return "root ::= line (\"\\n\" line (\"\\n\" line)?)? \"\\n\"?\n"
        "line ::= body \" \" cite (\" \" cite)? \".\"\n"
        "body ::= word (\" \" word){0,63}\n"
        "word ::= core{1,32} [,)%;:]? | [0-9]{1,6} \".\" [0-9]{1,6}\n"
        "core ::= letter | ['(/+-]\n"
        "letter ::= "+letters+"\n"
        "cite ::= "+refs+"\n";
}
