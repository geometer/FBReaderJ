-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class * extends java.lang.Exception

-keep class org.geometerplus.zlibrary.text.model.ZLTextPlainModel$EntryIteratorImpl { *; }
-keep class org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor$Processor { *; }

-keep class org.geometerplus.zlibrary.core.library.ZLibrary
-keepclassmembers class org.geometerplus.zlibrary.core.library.ZLibrary {
    public static ** Instance();
    public ** getVersionName();
}
-keep class org.geometerplus.zlibrary.core.filesystem.ZLFile
-keepclassmembers class org.geometerplus.zlibrary.core.filesystem.ZLFile {
    public static ** createFileByPath(**);
    public ** children();
    public boolean exists();
    public boolean isDirectory();
    public ** getInputStream();
    public ** getPath();
    public long size();
}
-keep class org.geometerplus.zlibrary.core.image.ZLImage
-keep class org.geometerplus.zlibrary.core.image.ZLFileImage
-keepclassmembers class org.geometerplus.zlibrary.core.image.ZLFileImage {
		public <init>(...);
}
-keep class org.geometerplus.zlibrary.text.model.ZLTextModel
-keep class org.geometerplus.fbreader.formats.PluginCollection
-keepclassmembers class org.geometerplus.fbreader.formats.PluginCollection {
    public static ** Instance();
}
-keepclassmembers class org.geometerplus.fbreader.formats.FormatPlugin {
    public ** supportedFileType();
}
-keep class org.geometerplus.fbreader.formats.NativeFormatPlugin
-keepclassmembers class org.geometerplus.fbreader.formats.NativeFormatPlugin {
    public static ** create(**);
}
-keep class org.geometerplus.zlibrary.core.encodings.Encoding
-keepclassmembers class org.geometerplus.zlibrary.core.encodings.Encoding {
		public ** createConverter();
}
-keep class org.geometerplus.zlibrary.core.encodings.EncodingConverter
-keepclassmembers class org.geometerplus.zlibrary.core.encodings.EncodingConverter {
    public ** Name;
		public int convert(byte[],int,int,char[]);
		public void reset();
}
-keep class org.geometerplus.zlibrary.core.encodings.JavaEncodingCollection
-keepclassmembers class org.geometerplus.zlibrary.core.encodings.JavaEncodingCollection {
    public static ** Instance();
    public ** getEncoding(java.lang.String);
    public ** getEncoding(int);
		public boolean providesConverterFor(java.lang.String);
}
-keep class org.geometerplus.fbreader.Paths
-keepclassmembers class org.geometerplus.fbreader.Paths {
    public static ** cacheDirectory();
}
-keep class org.geometerplus.fbreader.book.Book
-keepclassmembers class org.geometerplus.fbreader.book.Book {
    public ** File;
    public ** getTitle();
    public ** getLanguage();
    public ** getEncodingNoDetection();
    public void setTitle(**);
    public void setSeriesInfo(**,**);
    public void setLanguage(**);
    public void setEncoding(**);
    public void addAuthor(**,**);
    public void addTag(**);
    public void addUid(**);
}
-keep class org.geometerplus.fbreader.book.Tag
-keepclassmembers class org.geometerplus.fbreader.book.Tag {
    public static ** getTag(**,**);
}
-keepclassmembers class org.geometerplus.fbreader.bookmodel.BookModelImpl {
		public void addImage(**,**);
}
-keep class org.geometerplus.fbreader.bookmodel.NativeBookModel
-keepclassmembers class org.geometerplus.fbreader.bookmodel.NativeBookModel {
		public ** Book;
		public void initInternalHyperlinks(**,**,int);
		public void addTOCItem(**,int);
		public void leaveTOCItem();
		public ** createTextModel(**,**,int,int[],int[],int[],int[],byte[],**,**,int);
		public void setBookTextModel(**);
		public void setFootnoteModel(**);
}
-keepclassmembers class org.geometerplus.fbreader.bookmodel.BookReadingException {
    public static void throwForFile(**,**);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
