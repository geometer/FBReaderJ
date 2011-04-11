LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE                  := DeflatingDecompressor
LOCAL_SRC_FILES               := DeflatingDecompressor/DeflatingDecompressor.cpp
LOCAL_LDLIBS                  := -lz

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := LineBreak
LOCAL_SRC_FILES               := LineBreak/LineBreaker.cpp LineBreak/liblinebreak-2.0/linebreak.c LineBreak/liblinebreak-2.0/linebreakdata.c LineBreak/liblinebreak-2.0/linebreakdef.c

include $(BUILD_SHARED_LIBRARY)


#include $(CLEAR_VARS)
#
#LOCAL_MODULE                  := expat
#LOCAL_SRC_FILES               := expat-2.0.1/lib/xmlparse.c expat-2.0.1/lib/xmlrole.c expat-2.0.1/lib/xmltok.c
#LOCAL_C_INCLUDES              := $(LOCAL_PATH)/expat-2.0.1
#LOCAL_CFLAGS                  := -DHAVE_EXPAT_CONFIG_H
#LOCAL_EXPORT_C_INCLUDES       := $(LOCAL_PATH)/expat-2.0.1/lib
#
#include $(BUILD_STATIC_LIBRARY)



#include $(CLEAR_VARS)
#
#LOCAL_MODULE                  := zlcore
#LOCAL_STATIC_LIBRARIES        := expat
#
#LOCAL_SRC_FILES               := \
#	zlibrary/core/src/xml/ZLXMLReader.cpp \
#	zlibrary/core/src/xml/expat/ZLXMLReaderInternal.cpp \
#
#LOCAL_C_INCLUDES              := \
#	$(LOCAL_PATH)/zlibrary/core/src/library \
#	$(LOCAL_PATH)/zlibrary/core/src/typeId \
#	$(LOCAL_PATH)/zlibrary/core/src/util \
#	$(LOCAL_PATH)/zlibrary/core/src/constants \
#	$(LOCAL_PATH)/zlibrary/core/src/logger \
#	$(LOCAL_PATH)/zlibrary/core/src/filesystem \
#	$(LOCAL_PATH)/zlibrary/core/src/filesystem/zip \
#	$(LOCAL_PATH)/zlibrary/core/src/filesystem/bzip2 \
#	$(LOCAL_PATH)/zlibrary/core/src/filesystem/tar \
#	$(LOCAL_PATH)/zlibrary/core/src/dialogs \
#	$(LOCAL_PATH)/zlibrary/core/src/optionEntries \
#	$(LOCAL_PATH)/zlibrary/core/src/application \
#	$(LOCAL_PATH)/zlibrary/core/src/view \
#	$(LOCAL_PATH)/zlibrary/core/src/encoding \
#	$(LOCAL_PATH)/zlibrary/core/src/options \
#	$(LOCAL_PATH)/zlibrary/core/src/message \
#	$(LOCAL_PATH)/zlibrary/core/src/resources \
#	$(LOCAL_PATH)/zlibrary/core/src/time \
#	$(LOCAL_PATH)/zlibrary/core/src/xml \
#	$(LOCAL_PATH)/zlibrary/core/src/xml/expat \
#	$(LOCAL_PATH)/zlibrary/core/src/image \
#	$(LOCAL_PATH)/zlibrary/core/src/language \
#	$(LOCAL_PATH)/zlibrary/core/src/unix/time \
#	$(LOCAL_PATH)/zlibrary/core/src/runnable \
#	$(LOCAL_PATH)/zlibrary/core/src/network \
#	$(LOCAL_PATH)/zlibrary/core/src/network/requests \
#	$(LOCAL_PATH)/zlibrary/core/src/blockTreeView \
#	$(LOCAL_PATH)/zlibrary/core/src/unix/curl \
#	$(LOCAL_PATH)/zlibrary/core/src/desktop/application \
#	$(LOCAL_PATH)/zlibrary/core/src/desktop/dialogs \
#	$(LOCAL_PATH)/zlibrary/core/src/unix/xmlconfig \
#	$(LOCAL_PATH)/zlibrary/core/src/unix/filesystem \
#	$(LOCAL_PATH)/zlibrary/core/src/unix/iconv \
#	$(LOCAL_PATH)/zlibrary/core/src/unix/library
#
#include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE                  := NativeFormats
LOCAL_SRC_FILES               := NativeFormats/NativeFormatPlugin.cpp NativeFormats/PluginCollection.cpp
LOCAL_C_INCLUDES              := \
	$(LOCAL_PATH)/util

include $(BUILD_SHARED_LIBRARY)
