#include "PromptDiagnostics.h"
#include <cassert>
int main() {
 assert(astral_prompt_contains("system: rules; user: question","rules"));
 assert(!astral_prompt_contains("user: question","rules"));
 assert(!astral_prompt_contains("anything",""));
 assert(astral_template_id("hello")=="a430d84680aabd0b");
 assert(astral_diagnostic_text("name\narchitecture\tbad")=="name architecture bad");
 assert(astral_diagnostic_text("")=="unavailable");
}
