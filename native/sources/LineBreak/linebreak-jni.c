#include <jni.h>

#include "liblinebreak-1.2/linebreak.h"

void Java_org_vimgadgets_linebreak_LineBreak_init() {
	init_linebreak();
}

void Java_org_vimgadgets_linebreak_LineBreak_setLineBreaksForCharArray(jcharArray data, jstring lang, jbyteArray breaks) {
	// TODO: implement
}

void Java_org_vimgadgets_linebreak_LineBreak_setLineBreaksForString(jstring data, jstring lang, jbyteArray breaks) {
	// TODO: implement
}
