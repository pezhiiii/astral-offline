#include "CitationGrammar.h"
#include <cassert>
int main() {
 for(int n=1;n<=4;++n) {
  auto g=astral_citation_grammar(n);
  for(int i=1;i<=4;++i)
   assert((g.find("\"["+std::to_string(i)+"]\"")!=std::string::npos)==(i<=n));
  assert(g.find("{0,63}")!=std::string::npos);
  assert(g.find("core{1,32}")!=std::string::npos);
  assert(g.find("cite)*")==std::string::npos);
  assert(g.find("root ::= line")!=std::string::npos);
 }
 assert(astral_citation_grammar(2,true).find("uFFFF")==std::string::npos);
 assert(astral_citation_grammar(2,false).find("uFFFF")!=std::string::npos);
 for(int n : {0,5,-1}) { bool threw=false;try { astral_citation_grammar(n); } catch(...) {threw=true;}assert(threw); }
}
