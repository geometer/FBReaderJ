package org.geometerplus.zlibrary.core.util;

import org.xml.sax.helpers.DefaultHandler;
import android.util.Xml;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class XmlUtil {
	public static void parseQuietly(ZLFile file, DefaultHandler handler) {
		try {
			Xml.parse(file.getInputStream(), Xml.Encoding.UTF_8, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
