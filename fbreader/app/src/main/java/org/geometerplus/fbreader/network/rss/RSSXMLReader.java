/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network.rss;

import java.util.Map;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.atom.*;

public class RSSXMLReader<MetadataType extends RSSChannelMetadata,EntryType extends RSSItem> extends ZLXMLReaderAdapter {
	public RSSXMLReader(NetworkLibrary library, ATOMFeedHandler<MetadataType,EntryType> handler, boolean readEntryNotFeed) {
		myFeedHandler = handler;
		myState = START;
		myFormattedBuffer = new FormattedBuffer(library);
	}

	protected int myState;
	private final ATOMFeedHandler<MetadataType,EntryType> myFeedHandler;
	private Map<String,String> myNamespaceMap;
	private final StringBuilder myBuffer = new StringBuilder();
	protected final FormattedBuffer myFormattedBuffer;
	private EntryType myItem;
	private RSSAuthor myAuthor;
	private RSSCategory myCategory;
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
	protected static final String TAG_CATEGORY = "category";
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
				if(testTag(TAG_RSS, tag, ns, null)) {
					myState = RSS;
				}
				break;
			case RSS:
				if (testTag(TAG_CHANNEL, tag, ns, null)) {
					myState = CHANNEL;
				}
				break;
			case CHANNEL:
				if (testTag(TAG_TITLE, tag, ns, null)) {
					myState = C_TITLE;
				}
				if (testTag(TAG_LINK, tag, ns, null)) {
					myState = C_LINK;
				}
				if (testTag(TAG_ITEM, tag, ns, null)) {
					myItem = myFeedHandler.createEntry(attributes);
					myState = ITEM;
				}
				break;
			case ITEM:
				if (testTag(TAG_TITLE, tag, ns, null)) {
					myAuthor = new RSSAuthor(attributes);
					myState = TITLE;
				}
				if (testTag(TAG_LINK, tag, ns, null)) {
					myState = LINK;
				}
				if (testTag(TAG_DESCRIPTION, tag, ns, null)) {
					myState = DESCRIPTION;
				}
				if (testTag(TAG_CATEGORY, tag, ns, null)) {
					myState = CATEGORY;
				}
				if (testTag(TAG_GUID, tag, ns, null)) {
					myId = new ATOMId();
					myState = GUID;
				}
				if (testTag(TAG_PUBDATE, tag, ns, null)) {
					myState = PUBDATE;
				}
		}
		return false;
	}

	public boolean endElementHandler(String ns, String tag, String bufferContent) {
		switch (myState) {
			case START:
				break;
			case RSS:
				if (testTag(TAG_RSS, tag, ns, null)) {
					myState = START;
				}
				break;
			case CHANNEL:
				if (testTag(TAG_CHANNEL, tag, ns, null)) {
					myState = RSS;
				}
				break;
			case C_TITLE:
				if (testTag(TAG_TITLE, tag, ns, null)) {
					myState = CHANNEL;
				}
				break;
			case C_LINK:
				if (testTag(TAG_LINK, tag, ns, null)) {
					myState = CHANNEL;
				}
				break;
			case ITEM:
				if (testTag(TAG_ITEM, tag, ns, null)) {
					myFeedHandler.processFeedEntry(myItem);
					myState = CHANNEL;
				}
			case TITLE:
				if (testTag(TAG_TITLE, tag, ns, null)) {
					parseTitle(bufferContent);
					myState = ITEM;
				}
				break;
			case GUID:
				if (testTag(TAG_GUID, tag, ns, null)) {
					if (myId != null) {
						myId.Uri = bufferContent;
						myItem.Id = myId;
						myId = null;
					}
					myState = ITEM;
				}
				break;
			case DESCRIPTION:
				if (testTag(TAG_DESCRIPTION, tag, ns, null)) {
					myFormattedBuffer.reset(FormattedBuffer.Type.Html);
					myFormattedBuffer.appendText(makeFormat(bufferContent));
					myItem.Summary = myFormattedBuffer.getText();
					myState = ITEM;
				}
				break;
			case CATEGORY:
				if (testTag(TAG_CATEGORY, tag, ns, null)) {
					String[] tokens = bufferContent.split(", ");
					for (String str : tokens) {
						ZLStringMap source = new ZLStringMap();
						source.put(RSSCategory.LABEL, str);
						myCategory = new RSSCategory(source);
						if (myCategory != null) {
							myItem.Categories.add(myCategory);
						}
						myCategory = null;
					}
					myState = ITEM;
				}
				break;
			case PUBDATE:
				if (testTag(TAG_PUBDATE, tag, ns, null)) {
					myState = ITEM;
				}
				break;
			case LINK:
				if (testTag(TAG_LINK, tag, ns, null)) {
					myState = ITEM;
				}
				break;
		}
		return false;
	}

	private void parseTitle(String bufferContent) {
		String[] marks = {"~ by:", "By"};
		boolean found = false;

		for (int i = 0; i < marks.length; i++) {
			int foundIndex = bufferContent.indexOf(marks[i]);
			if (foundIndex >= 0) {
				if (myAuthor != null) {
					String title = bufferContent.substring(0, foundIndex);
					myItem.Title = title;
					String authorName = bufferContent.substring(foundIndex+marks[i].length());
					myAuthor.Name = authorName.trim();
					myItem.Authors.add(myAuthor);
					myAuthor = null;
				}
				found = true;
				break;
			}
		}

		if (!found) {
			myItem.Title = bufferContent;
		}
	}

	private String makeFormat(String buffer) {
		//TODO: maybe need to make the text more readable?
		StringBuffer s1 = new StringBuffer(buffer);
		int index;
		String[] marks = {"Author:", "Price:", "Rating:"};

		for (int i = 0; i < marks.length; i++) {
			index = s1.indexOf(marks[i]);
			if (index >= 0) {
				s1.insert(index, "<br/>");
			}
		}

		return s1.toString();
	}

	public boolean testTag(String name, String tag, String ns, String nsName) {
		return name == tag && ns == nsName;
	}

	@Override
	public final boolean processNamespaces() {
		return true;
	}

	@Override
	public final void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		myNamespaceMap = namespaceMap;
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
