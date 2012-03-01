LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE                  := DeflatingDecompressor-v2
LOCAL_SRC_FILES               := DeflatingDecompressor/DeflatingDecompressor.cpp
LOCAL_LDLIBS                  := -lz

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := LineBreak-v2
LOCAL_SRC_FILES               := LineBreak/LineBreaker.cpp LineBreak/liblinebreak-2.0/linebreak.c LineBreak/liblinebreak-2.0/linebreakdata.c LineBreak/liblinebreak-2.0/linebreakdef.c

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

EXPAT_DIR                     := expat-2.0.1

LOCAL_MODULE                  := expat
LOCAL_SRC_FILES               := $(EXPAT_DIR)/lib/xmlparse.c $(EXPAT_DIR)/lib/xmlrole.c $(EXPAT_DIR)/lib/xmltok.c
LOCAL_CFLAGS                  := -DHAVE_EXPAT_CONFIG_H
LOCAL_C_INCLUDES              := $(LOCAL_PATH)/$(EXPAT_DIR)
LOCAL_EXPORT_C_INCLUDES       := $(LOCAL_PATH)/$(EXPAT_DIR)/lib

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := NativeFormats-v1
LOCAL_CFLAGS                  := -DBASEDIR=\"nativeShare\"
LOCAL_LDLIBS                  := -lz
LOCAL_STATIC_LIBRARIES        := expat

LOCAL_SRC_FILES               := \
	NativeFormats/JavaNativeFormatPlugin.cpp \
	NativeFormats/JavaPluginCollection.cpp \
	NativeFormats/util/AndroidUtil.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFSManager.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFile.cpp \
	NativeFormats/zlibrary/core/src/language/ZLCharSequence.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageDetector.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageList.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageMatcher.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatistics.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsGenerator.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsItem.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsXMLReader.cpp \
	NativeFormats/zlibrary/core/src/library/ZLibrary.cpp \
	NativeFormats/zlibrary/core/src/logger/ZLLogger.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLXMLReader.cpp \
	NativeFormats/fbreader/src/formats/FormatPlugin.cpp \
	NativeFormats/fbreader/src/formats/PluginCollection.cpp \
	NativeFormats/fbreader/src/library/Book.cpp

LOCAL_C_INCLUDES              := \
	$(LOCAL_PATH)/NativeFormats/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/filesystem \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/image \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/language \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/library \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/logger \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/xml

include $(BUILD_SHARED_LIBRARY)
