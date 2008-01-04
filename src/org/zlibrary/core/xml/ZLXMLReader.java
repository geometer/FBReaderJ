package org.zlibrary.core.xml;

public interface ZLXMLReader {
	public interface StringMap {
		int getSize();
		String getKey(int index);
		String getValue(String key);
	}

	public boolean dontCacheAttributeValues();

	public void startDocumentHandler();
	public void endDocumentHandler();

	public void startElementHandler(String tag, StringMap attributes);
	public void endElementHandler(String tag);
	public void characterDataHandler(char[] ch, int start, int length);
	public void characterDataHandlerFinal(char[] ch, int start, int length);
}
