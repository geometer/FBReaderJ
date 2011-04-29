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

public class OPDSXMLReader extends ATOMXMLReader {
	public static final String OPDS_LOG = "opds.log";
	public static final String KEY_PRICE = "price";

	private DCDate myDCIssued;
	private String myPriceCurrency;

	//private ATOMTitle myTitle;      // TODO: implement ATOMTextConstruct & ATOMTitle
	//private ATOMSummary mySummary;  // TODO: implement ATOMTextConstruct & ATOMSummary

	private String myDublinCoreNamespaceId;
	private String myOpenSearchNamespaceId;
	private String myCalibreNamespaceId;
	private String myOpdsNamespaceId;

	public OPDSXMLReader(OPDSFeedReader feedReader) {
		super(feedReader);
	}

	protected final OPDSFeedMetadata getOPDSFeed() {
		return (OPDSFeedMetadata)getATOMFeed();
	}

	protected final OPDSEntry getOPDSEntry() {
		return (OPDSEntry)getATOMEntry();
	}

	protected final OPDSLink getOPDSLink() {
		return (OPDSLink)getATOMLink();
	}

	@Override
	public void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
		super.namespaceMapChangedHandler(namespaceMap);

		myDublinCoreNamespaceId = null;
		myOpenSearchNamespaceId = null;
		myCalibreNamespaceId = null;
		myOpdsNamespaceId = null;

		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			final String value = entry.getValue();
			if (value == XMLNamespaces.DublinCoreTerms) {
				myDublinCoreNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.OpenSearch) {
				myOpenSearchNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.CalibreMetadata) {
				myCalibreNamespaceId = intern(entry.getKey());
			} else if (value == XMLNamespaces.Opds) {
				myOpdsNamespaceId = intern(entry.getKey());
			}
		}
	}

	private static final int FE_DC_LANGUAGE = ATOM_STATE_FIRST_UNUSED;
	private static final int FE_DC_ISSUED = ATOM_STATE_FIRST_UNUSED + 1;
	private static final int FE_DC_PUBLISHER = ATOM_STATE_FIRST_UNUSED + 2;
	private static final int FE_CALIBRE_SERIES = ATOM_STATE_FIRST_UNUSED + 3;
	private static final int FE_CALIBRE_SERIES_INDEX = ATOM_STATE_FIRST_UNUSED + 4;
	private static final int FEL_PRICE = ATOM_STATE_FIRST_UNUSED + 5;
	private static final int FEL_FORMAT = ATOM_STATE_FIRST_UNUSED + 6;
	private static final int OPENSEARCH_TOTALRESULTS = ATOM_STATE_FIRST_UNUSED + 7;
	private static final int OPENSEARCH_ITEMSPERPAGE = ATOM_STATE_FIRST_UNUSED + 8;
	private static final int OPENSEARCH_STARTINDEX = ATOM_STATE_FIRST_UNUSED + 9;
	private static final int FEC_HACK_SPAN = ATOM_STATE_FIRST_UNUSED + 10;

	private static final String TAG_PRICE = "price";
	private static final String TAG_HACK_SPAN = "span";

	private static final String DC_TAG_LANGUAGE = "language";
	private static final String DC_TAG_ISSUED = "issued";
	private static final String DC_TAG_PUBLISHER = "publisher";
	private static final String DC_TAG_FORMAT = "format";

	private static final String CALIBRE_TAG_SERIES = "series";
	private static final String CALIBRE_TAG_SERIES_INDEX = "series_index";

	private static final String OPENSEARCH_TAG_TOTALRESULTS = "totalResults";
	private static final String OPENSEARCH_TAG_ITEMSPERPAGE = "itemsPerPage";
	private static final String OPENSEARCH_TAG_STARTINDEX = "startIndex";

	@Override
	protected OPDSFeedMetadata createFeed() {
		return new OPDSFeedMetadata();
	}

	@Override
	protected OPDSLink createLink() {
		return new OPDSLink();
	}

	@Override
	protected OPDSEntry createEntry() {
		return new OPDSEntry();
	}

	@Override
	public boolean startElementHandler(final String tagPrefix, final String tag,
			final ZLStringMap attributes, final String bufferContent) {
		int state = myState;
		boolean interruptReading = super.startElementHandler(tagPrefix, tag, attributes, bufferContent);
		switch (state) {
			case FEED:
				if (tagPrefix == myOpenSearchNamespaceId) {
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
				if (tagPrefix == myDublinCoreNamespaceId) {
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
			default:
				break;
		}

		return interruptReading;
	}

	
	public boolean endElementHandler(final String tagPrefix, final String tag,
			final String bufferContent) {
		boolean interruptReading = super.endElementHandler(tagPrefix, tag, bufferContent);
		switch (myState) {
			case FEL_PRICE:
				if (tagPrefix == myOpdsNamespaceId && tag == TAG_PRICE) {
					if (bufferContent != null && myPriceCurrency != null) {
						getOPDSLink().Prices.add(new OPDSPrice(bufferContent.intern(), myPriceCurrency));
						myPriceCurrency = null;
					}
					myState = FE_LINK;
				}
				break;
			case FEL_FORMAT:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_FORMAT) {
					if (bufferContent != null) {
						getOPDSLink().Formats.add(bufferContent.intern());
					}
					myState = FE_LINK;
				}
				break;
			case FEC_HACK_SPAN:
				// FIXME: HACK
				myHtmlToString.processTextContent(true, tag, null, bufferContent);
				if (bufferContent != null) {
					getOPDSEntry().addAttribute(KEY_PRICE, bufferContent.intern());
				}
				myState = FE_CONTENT;
				break;
			case FE_DC_LANGUAGE:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_LANGUAGE) {
					// FIXME:language can be lost:buffer will be truncated, if there are extension tags inside the <dc:language> tag
					getOPDSEntry().DCLanguage = bufferContent;
					myState = F_ENTRY;
				}
				break;
			case FE_DC_ISSUED:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_ISSUED) {
					// FIXME:issued can be lost:buffer will be truncated, if there are extension tags inside the <dc:issued> tag
					if (ATOMDateConstruct.parse(bufferContent, myDCIssued)) {
						getOPDSEntry().DCIssued = myDCIssued;
					} 
					myDCIssued = null;
					myState = F_ENTRY;
				}
				break;
			case FE_DC_PUBLISHER:
				if (tagPrefix == myDublinCoreNamespaceId && tag == DC_TAG_PUBLISHER) {
					// FIXME:publisher can be lost:buffer will be truncated, if there are extension tags inside the <dc:publisher> tag
					getOPDSEntry().DCPublisher = bufferContent;
					myState = F_ENTRY;
				}
				break;
			case FE_CALIBRE_SERIES:
				if (tagPrefix == myCalibreNamespaceId && tag == CALIBRE_TAG_SERIES) {
					getOPDSEntry().SeriesTitle = bufferContent;
					myState = F_ENTRY;
				}
				break;
			case FE_CALIBRE_SERIES_INDEX:
				if (tagPrefix == myCalibreNamespaceId && tag == CALIBRE_TAG_SERIES_INDEX) {
					if (bufferContent != null) {
						try {
							getOPDSEntry().SeriesIndex = Float.parseFloat(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = F_ENTRY;
				}
				break;
			case OPENSEARCH_TOTALRESULTS:
				if (tagPrefix == myOpenSearchNamespaceId &&
						tag == OPENSEARCH_TAG_TOTALRESULTS) {
					if (getOPDSFeed() != null && bufferContent != null) {
						try {
							getOPDSFeed().OpensearchTotalResults = Integer.parseInt(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				break;
			case OPENSEARCH_ITEMSPERPAGE:
				if (tagPrefix == myOpenSearchNamespaceId &&
						tag == OPENSEARCH_TAG_ITEMSPERPAGE) {
					if (getOPDSFeed() != null && bufferContent != null) {
						try {
							getOPDSFeed().OpensearchItemsPerPage = Integer.parseInt(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				break;
			case OPENSEARCH_STARTINDEX:
				if (tagPrefix == myOpenSearchNamespaceId &&
						tag == OPENSEARCH_TAG_STARTINDEX) {
					if (getOPDSFeed() != null && bufferContent != null) {
						try {
							getOPDSFeed().OpensearchStartIndex = Integer.parseInt(bufferContent);
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
