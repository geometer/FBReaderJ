#include <jni.h>

#include "liblinebreak-2.0/linebreak.h"

extern "C"
void Java_org_vimgadgets_linebreak_LineBreaker_init(JNIEnv *env, jobject thiz) {
	init_linebreak();
}

extern "C"
void Java_org_vimgadgets_linebreak_LineBreaker_setLineBreaksForCharArray(JNIEnv *env, jobject thiz, jcharArray data, jint offset, jint length, jstring lang, jbyteArray breaks) {
	jchar* dataArray = env->GetCharArrayElements(data, 0);
	jbyte* breaksArray = env->GetByteArrayElements(breaks, 0);
	const char *langArray = (lang != 0) ? env->GetStringUTFChars(lang, 0) : 0;

	set_linebreaks_utf16(dataArray + offset, length, langArray, (char*)breaksArray);

	if (lang != 0) {
  	env->ReleaseStringUTFChars(lang, langArray);
	}
	env->ReleaseByteArrayElements(breaks, breaksArray, 0);
	env->ReleaseCharArrayElements(data, dataArray, 0);
}

extern "C"
void Java_org_vimgadgets_linebreak_LineBreaker_setLineBreaksForString(JNIEnv *env, jobject thiz, jstring data, jstring lang, jbyteArray breaks) {
	const jchar* dataArray = env->GetStringChars(data, 0);
	jbyte* breaksArray = env->GetByteArrayElements(breaks, 0);
	const size_t len = env->GetStringLength(data);
	const char *langArray = (lang != 0) ? env->GetStringUTFChars(lang, 0) : 0;

	set_linebreaks_utf16(dataArray, len, langArray, (char*)breaksArray);

	if (lang != 0) {
  	env->ReleaseStringUTFChars(lang, langArray);
	}
	env->ReleaseByteArrayElements(breaks, breaksArray, 0);
	env->ReleaseStringChars(data, dataArray);
}
