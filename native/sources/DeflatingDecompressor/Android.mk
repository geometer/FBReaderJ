LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE                  := DeflatingDecompressor
LOCAL_SRC_FILES               := DeflatingDecompressor.cpp
LOCAL_LDLIBS                  := -L$(SYSROOT)/usr/lib -lz
LOCAL_ALLOW_UNDEFINED_SYMBOLS := false

include $(BUILD_SHARED_LIBRARY)
