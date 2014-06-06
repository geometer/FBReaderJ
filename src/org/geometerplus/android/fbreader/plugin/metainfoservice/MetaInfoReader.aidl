package org.geometerplus.android.fbreader.plugin.metainfoservice;

import android.graphics.Bitmap;

// Declare the interface.
interface MetaInfoReader {
	String readMetaInfo(String path);
	Bitmap readBitmap(String path);
}
