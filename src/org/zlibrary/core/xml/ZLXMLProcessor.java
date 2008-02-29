package org.zlibrary.core.xml;

import java.io.IOException;
import java.io.InputStream;

import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.library.ZLibrary;

public abstract class ZLXMLProcessor {
	public abstract boolean read(ZLXMLReader xmlReader, InputStream stream);

	public boolean read(ZLXMLReader xmlReader, String fileName) {
		InputStream stream = null;
		if (fileName.lastIndexOf(ZLibrary.JAR_DATA_PREFIX) != -1) {
			stream = ZLibrary.getInstance().getInputStream(fileName);
		} else {
			try {
				stream = (new ZLFile(fileName)).getInputStream();
			} catch (IOException e) {
			}
		}
		return (stream != null) ? read(xmlReader, stream) : false;
	}
}
