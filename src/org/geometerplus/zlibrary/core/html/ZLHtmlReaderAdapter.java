package org.geometerplus.zlibrary.core.html;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class ZLHtmlReaderAdapter implements ZLHtmlReader {
	public boolean read(String fileName) {
		return ZLHtmlProcessorFactory.getInstance().createHtmlProcessor().read(this, fileName);
	}
	
	public boolean dontCacheAttributeValues() {
		return false;
	}

	public void startElementHandler(String tag, ZLStringMap attributes) {
	}
	
	public void endElementHandler(String tag) {
	}
	
	public void characterDataHandler(char[] ch, int start, int length) {
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		characterDataHandler(ch, start, length);	
	}

	public void startDocumentHandler() {
	}
	
	public void endDocumentHandler() {
	}
}
