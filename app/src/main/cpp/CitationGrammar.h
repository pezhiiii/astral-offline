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
 // One to three lines. Sentence punctuation can only follow citations.
 // Decimal points are allowed only between digits; abbreviations are spelled out.
 return "root ::= line (\"\\n\" line (\"\\n\" line)?)? \"\\n\"?\n"
        "line ::= body \" \" cite (\" \" cite)* \".\"\n"
        "body ::= (letter | decimal | [0-9]) (letter | decimal | [0-9 ,;:'()/+%−–—-])*\n"
        "letter ::= [a-zA-Z] | [\\u0080-\\uFFFF]\n"
        "decimal ::= [0-9]+ \".\" [0-9]+\n"
        "cite ::= "+refs+"\n";
}
