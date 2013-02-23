package org.geometerplus.fbreader.network.rss;

import java.util.Map;

import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public class RSSXMLReader<MetadataType extends RSSChannelMetadata,EntryType extends RSSItem> extends ZLXMLReaderAdapter {
	
	public RSSXMLReader(ATOMFeedHandler<MetadataType,EntryType> handler, boolean readEntryNotFeed) {
		myFeedHandler = handler;
		myState = readEntryNotFeed ? FEED : START;
		System.out.println("RSSXMLReader start");
	}
	
	protected int myState;
	private final ATOMFeedHandler<MetadataType,EntryType> myFeedHandler;
	private Map<String,String> myNamespaceMap;
	private final StringBuilder myBuffer = new StringBuilder();
	private EntryType myItem;
	
	private static final int START = 0;
	protected static final int FEED = 1;
	
	@Override
	public final boolean startElementHandler(String tag, ZLStringMap attributes) {
		final int index = tag.indexOf(':');
		final String tagPrefix;
		if (index != -1) {
			tagPrefix = tag.substring(0, index).intern();
			tag = tag.substring(index + 1).intern();
		} else {
			tagPrefix = "";
			tag = tag.intern();
		}
		return startElementHandler(getNamespace(tagPrefix), tag, attributes, extractBufferContent());
	}

	@Override
	public final boolean endElementHandler(String tag) {
		final int index = tag.indexOf(':');
		final String tagPrefix;
		if (index != -1) {
			tagPrefix = tag.substring(0, index).intern();
			tag = tag.substring(index + 1).intern();
		} else {
			tagPrefix = "";
			tag = tag.intern();
		}
		return endElementHandler(getNamespace(tagPrefix), tag, extractBufferContent());
	}
	
	@Override
	public final void characterDataHandler(char[] data, int start, int length) {
		myBuffer.append(data, start, length);
	}
	
	public boolean startElementHandler(String ns, String tag, ZLStringMap attributes, String bufferContent) {
		System.out.println("RSSXMLReader > tag: "+tag);
		myItem = myFeedHandler.createEntry(attributes);
		return false;
	}
	
	public boolean endElementHandler(String ns, String tag, String bufferContent) {
		System.out.println("RSSXMLReader < tag: "+tag+", bufferContent: "+bufferContent);
		if (myItem != null) {
			myItem.Title = "test";
			myItem.SeriesTitle = "none";
		}
		myFeedHandler.processFeedEntry(myItem);
		return false;
	}
	
	protected final String getNamespace(String prefix) {
		if (myNamespaceMap == null) {
			return null;
		}
		final String ns = myNamespaceMap.get(prefix);
		return ns != null ? ns.intern() : null;
	}
	
	private final String extractBufferContent() {
		final char[] bufferContentArray = myBuffer.toString().toCharArray();
		myBuffer.delete(0, myBuffer.length());
		if (bufferContentArray.length == 0) {
			return null;
		}
		return new String(bufferContentArray);
	}
}
