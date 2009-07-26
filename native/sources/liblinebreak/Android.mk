LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE                  := liblinebreak
LOCAL_SRC_FILES               := linebreak-jni.c liblinebreak-1.2/linebreak.c liblinebreak-1.2/linebreakdata.c liblinebreak-1.2/linebreakdef.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := false

include $(BUILD_SHARED_LIBRARY)
