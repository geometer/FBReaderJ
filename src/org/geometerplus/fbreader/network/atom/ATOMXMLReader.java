package org.geometerplus.fbreader.network.atom;

import java.util.Map;

import org.geometerplus.fbreader.network.opds.HtmlToString;
import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public class ATOMXMLReader extends ZLXMLReaderAdapter {
	
	protected final ATOMFeedReader myFeedReader;

	protected ATOMFeedMetadata myFeed;
	protected ATOMEntry myEntry;
	protected ATOMAuthor myAuthor;
	protected ATOMId myId;
	protected ATOMLink myLink;
	protected ATOMCategory myCategory;
	protected ATOMUpdated myUpdated;
	protected ATOMPublished myPublished;
	protected ATOMIcon myIcon;
	
	public ATOMXMLReader(ATOMFeedReader feedReader) {
		myFeedReader = feedReader;
	}

	protected String myAtomNamespaceId;

	@Override
	public final boolean processNamespaces() {
		return true;
	}

	public static String intern(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		return str.intern();
	}

	@Override
	public void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		myAtomNamespaceId = null;

		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			final String value = entry.getValue();
			if (value == XMLNamespaces.Atom) {
				myAtomNamespaceId = intern(entry.getKey());
			} 
		}
	}

	protected static final int START = 0;
	protected static final int FEED = 1;
	protected static final int F_ENTRY = 2;
	protected static final int F_ID = 3;
	protected static final int F_LINK = 4;
	protected static final int F_CATEGORY = 5;
	protected static final int F_TITLE = 6;
	protected static final int F_UPDATED = 7;
	protected static final int F_AUTHOR = 8;
	protected static final int FA_NAME = 9;
	protected static final int FA_URI = 10;
	protected static final int FA_EMAIL = 11;
	protected static final int FE_AUTHOR = 12;
	protected static final int FE_ID = 13;
	protected static final int FE_CATEGORY = 14;
	protected static final int FE_LINK = 15;
	protected static final int FE_PUBLISHED = 16;
	protected static final int FE_SUMMARY = 17;
	protected static final int FE_CONTENT = 18;
	protected static final int FE_TITLE = 19;
	protected static final int FE_UPDATED = 20;
	protected static final int FEA_NAME = 28;
	protected static final int FEA_URI = 29;
	protected static final int FEA_EMAIL = 30;
	protected static final int F_SUBTITLE = 35;
	protected static final int F_ICON = 36;


	protected static final String TAG_FEED = "feed";
	protected static final String TAG_ENTRY = "entry";
	protected static final String TAG_AUTHOR = "author";
	protected static final String TAG_NAME = "name";
	protected static final String TAG_URI = "uri";
	protected static final String TAG_EMAIL = "email";
	protected static final String TAG_ID = "id";
	protected static final String TAG_CATEGORY = "category";
	protected static final String TAG_LINK = "link";
	protected static final String TAG_PUBLISHED = "published";
	protected static final String TAG_SUMMARY = "summary";
	protected static final String TAG_CONTENT = "content";
	protected static final String TAG_TITLE = "title";
	protected static final String TAG_UPDATED = "updated";
	protected static final String TAG_SUBTITLE = "subtitle";
	protected static final String TAG_ICON = "icon";

	
	protected int myState = START;
	protected final StringBuilder myBuffer = new StringBuilder();
	protected HtmlToString myHtmlToString = new HtmlToString();
	protected boolean myFeedMetadataProcessed;

	protected int getState() {
		return myState;
	}

	@Override
	public final boolean startElementHandler(String tag, ZLStringMap attributes) {
		final int index = tag.indexOf(':');
		final String tagPrefix;
		if (index != -1) {
			tagPrefix = tag.substring(0, index).intern();
			tag = tag.substring(index + 1).intern();
		} else {
			tagPrefix = null;
			tag = tag.intern();
		}
		return startElementHandler(tagPrefix, tag, attributes, extractBufferContent());
	}

	@Override
	public final boolean endElementHandler(String tag) {
		final int index = tag.indexOf(':');
		final String tagPrefix;
		if (index != -1) {
			tagPrefix = tag.substring(0, index).intern();
			tag = tag.substring(index + 1).intern();
		} else {
			tagPrefix = null;
			tag = tag.intern();
		}
		return endElementHandler(tagPrefix, tag, extractBufferContent());
	}

	private final String extractBufferContent() {
		final char[] bufferContentArray = myBuffer.toString().trim().toCharArray();
		myBuffer.delete(0, myBuffer.length());
		if (bufferContentArray.length == 0) {
			return null;
		}
		return new String(bufferContentArray);
	}

	public boolean startElementHandler(final String tagPrefix, final String tag,
			final ZLStringMap attributes, final String bufferContent) {
		boolean interruptReading = false;
		switch (myState) {
			case START:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_FEED) {
					myFeedReader.processFeedStart();
					myFeed = new ATOMFeedMetadata();
					myFeed.readAttributes(attributes);
					myState = FEED;
					myFeedMetadataProcessed = false;
				}
				break;
			case FEED:
				if (tagPrefix == myAtomNamespaceId) {
					if (tag == TAG_AUTHOR) {
						myAuthor = new ATOMAuthor();
						myAuthor.readAttributes(attributes);
						myState = F_AUTHOR;
					} else if (tag == TAG_ID) {
						myId = new ATOMId();
						myId.readAttributes(attributes);
						myState = F_ID;
					} else if (tag == TAG_ICON) {
						myIcon = new ATOMIcon();
						myIcon.readAttributes(attributes);
						myState = F_ICON;
					} else if (tag == TAG_LINK) {
						myLink = new ATOMLink();			// TODO
						myLink.readAttributes(attributes);
						myState = F_LINK;
					} else if (tag == TAG_CATEGORY) {
						myCategory = new ATOMCategory();
						myCategory.readAttributes(attributes);
						myState = F_CATEGORY;
					} else if (tag == TAG_TITLE) {
						//myTitle = new ATOMTitle(); // TODO:implement ATOMTextConstruct & ATOMTitle
						//myTitle.readAttributes(attributes);
						myHtmlToString.setupTextContent(attributes.getValue("type"));
						myState = F_TITLE;
					} else if (tag == TAG_SUBTITLE) {
						//mySubtitle = new ATOMTitle(); // TODO:implement ATOMTextConstruct & ATOMSubtitle
						//mySubtitle.readAttributes(attributes);
						myHtmlToString.setupTextContent(attributes.getValue("type"));
						myState = F_SUBTITLE;
					} else if (tag == TAG_UPDATED) {
						myUpdated = new ATOMUpdated();
						myUpdated.readAttributes(attributes);
						myState = F_UPDATED;
					} else if (tag == TAG_ENTRY) {
						myEntry = new ATOMEntry();
						myEntry.readAttributes(attributes);
						myState = F_ENTRY;
						// Process feed metadata just before first feed entry
						if (myFeed != null && !myFeedMetadataProcessed) {
							interruptReading = myFeedReader.processFeedMetadata(myFeed, true);
							myFeedMetadataProcessed = true;
						}
					} 
				} 
				break;
			case F_ENTRY:
				if (tagPrefix == myAtomNamespaceId) {
					if (tag == TAG_AUTHOR) {
						myAuthor = new ATOMAuthor();
						myAuthor.readAttributes(attributes);
						myState = FE_AUTHOR;
					} else if (tag == TAG_ID) {
						myId = new ATOMId();
						myId.readAttributes(attributes);
						myState = FE_ID;
					} else if (tag == TAG_CATEGORY) {
						myCategory = new ATOMCategory();
						myCategory.readAttributes(attributes);
						myState = FE_CATEGORY;
					} else if (tag == TAG_LINK) {
						myLink = new ATOMLink();				// TODO
						myLink.readAttributes(attributes);
						myState = FE_LINK;
					} else if (tag == TAG_PUBLISHED) {
						myPublished = new ATOMPublished();
						myPublished.readAttributes(attributes);
						myState = FE_PUBLISHED;
					} else if (tag == TAG_SUMMARY) {
						//mySummary = new ATOMSummary(); // TODO:implement ATOMTextConstruct & ATOMSummary
						//mySummary.readAttributes(attributes);
						myHtmlToString.setupTextContent(attributes.getValue("type"));
						myState = FE_SUMMARY;
					} else if (tag == TAG_CONTENT) {
						//myConent = new ATOMContent(); // TODO:implement ATOMContent
						//myConent.readAttributes(attributes);
						myHtmlToString.setupTextContent(attributes.getValue("type"));
						myState = FE_CONTENT;
					} else if (tag == TAG_TITLE) {
						//myTitle = new ATOMTitle(); // TODO:implement ATOMTextConstruct & ATOMTitle
						//myTitle.readAttributes(attributes);
						myHtmlToString.setupTextContent(attributes.getValue("type"));
						myState = FE_TITLE;
					} else if (tag == TAG_UPDATED) {
						myUpdated = new ATOMUpdated();
						myUpdated.readAttributes(attributes);
						myState = FE_UPDATED;
					}
				}
				break;
			case F_AUTHOR:
				if (tagPrefix == myAtomNamespaceId) {
					if (tag == TAG_NAME) {
						myState = FA_NAME;
					} else if (tag == TAG_URI) {
						myState = FA_URI;
					} else if (tag == TAG_EMAIL) {
						myState = FA_EMAIL;
					} 
				} 
				break;
			case FE_AUTHOR:
				if (tagPrefix == myAtomNamespaceId) {
					if (tag == TAG_NAME) {
						myState = FEA_NAME;
					} else if (tag == TAG_URI) {
						myState = FEA_URI;
					} else if (tag == TAG_EMAIL) {
						myState = FEA_EMAIL;
					} 
				}
				break;
			case FE_SUMMARY:
			case FE_TITLE:
			case F_TITLE:
			case F_SUBTITLE:
				myHtmlToString.processTextContent(false, tag, attributes, bufferContent);
				break;
			default:
				break;
		}

		return interruptReading;
	}

	public boolean endElementHandler(final String tagPrefix, final String tag,
			final String bufferContent) {
		boolean interruptReading = false;
		switch (myState) {
			case START:
				break;
			case FEED:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_FEED) {
					if (myFeed != null) {
						interruptReading = myFeedReader.processFeedMetadata(myFeed, false);
					}
					myFeed = null;
					myFeedReader.processFeedEnd();
					myState = START;
				} 
				break;
			case F_ENTRY:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_ENTRY) {
					if (myEntry != null) {
						interruptReading = myFeedReader.processFeedEntry(myEntry);
					}
					myEntry = null;
					myState = FEED;
				}
				break;
			case F_ID:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_ID) {
					// FIXME:uri can be lost:buffer will be truncated, if there are extension tags inside the <id> tag
					if (bufferContent != null && myFeed != null) {
						myId.Uri = bufferContent;
						myFeed.Id = myId;
					}
					myId = null;
					myState = FEED;
				} 
				break;
			case F_ICON:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_ICON) {
					// FIXME:uri can be lost:buffer will be truncated, if there are extension tags inside the <id> tag
					if (bufferContent != null && myFeed != null) {
						myIcon.Uri = bufferContent;
						myFeed.Icon = myIcon;
					}
					myIcon = null;
					myState = FEED;
				} 
				break;
			case F_LINK:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_LINK) {
					if (myFeed != null) {
						myFeed.Links.add(myLink);
					}
					myLink = null;
					myState = FEED;
				} 
				break;
			case F_CATEGORY:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_CATEGORY) {
					if (myFeed != null) {
						myFeed.Categories.add(myCategory);
					}
					myCategory = null;
					myState = FEED;
				} 
				break;
			case F_TITLE:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_TITLE) {
					// TODO:implement ATOMTextConstruct & ATOMTitle
					final String title = myHtmlToString.finishTextContent(bufferContent);
					if (myFeed != null) {
						myFeed.Title = title;
					}
					myState = FEED;
				} else {
					myHtmlToString.processTextContent(true, tag, null, bufferContent);
				} 
				break;
			case F_SUBTITLE:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_SUBTITLE) {
					// TODO:implement ATOMTextConstruct & ATOMSubtitle
					final String subtitle = myHtmlToString.finishTextContent(bufferContent);
					if (myFeed != null) {
						myFeed.Subtitle = subtitle;
					}
					myState = FEED;
				} else {
					myHtmlToString.processTextContent(true, tag, null, bufferContent);
				} 
				break;
			case F_UPDATED:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_UPDATED) {
					// FIXME:uri can be lost:buffer will be truncated, if there are extension tags inside the <id> tag
					if (ATOMDateConstruct.parse(bufferContent, myUpdated) && myFeed != null) {
						myFeed.Updated = myUpdated;
					}
					myUpdated = null;
					myState = FEED;
				} 
				break;
			case F_AUTHOR:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_AUTHOR) {
					if (myFeed != null && myAuthor.Name != null) {
						myFeed.Authors.add(myAuthor);
					}
					myAuthor = null;
					myState = FEED;
				} 
				break;
			case FA_NAME:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_NAME) {
					myAuthor.Name = bufferContent;
					myState = F_AUTHOR;
				}
				break;
			case FEA_NAME:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_NAME) {
					myAuthor.Name = bufferContent;
					myState = FE_AUTHOR;
				}
				break;
			case FA_URI:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_URI) {
					myAuthor.Uri = bufferContent;
					myState = F_AUTHOR;
				}
				break;
			case FEA_URI:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_URI) {
					myAuthor.Uri = bufferContent;
					myState = FE_AUTHOR;
				}
				break;
			case FA_EMAIL:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_EMAIL) {
					myAuthor.Email = bufferContent;
					myState = F_AUTHOR;
				}
				break;
			case FEA_EMAIL:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_EMAIL) {
					myAuthor.Email = bufferContent;
					myState = FE_AUTHOR;
				}
				break;
			case FE_AUTHOR:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_AUTHOR) {
					if (myAuthor.Name != null) {
						myEntry.Authors.add(myAuthor);
					}
					myAuthor = null;
					myState = F_ENTRY;
				} 
				break;
			case FE_ID:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_ID) {
					// FIXME:uri can be lost:buffer will be truncated, if there are extension tags inside the <id> tag
					if (bufferContent != null) {
						myId.Uri = bufferContent;
						myEntry.Id = myId;
					}
					myId = null;
					myState = F_ENTRY;
				}
				break;
			case FE_CATEGORY:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_CATEGORY) {
					myEntry.Categories.add(myCategory);
					myCategory = null;
					myState = F_ENTRY;
				}
				break;
			case FE_LINK:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_LINK) {
					myEntry.Links.add(myLink);
					myLink = null;
					myState = F_ENTRY;
				}
				break;
			case FE_PUBLISHED:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_PUBLISHED) {
					// FIXME:uri can be lost:buffer will be truncated, if there are extension tags inside the <id> tag
					if (ATOMDateConstruct.parse(bufferContent, myPublished)) {
						myEntry.Published = myPublished;
					}
					myPublished = null;
					myState = F_ENTRY;
				}
				break;
			case FE_SUMMARY:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_SUMMARY) {
					// TODO:implement ATOMTextConstruct & ATOMSummary
					myEntry.Summary = myHtmlToString.finishTextContent(bufferContent);
					myState = F_ENTRY;
				} else {
					myHtmlToString.processTextContent(true, tag, null, bufferContent);
				}
				break;
			case FE_CONTENT:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_CONTENT) {
					// TODO:implement ATOMContent
					myEntry.Content = myHtmlToString.finishTextContent(bufferContent);
					myState = F_ENTRY;
				} else {
					myHtmlToString.processTextContent(true, tag, null, bufferContent);
				}
				break;
			case FE_TITLE:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_TITLE) {
					// TODO:implement ATOMTextConstruct & ATOMTitle
					myEntry.Title = myHtmlToString.finishTextContent(bufferContent);
					myState = F_ENTRY;
				} else {
					myHtmlToString.processTextContent(true, tag, null, bufferContent);
				}
				break;
			case FE_UPDATED:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_UPDATED) {
					// FIXME:uri can be lost:buffer will be truncated, if there are extension tags inside the <id> tag
					if (ATOMDateConstruct.parse(bufferContent, myUpdated)) {
						myEntry.Updated = myUpdated;
					}
					myUpdated = null;
					myState = F_ENTRY;
				}
				break;
		}

		return interruptReading;
	}
	
	

	@Override
	public final void characterDataHandler(char[] data, int start, int length) {
		final int startIndex = myBuffer.length();
		myBuffer.append(data, start, length);
		int index = startIndex;
		while ((index = myBuffer.indexOf("\r\n", index)) != -1) {
			myBuffer.replace(index, index + 2, "\n");
		}
		index = startIndex;
		while ((index = myBuffer.indexOf("\r", index)) != -1) {
			myBuffer.setCharAt(index, '\n');
		}
	}

}
