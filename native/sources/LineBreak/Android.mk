LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE                  := LineBreak
LOCAL_SRC_FILES               := LineBreaker.cpp liblinebreak-2.0/linebreak.c liblinebreak-2.0/linebreakdata.c liblinebreak-2.0/linebreakdef.c
LOCAL_ALLOW_UNDEFINED_SYMBOLS := false

include $(BUILD_SHARED_LIBRARY)
