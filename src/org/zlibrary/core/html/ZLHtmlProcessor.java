package org.zlibrary.core.html;

import java.io.*;
import org.zlibrary.core.util.*;
import org.zlibrary.core.library.ZLibrary;

public abstract class ZLHtmlProcessor {
	public abstract boolean read(ZLHtmlReader xmlReader, InputStream stream);

	/*public boolean read(ZLHTMLReader xmlReader, String fileName) {
		InputStream stream = ZLibrary.getInstance().getInputStream(fileName);
		return (stream != null) ? read(xmlReader, stream) : false;
	}*/
	
	public boolean read(ZLHtmlReader xmlReader, String filename) {
		try {
			InputStream stream = ZLibrary.getInstance().getInputStream(filename);
			return (stream != null) ? read(xmlReader, stream) : false;
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}
}
