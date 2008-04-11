package org.geometerplus.fbreader.formats.html;

import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.description.BookDescription.WritableBookDescription;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessorFactory;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public class HtmlDescriptionReader extends ZLXMLReaderAdapter {

	private WritableBookDescription myDescription;

	private boolean myReadTitle;

	public HtmlDescriptionReader(BookDescription description) {
		myDescription = new WritableBookDescription(description);
		myDescription.setTitle("");
	}

	public boolean dontCacheAttributeValues() {
		return true;
	}

	public boolean readDescription(String fileName) {
		myReadTitle = false;
		return readDocument(fileName);
	}

	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		switch (HtmlTag.getTagByName(tagName)) {
			case HtmlTag.TITLE:
				myReadTitle = true;
				break;
			default:
				break;
		}
		return false;
	}

	public boolean endElementHandler(String tag) {
		switch (HtmlTag.getTagByName(tag)) {
			case HtmlTag.TITLE:
				myReadTitle = false;
				break;
			default:
				break;
		}
		return false;
	}

	public void characterDataHandler(char[] ch, int start, int length) {
		// TODO + length -- remove
		final String text = new String(ch).substring(start, start + length);
		if (myReadTitle) {
			myDescription.setTitle(myDescription.getTitle() + text);
		}
	}

	public boolean readDocument(String fileName) {
		final ZLXMLProcessor processor = ZLXMLProcessorFactory.getInstance()
				.createXMLProcessor();
		return processor.read(this, fileName);
	}

}
