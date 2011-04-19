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


include $(CLEAR_VARS)

EXPAT_DIR := expat-2.0.1

LOCAL_MODULE                  := expat
LOCAL_SRC_FILES               := $(EXPAT_DIR)/lib/xmlparse.c $(EXPAT_DIR)/lib/xmlrole.c $(EXPAT_DIR)/lib/xmltok.c
LOCAL_CFLAGS                  := -DHAVE_EXPAT_CONFIG_H
LOCAL_C_INCLUDES              := $(LOCAL_PATH)/$(EXPAT_DIR)
LOCAL_EXPORT_C_INCLUDES       := $(LOCAL_PATH)/$(EXPAT_DIR)/lib

include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

SHAREDIR_MACRO := nativeShare

LOCAL_MODULE := NativeFormats
LOCAL_LDLIBS := -lz
LOCAL_CFLAGS := -Wall -DBASEDIR=\"$(SHAREDIR_MACRO)\"
LOCAL_STATIC_LIBRARIES := expat

LOCAL_SRC_FILES := \
	NativeFormats/extensions.cpp \
	NativeFormats/fbreader/src/formats/FormatPlugin.cpp \
	NativeFormats/JavaNativeFormatPlugin.cpp \
	NativeFormats/JavaPluginCollection.cpp \
	NativeFormats/main.cpp \
	NativeFormats/util/AndroidUtil.cpp \
	NativeFormats/zlibrary/core/src/encoding/DummyEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/EncodingCollectionReader.cpp \
	NativeFormats/zlibrary/core/src/encoding/MyEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingCollection.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingSet.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLGzipInputStream.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZDecompressor.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipEntryCache.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipHeader.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipInputStream.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFile.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFSManager.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLInputStreamDecorator.cpp \
	NativeFormats/zlibrary/core/src/image/ZLImage.cpp \
	NativeFormats/zlibrary/core/src/image/ZLImageManager.cpp \
	NativeFormats/zlibrary/core/src/library/ZLibrary.cpp \
	NativeFormats/zlibrary/core/src/logger/ZLLogger.cpp \
	NativeFormats/zlibrary/core/src/runnable/ZLExecutionData.cpp \
	NativeFormats/zlibrary/core/src/runnable/ZLRunnable.cpp \
	NativeFormats/zlibrary/core/src/typeId/ZLTypeId.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileInputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSDir.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSManager.cpp \
	NativeFormats/zlibrary/core/src/unix/library/ZLUnixLibrary.cpp \
	NativeFormats/zlibrary/core/src/util/ZLFileUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLStringUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLUnicodeUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLUserData.cpp \
	NativeFormats/zlibrary/core/src/xml/expat/ZLXMLReaderInternal.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLXMLReader.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaFSDir.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaInputStream.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/ZLAndroidFSManager.cpp \
	NativeFormats/zlibrary/ui/src/android/library/ZLAndroidLibraryImplementation.cpp \


LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/NativeFormats/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/application \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/blockTreeView \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/constants \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/dialogs \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/encoding \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/filesystem \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/filesystem/zip \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/image \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/language \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/library \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/logger \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/message \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/network \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/optionEntries \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/options \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/resources \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/runnable \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/time \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/typeId \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/view \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/xml \


include $(BUILD_SHARED_LIBRARY)
