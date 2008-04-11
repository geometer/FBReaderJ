package org.geometerplus.zlibrary.core.html;

import java.io.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.library.ZLibrary;

public abstract class ZLHtmlProcessor {
	public abstract boolean read(ZLHtmlReader xmlReader, InputStream stream);

	/*public boolean read(ZLHTMLReader xmlReader, String fileName) {
		InputStream stream = ZLibrary.getInstance().getInputStream(fileName);
		return (stream != null) ? read(xmlReader, stream) : false;
	}*/
	
	public boolean read(ZLHtmlReader htmlReader, String filename) {
		try {
			InputStream stream = ZLibrary.getInstance().getInputStream(filename);
			//InputStream stream = new FileInputStream(filename);
			return (stream != null) ? read(htmlReader, stream) : false;
		} catch (Exception e) {
			//System.out.println(e);
			//e.printStackTrace();
		}
		return false;
	}
}
