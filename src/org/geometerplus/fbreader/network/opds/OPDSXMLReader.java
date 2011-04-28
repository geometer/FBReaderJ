/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.network.atom.*;

import android.util.Log;

public class OPDSXMLReader extends ATOMXMLReader {
	public static final String OPDS_LOG = "opds.log";
	public static final String KEY_PRICE = "price";

	protected final OPDSFeedReader myFeedReader;

	private DCDate myDCIssued;

	private String myPriceCurrency;

	//private ATOMTitle myTitle;      // TODO: implement ATOMTextConstruct & ATOMTitle
	//private ATOMSummary mySummary;  // TODO: implement ATOMTextConstruct & ATOMSummary


	public OPDSXMLReader(OPDSFeedReader feedReader) {
		super(new ATOMFeedReader() {
			
			@Override
			public void processFeedStart() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean processFeedMetadata(ATOMFeedMetadata feed,
					boolean beforeEntries) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean processFeedEntry(ATOMEntry entry) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void processFeedEnd() {
				// TODO Auto-generated method stub
				
			}
		});
		myFeedReader = feedReader;
	}

	protected String myDublinCoreNamespaceId;
	protected String myOpenSearchNamespaceId;
	protected String myCalibreNamespaceId;
	protected String myOpdsNamespaceId;

