package org.zlibrary.core.html;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.zlibrary.core.library.ZLibrary;

public abstract class ZLHtmlProcessor {
	public abstract boolean read(ZLHtmlReader xmlReader, InputStream stream);

	/*public boolean read(ZLHTMLReader xmlReader, String fileName) {
		InputStream stream = ZLibrary.getInstance().getInputStream(fileName);
		return (stream != null) ? read(xmlReader, stream) : false;
	}*/
	
	public boolean read(ZLHtmlReader xmlReader, String filename) {
		try {
			InputStream stream = new FileInputStream(filename);
			return (stream != null) ? read(xmlReader, stream) : false;
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
		return false;
	}
}
