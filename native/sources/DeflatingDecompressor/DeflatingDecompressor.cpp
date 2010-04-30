#include <jni.h>

#include <string.h>
#include <zlib.h>

#include <new>

#define								SIZE							10

static jobject				keys[SIZE]				= { 0 };
static z_stream*			values[SIZE]			= { 0 };

extern "C"
jboolean Java_org_amse_ys_zip_DeflatingDecompressor_startInflating(JNIEnv *env, jobject thiz) {
	int i;
	for (i = 0; i < SIZE; ++i) {
		if (keys[i] == 0) {
			keys[i] = thiz;
			values[i] = new z_stream;
			memset(values[i], 0, sizeof(z_stream));
			inflateInit2(values[i], -MAX_WBITS);
			return 1;
		}
	}
	return 0;
}

extern "C"
void Java_org_amse_ys_zip_DeflatingDecompressor_endInflating(JNIEnv *env, jobject thiz) {
	int i;
	for (i = 0; i < SIZE; ++i) {
		if (keys[i] == thiz) {
			keys[i] = 0;
			inflateEnd(values[i]);
			delete values[i];
			values[i] = 0;
			break;
		}
	}
}

// returns (endFlag << 32) + ((used inLength) << 16) + outLength
extern "C"
jlong Java_org_amse_ys_zip_DeflatingDecompressor_inflate(JNIEnv *env, jobject thiz, jbyteArray in, jint inOffset, jint inLength, jbyteArray out) {
	int i;
	z_stream *stream = 0;
	for (i = 0; i < SIZE; ++i) {
		if (keys[i] == thiz) {
			stream = values[i];
			break;
		}
	}
	if (stream == 0) {
		return 0;
	}

	jbyte* inStart = env->GetByteArrayElements(in, 0);
	jbyte* outStart = env->GetByteArrayElements(out, 0);
	stream->next_in = (Bytef*)inStart + inOffset;
	stream->avail_in = inLength;
	stream->next_out = (Bytef*)outStart;
	const int outLength = env->GetArrayLength(out);
	stream->avail_out = outLength;
	const int code = inflate(stream, Z_SYNC_FLUSH);
	env->ReleaseByteArrayElements(in, inStart, 0);
	env->ReleaseByteArrayElements(out, outStart, 0);
	if (code == Z_OK || code == Z_STREAM_END) {
		jlong result = ((inLength - stream->avail_in) << 16) + outLength - stream->avail_out;
		if (code == Z_STREAM_END) {
			result |= ((jlong)1) << 32;
		}
		return result;
	}
	return 0;
}