	@Override
	public void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		myDublinCoreNamespaceId = null;
		myAtomNamespaceId = null;
		myOpenSearchNamespaceId = null;
		myCalibreNamespaceId = null;
		myOpdsNamespaceId = null;

		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			final String value = entry.getValue();
			if (value == XMLNamespaces.DublinCoreTerms) {
				myDublinCoreNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.Atom) {
				myAtomNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.OpenSearch) {
				myOpenSearchNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.CalibreMetadata) {
				myCalibreNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.Opds) {
				myOpdsNamespaceId = intern(entry.getKey());
			}
		}
	}


	protected static final int FE_DC_LANGUAGE = 21;
	protected static final int FE_DC_ISSUED = 22;
	protected static final int FE_DC_PUBLISHER = 23;
	protected static final int FE_CALIBRE_SERIES = 24;
	protected static final int FE_CALIBRE_SERIES_INDEX = 25;
	protected static final int FEL_PRICE = 26;
	protected static final int FEL_FORMAT = 27;
	protected static final int OPENSEARCH_TOTALRESULTS = 31;
	protected static final int OPENSEARCH_ITEMSPERPAGE = 32;
	protected static final int OPENSEARCH_STARTINDEX = 33;
	protected static final int FEC_HACK_SPAN = 34;

	protected static final String TAG_PRICE = "price";
	protected static final String TAG_HACK_SPAN = "span";

	protected static final String DC_TAG_LANGUAGE = "language";
	protected static final String DC_TAG_ISSUED = "issued";
	protected static final String DC_TAG_PUBLISHER = "publisher";
	protected static final String DC_TAG_FORMAT = "format";

	protected static final String CALIBRE_TAG_SERIES = "series";
	protected static final String CALIBRE_TAG_SERIES_INDEX = "series_index";

	protected static final String OPENSEARCH_TAG_TOTALRESULTS = "totalResults";
	protected static final String OPENSEARCH_TAG_ITEMSPERPAGE = "itemsPerPage";
	protected static final String OPENSEARCH_TAG_STARTINDEX = "startIndex";


	private int myState = START;
	private HtmlToString myHtmlToString = new HtmlToString();
	private boolean myFeedMetadataProcessed;

	protected final int getState() {
		return myState;
	}

	public boolean startElementHandler(final String tagPrefix, final String tag,
			final ZLStringMap attributes, final String bufferContent) {
		super.startElementHandler(tagPrefix, tag, attributes, bufferContent);
		
		boolean interruptReading = false;
		switch (myState) {
			case START:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_FEED) {
					myFeedReader.processFeedStart();
					myFeed = new OPDSFeedMetadata();
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
						myLink = new OPDSLink();
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
						myEntry = new OPDSEntry();
						myEntry.readAttributes(attributes);
						myState = F_ENTRY;
						// Process feed metadata just before first feed entry
						if (myFeed != null && !myFeedMetadataProcessed) {
							if (myFeed instanceof OPDSFeedMetadata){
								OPDSFeedMetadata feed = (OPDSFeedMetadata) myFeed;
								interruptReading = myFeedReader.processFeedMetadata(feed, true);
								myFeedMetadataProcessed = true;
							} else {
								Log.v(OPDS_LOG, "! (myFeed instanceof OPDSFeedMetadata)");
							}
						}
					} 
				} else if (tagPrefix == myOpenSearchNamespaceId) {
					if (tag == OPENSEARCH_TAG_TOTALRESULTS) {
						myState = OPENSEARCH_TOTALRESULTS;
					} else if (tag == OPENSEARCH_TAG_ITEMSPERPAGE) {
						myState = OPENSEARCH_ITEMSPERPAGE;
					} else if (tag == OPENSEARCH_TAG_STARTINDEX) {
						myState = OPENSEARCH_STARTINDEX;
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
						myLink = new OPDSLink();
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
				} else if (tagPrefix == myDublinCoreNamespaceId) {
					if (tag == DC_TAG_LANGUAGE) {
						myState = FE_DC_LANGUAGE;
					} else if (tag == DC_TAG_ISSUED) {
						myDCIssued = new DCDate();
						myDCIssued.readAttributes(attributes);
						myState = FE_DC_ISSUED;
					} else if (tag == DC_TAG_PUBLISHER) {
						myState = FE_DC_PUBLISHER;
					} 
				} else if (tagPrefix == myCalibreNamespaceId) {
					if (tag == CALIBRE_TAG_SERIES) {
						myState = FE_CALIBRE_SERIES;
					} else if (tag == CALIBRE_TAG_SERIES_INDEX) {
						myState = FE_CALIBRE_SERIES_INDEX;
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
			case FE_LINK:
				if (tagPrefix == myOpdsNamespaceId && tag == TAG_PRICE) {
					myPriceCurrency = attributes.getValue("currencycode");
					myState = FEL_PRICE;
				} if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_FORMAT) {
					myState = FEL_FORMAT;
				}
				break;
			case FE_CONTENT:
				myHtmlToString.processTextContent(false, tag, attributes, bufferContent);
				// FIXME: HACK: html handling must be implemeted neatly
				if (tag == TAG_HACK_SPAN || attributes.getValue("class") == "price") {
					myState = FEC_HACK_SPAN;
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
						if (myFeed instanceof OPDSFeedMetadata){
							interruptReading = myFeedReader.processFeedMetadata((OPDSFeedMetadata)myFeed, false);
						} else {
							Log.v(OPDS_LOG, "! (myFeed instanceof OPDSFeedMetadata)");
						}
					}
					myFeed = null;
					myFeedReader.processFeedEnd();
					myState = START;
				} 
				break;
			case F_ENTRY:
				if (tagPrefix == myAtomNamespaceId && tag == TAG_ENTRY) {
					if (myEntry != null) {
						if (myEntry instanceof OPDSEntry){
							OPDSEntry entry = (OPDSEntry) myEntry;
							interruptReading = myFeedReader.processFeedEntry(entry);
						} else {
							Log.v(OPDS_LOG, "! (myEntry instanceof OPDSEntry)");
						}
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
			case FEL_PRICE:
				if (tagPrefix == myOpdsNamespaceId && tag == TAG_PRICE) {
					if (bufferContent != null && myPriceCurrency != null) {
						if (myLink instanceof OPDSLink){
							((OPDSLink)myLink).Prices.add(new OPDSPrice(bufferContent.intern(), myPriceCurrency));
							myPriceCurrency = null;
						} else {
							Log.v(OPDS_LOG, "! (myLink instanceof OPDSLink)");
						}
					}
					myState = FE_LINK;
				}
				break;
			case FEL_FORMAT:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_FORMAT) {
					if (bufferContent != null) {
						if (myLink instanceof OPDSLink){
							((OPDSLink)myLink).Formats.add(bufferContent.intern());
						} else {
							Log.v(OPDS_LOG, "! (myLink instanceof OPDSLink)");
						}
					}
					myState = FE_LINK;
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
			case FEC_HACK_SPAN:
				// FIXME: HACK
				myHtmlToString.processTextContent(true, tag, null, bufferContent);
				if (bufferContent != null) {
					myEntry.addAttribute(KEY_PRICE, bufferContent.intern());
				}
				myState = FE_CONTENT;
				break;
			case FE_DC_LANGUAGE:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_LANGUAGE) {
					// FIXME:language can be lost:buffer will be truncated, if there are extension tags inside the <dc:language> tag
					if (myEntry instanceof OPDSEntry){
						((OPDSEntry)myEntry).DCLanguage = bufferContent;
						myState = F_ENTRY;
					} else {
						Log.v(OPDS_LOG, "! (myEntry instanceof OPDSEntry)");
					}
				}
				break;
			case FE_DC_ISSUED:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_ISSUED) {
					// FIXME:issued can be lost:buffer will be truncated, if there are extension tags inside the <dc:issued> tag
					if (ATOMDateConstruct.parse(bufferContent, myDCIssued)) {
						if (myEntry instanceof OPDSEntry){
							((OPDSEntry)myEntry).DCIssued = myDCIssued;
						} else {
							Log.v(OPDS_LOG, "! (myEntry instanceof OPDSEntry)");
						}
					} 
					myDCIssued = null;
					myState = F_ENTRY;
				}
				break;
			case FE_DC_PUBLISHER:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_PUBLISHER) {
					// FIXME:publisher can be lost:buffer will be truncated, if there are extension tags inside the <dc:publisher> tag
					if (myEntry instanceof OPDSEntry){
						((OPDSEntry)myEntry).DCPublisher = bufferContent;
						myState = F_ENTRY;
					} else {
						Log.v(OPDS_LOG, "! (myEntry instanceof OPDSEntry)");
					}
				}
				break;
			case FE_CALIBRE_SERIES:
				if (tagPrefix == myCalibreNamespaceId && tag == CALIBRE_TAG_SERIES) {
					if (myEntry instanceof OPDSEntry){
						((OPDSEntry)myEntry).SeriesTitle = bufferContent;
						myState = F_ENTRY;
					} else {
						Log.v(OPDS_LOG, "! (myEntry instanceof OPDSEntry)");
					}
				}
				break;
			case FE_CALIBRE_SERIES_INDEX:
				if (tagPrefix == myCalibreNamespaceId && tag == CALIBRE_TAG_SERIES_INDEX) {
					if (bufferContent != null) {
						try {
							if (myEntry instanceof OPDSEntry){
								((OPDSEntry)myEntry).SeriesIndex = Float.parseFloat(bufferContent);
							} else {
								Log.v(OPDS_LOG, "! (myEntry instanceof OPDSEntry)");
							}
						} catch (NumberFormatException ex) {
						}
					}
					myState = F_ENTRY;
				}
				break;
			case OPENSEARCH_TOTALRESULTS:
				if (tagPrefix == myOpenSearchNamespaceId &&
						tag == OPENSEARCH_TAG_TOTALRESULTS) {
					if (myFeed != null && bufferContent != null) {
						try {
							if (myFeed instanceof OPDSFeedMetadata){
								((OPDSFeedMetadata)myFeed).OpensearchTotalResults = Integer.parseInt(bufferContent);
							} else {
								Log.v(OPDS_LOG, "! (myFeed instanceof OPDSFeedMetadata)");
							}
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				break;
			case OPENSEARCH_ITEMSPERPAGE:
				if (tagPrefix == myOpenSearchNamespaceId &&
						tag == OPENSEARCH_TAG_ITEMSPERPAGE) {
					if (myFeed != null && bufferContent != null) {
						try {
							if (myFeed instanceof OPDSFeedMetadata){
								((OPDSFeedMetadata)myFeed).OpensearchItemsPerPage = Integer.parseInt(bufferContent);
							} else {
								Log.v(OPDS_LOG, "! (myFeed instanceof OPDSFeedMetadata)");
							}
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				break;
			case OPENSEARCH_STARTINDEX:
				if (tagPrefix == myOpenSearchNamespaceId &&
						tag == OPENSEARCH_TAG_STARTINDEX) {
					if (myFeed != null && bufferContent != null) {
						try {
							if (myFeed instanceof OPDSFeedMetadata){
								((OPDSFeedMetadata)myFeed).OpensearchStartIndex = Integer.parseInt(bufferContent);
							} else {
								Log.v(OPDS_LOG, "! (myFeed instanceof OPDSFeedMetadata)");
							}
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				break;
		}

		return interruptReading;
	}

}
