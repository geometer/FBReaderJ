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
	NativeFormats/main.cpp \
	NativeFormats/JavaNativeFormatPlugin.cpp \
	NativeFormats/JavaPluginCollection.cpp \
	NativeFormats/util/AndroidUtil.cpp \
	NativeFormats/zlibrary/core/src/encoding/DummyEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/EncodingCollectionReader.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingCollection.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingSet.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFSManager.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFile.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLInputStreamDecorator.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLGzipInputStream.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZDecompressor.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipEntryCache.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipHeader.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipInputStream.cpp \
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
	NativeFormats/zlibrary/core/src/util/ZLFileUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLStringUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLUnicodeUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLUserData.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLAsynchronousInputStream.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLXMLReader.cpp \
	NativeFormats/zlibrary/core/src/xml/expat/ZLXMLReaderInternal.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSDir.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSManager.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileInputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileOutputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/library/ZLUnixLibrary.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaFSDir.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaInputStream.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/ZLAndroidFSManager.cpp \
	NativeFormats/zlibrary/ui/src/android/library/ZLAndroidLibraryImplementation.cpp \
	NativeFormats/fbreader/src/formats/FormatPlugin.cpp \
	NativeFormats/fbreader/src/formats/PluginCollection.cpp \
	NativeFormats/fbreader/src/library/Author.cpp \
	NativeFormats/fbreader/src/library/Book.cpp \
	NativeFormats/fbreader/src/library/Comparators.cpp \
	NativeFormats/fbreader/src/library/Tag.cpp

LOCAL_C_INCLUDES              := \
	$(LOCAL_PATH)/NativeFormats/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/encoding \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/filesystem \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/image \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/language \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/library \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/logger \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/xml

include $(BUILD_SHARED_LIBRARY)
