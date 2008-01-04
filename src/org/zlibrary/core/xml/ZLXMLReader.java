package org.zlibrary.core.xml;

import java.io.*;

import org.zlibrary.core.library.ZLibrary;

public abstract class ZLXMLReader {
	public interface StringMap {
		int getSize();
		String getKey(int index);
		String getValue(String key);
	}

	public boolean read(String fileName) {
		InputStream stream = ZLibrary.getInstance().getResourceInputStream(fileName);
		if (stream == null) {
			try {
				stream = new BufferedInputStream(new FileInputStream(fileName));
			} catch (FileNotFoundException e) {
//				System.out.println("File not found");
			}
		}
		return (stream != null) ? ZLXMLProcessorFactory.getInstance().createXMLProcessor().read(this, stream) : false;
	}
	
	public boolean dontCacheAttributeValues() {
		return false;
	}

	public void startElementHandler(String tag, StringMap attributes) {
	}
	
	public void endElementHandler(String tag) {
	}
	
	public void characterDataHandler(char[] ch, int start, int length) {
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		characterDataHandler(ch, start, length);	
	}

	//?
	public void startDocumentHandler() {
	}
	
	public void endDocumentHandler() {
	}
}
