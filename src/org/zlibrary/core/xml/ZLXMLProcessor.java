package org.zlibrary.core.xml;

import java.io.*;

import org.zlibrary.core.library.ZLibrary;

public abstract class ZLXMLProcessor {
	public abstract boolean read(ZLXMLReader xmlReader, InputStream stream);

	public boolean read(ZLXMLReader xmlReader, String fileName) {
		InputStream stream = ZLibrary.getInstance().getResourceInputStream(fileName);
		if (stream == null) {
			try {
				stream = new BufferedInputStream(new FileInputStream(fileName));
			} catch (FileNotFoundException e) {
//				System.out.println("File not found");
			}
		}
		return (stream != null) ? read(xmlReader, stream) : false;
	}
}
