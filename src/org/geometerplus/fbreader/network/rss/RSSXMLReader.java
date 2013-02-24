package org.geometerplus.fbreader.network.rss;

import java.util.Map;

import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.fbreader.network.atom.ATOMId;
import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public class RSSXMLReader<MetadataType extends RSSChannelMetadata,EntryType extends RSSItem> extends ZLXMLReaderAdapter {
	
	public RSSXMLReader(ATOMFeedHandler<MetadataType,EntryType> handler, boolean readEntryNotFeed) {
		myFeedHandler = handler;
		myState = START;
	}
	
	protected int myState;
	private final ATOMFeedHandler<MetadataType,EntryType> myFeedHandler;
	private Map<String,String> myNamespaceMap;
	private final StringBuilder myBuffer = new StringBuilder();
	private EntryType myItem;
	private RSSAuthor myAuthor;
	private ATOMId myId;
	
	private static final int START = 0;
	protected static final int RSS = 1;
	protected static final int CHANNEL = 2;
	protected static final int C_TITLE = 3;
	protected static final int C_LINK = 4;
	protected static final int ITEM = 5;
	protected static final int TITLE = 6;
	protected static final int LINK = 7;
	protected static final int COMMENTS = 8;
	protected static final int PUBDATE = 9;
	protected static final int CATEGORY = 10;
	protected static final int GUID = 11;
	protected static final int DESCRIPTION = 12;
	protected static final int CONTENT = 13;
	protected static final int COMMENTS_RSS = 14;
	
	protected static final String TAG_RSS = "rss";
	protected static final String TAG_CHANNEL = "channel";
	protected static final String TAG_ITEM = "item";
	protected static final String TAG_TITLE = "title";
	protected static final String TAG_LINK = "link";
	protected static final String TAG_GUID = "guid";
	protected static final String TAG_DESCRIPTION = "description";
	protected static final String TAG_PUBDATE = "pubDate";
	
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
		switch (myState) {
			case START:
				if(testTag(ns, TAG_RSS, tag)) {
					myState = RSS;
				}
				break;
			case RSS:
	            if (testTag(ns, TAG_CHANNEL, tag)) {
	                myState = CHANNEL;
	            }
	            break;
			case CHANNEL:
	            if (testTag(ns, TAG_TITLE, tag)) {
	                myState = C_TITLE;
	            }
	            if (testTag(ns, TAG_LINK, tag)) {                
	                myState = C_LINK;
	            }
	            if (testTag(ns, TAG_ITEM, tag)) {
	            	myItem = myFeedHandler.createEntry(attributes);
	                myState = ITEM;
	            }
	            break;
	        case ITEM:
	            if (testTag(ns, TAG_TITLE, tag)) {
	            	myAuthor = new RSSAuthor(attributes);
	                myState = TITLE;
	            }
	            if (testTag(ns, TAG_LINK, tag)) {
	                myState = LINK;
	            }
	            if (testTag(ns, TAG_DESCRIPTION, tag)) {
	                myState = DESCRIPTION;
	            }
	            if (testTag(ns, TAG_GUID, tag)) {
	            	myId = new ATOMId();
	                myState = GUID;
	            }
	            if (testTag(ns, TAG_PUBDATE, tag)) {
	                myState = PUBDATE;
	            }
		}
		return false;
	}
	
	public boolean testTag(String ns, String name, String tag){
		return (ns == XMLNamespaces.Atom && name == tag) ? true : false;
	}
	
	public boolean endElementHandler(String ns, String tag, String bufferContent) {
		switch (myState) {
			case START:
				break;
			case RSS:
				if (testTag(ns, TAG_RSS, tag)) {
					myState = START;
				}
				break;
			case CHANNEL:
				if (testTag(ns, TAG_CHANNEL, tag)) {
					myState = RSS;
				}
				break;
			case C_TITLE:
				if (testTag(ns, TAG_TITLE, tag)) {
					myState = CHANNEL;
				}
				break;
			case C_LINK:
				if (testTag(ns, TAG_LINK, tag)) {                
					myState = CHANNEL;
				}
				break;
			case ITEM:
				if (testTag(ns, TAG_ITEM, tag)) {
					myFeedHandler.processFeedEntry(myItem);
					myState = CHANNEL;
				}
			case TITLE:
				if (testTag(ns, TAG_TITLE, tag)) {
					String mark = "~ by:";
					int foundIndex = bufferContent.indexOf(mark);
					if(foundIndex >= 0){
						if(myAuthor != null){
							String title = bufferContent.substring(0, foundIndex);
							myItem.Title = title;
							String authorName = bufferContent.substring(foundIndex+mark.length());
							myAuthor.Name = authorName;
							myItem.Authors.push(myAuthor);
							myAuthor = null;
						}
					}else{
						myItem.Title = bufferContent;
					}
					myState = ITEM;
				}
				break;
			case GUID:
				if (testTag(ns, TAG_GUID, tag)) {
					if(myId != null){
						myId.Uri = bufferContent;
						myItem.Id = myId;
						myId = null;
					}
					myState = ITEM;
				}
				break;
			case DESCRIPTION:
				if (testTag(ns, TAG_DESCRIPTION, tag)) {
					myItem.Summary = bufferContent;
					myState = ITEM;
				}
				break;
			case PUBDATE:
				if (testTag(ns, TAG_PUBDATE, tag)) {
					myState = ITEM;
				}
				break;
			case LINK:
				if (testTag(ns, TAG_LINK, tag)) {
					myState = ITEM;
				}
				break;
		}
		return false;
	}
	
	@Override
	public final void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		myNamespaceMap = namespaceMap;
	}
	
	protected final String getNamespace(String prefix) {
		return XMLNamespaces.Atom;
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
