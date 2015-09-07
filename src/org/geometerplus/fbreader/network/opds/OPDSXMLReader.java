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

package org.geometerplus.fbreader.network.opds;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.money.Money;
import org.geometerplus.zlibrary.core.money.MoneyException;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.atom.*;

public class OPDSXMLReader extends ATOMXMLReader<OPDSFeedMetadata,OPDSEntry> {
	public static final String KEY_PRICE = "price";

	private DCDate myDCIssued;
	private String myPriceCurrency;

	public OPDSXMLReader(NetworkLibrary library, ATOMFeedHandler<OPDSFeedMetadata,OPDSEntry> handler, boolean readEntryNotFeed) {
		super(library, handler, readEntryNotFeed);
	}

	protected final OPDSFeedMetadata getOPDSFeed() {
		return getATOMFeed();
	}

	protected final OPDSEntry getOPDSEntry() {
		return getATOMEntry();
	}

	protected final OPDSLink getOPDSLink() {
		return (OPDSLink)getATOMLink();
	}

	private static final int FE_DC_LANGUAGE = ATOM_STATE_FIRST_UNUSED;
	private static final int FE_DC_ISSUED = ATOM_STATE_FIRST_UNUSED + 1;
	private static final int FE_DC_PUBLISHER = ATOM_STATE_FIRST_UNUSED + 2;
	private static final int FE_DC_IDENTIFIER = ATOM_STATE_FIRST_UNUSED + 3;
	private static final int FE_CALIBRE_SERIES = ATOM_STATE_FIRST_UNUSED + 4;
	private static final int FE_CALIBRE_SERIES_INDEX = ATOM_STATE_FIRST_UNUSED + 5;
	private static final int FEL_PRICE = ATOM_STATE_FIRST_UNUSED + 6;
	private static final int FEL_FORMAT = ATOM_STATE_FIRST_UNUSED + 7;
	private static final int OPENSEARCH_TOTALRESULTS = ATOM_STATE_FIRST_UNUSED + 8;
	private static final int OPENSEARCH_ITEMSPERPAGE = ATOM_STATE_FIRST_UNUSED + 9;
	private static final int OPENSEARCH_STARTINDEX = ATOM_STATE_FIRST_UNUSED + 10;
	private static final int FEC_HACK_SPAN = ATOM_STATE_FIRST_UNUSED + 11;
	private static final int FBREADER_VIEW = ATOM_STATE_FIRST_UNUSED + 12;

	private static final String TAG_PRICE = "price";
	private static final String TAG_HACK_SPAN = "span";

	private static final String DC_TAG_LANGUAGE = "language";
	private static final String DC_TAG_ISSUED = "issued";
	private static final String DC_TAG_PUBLISHER = "publisher";
	private static final String DC_TAG_FORMAT = "format";
	private static final String DC_TAG_IDENTIFIER = "identifier";

	private static final String CALIBRE_TAG_SERIES = "series";
	private static final String CALIBRE_TAG_SERIES_INDEX = "series_index";

	private static final String OPENSEARCH_TAG_TOTALRESULTS = "totalResults";
	private static final String OPENSEARCH_TAG_ITEMSPERPAGE = "itemsPerPage";
	private static final String OPENSEARCH_TAG_STARTINDEX = "startIndex";

	private static final String FBREADER_TAG_VIEW = "view";

	@Override
	public boolean startElementHandler(String ns, String tag, ZLStringMap attributes, String bufferContent) {
		switch (myState) {
			case FEED:
				if (ns == XMLNamespaces.OpenSearch) {
					if (tag == OPENSEARCH_TAG_TOTALRESULTS) {
						myState = OPENSEARCH_TOTALRESULTS;
					} else if (tag == OPENSEARCH_TAG_ITEMSPERPAGE) {
						myState = OPENSEARCH_ITEMSPERPAGE;
					} else if (tag == OPENSEARCH_TAG_STARTINDEX) {
						myState = OPENSEARCH_STARTINDEX;
					}
					return false;
				} else if (ns == XMLNamespaces.FBReaderCatalogMetadata) {
					if (tag == FBREADER_TAG_VIEW) {
						myState = FBREADER_VIEW;
					}
				} else {
					return super.startElementHandler(ns, tag, attributes, bufferContent);
				}
			case F_ENTRY:
				if (ns == XMLNamespaces.DublinCoreTerms) {
					if (tag == DC_TAG_LANGUAGE) {
						myState = FE_DC_LANGUAGE;
					} else if (tag == DC_TAG_ISSUED) {
						myDCIssued = new DCDate(attributes);
						myState = FE_DC_ISSUED;
					} else if (tag == DC_TAG_PUBLISHER) {
						myState = FE_DC_PUBLISHER;
					} else if (tag == DC_TAG_IDENTIFIER) {
						myState = FE_DC_IDENTIFIER;
					}
					return false;
				} else if (ns == XMLNamespaces.CalibreMetadata) {
					if (tag == CALIBRE_TAG_SERIES) {
						myState = FE_CALIBRE_SERIES;
					} else if (tag == CALIBRE_TAG_SERIES_INDEX) {
						myState = FE_CALIBRE_SERIES_INDEX;
					}
					return false;
				} else {
					return super.startElementHandler(ns, tag, attributes, bufferContent);
				}
			case FE_LINK:
				if (ns == XMLNamespaces.Opds && tag == TAG_PRICE) {
					myPriceCurrency = attributes.getValue("currencycode");
					myState = FEL_PRICE;
					return false;
				} else if (ns == XMLNamespaces.DublinCoreTerms && tag == DC_TAG_FORMAT) {
					myState = FEL_FORMAT;
					return false;
				} else {
					return super.startElementHandler(ns, tag, attributes, bufferContent);
				}
			case FE_CONTENT:
				super.startElementHandler(ns, tag, attributes, bufferContent);
				// FIXME: HACK: html handling must be implemeted neatly
				if (tag == TAG_HACK_SPAN || attributes.getValue("class") == "price") {
					myState = FEC_HACK_SPAN;
				}
				return false;
			default:
				return super.startElementHandler(ns, tag, attributes, bufferContent);
		}
	}

	@Override
	public boolean endElementHandler(String ns, String tag, String bufferContent) {
		switch (myState) {
			default:
				return super.endElementHandler(ns, tag, bufferContent);
			case FEL_PRICE:
				if (ns == XMLNamespaces.Opds && tag == TAG_PRICE) {
					if (bufferContent != null && myPriceCurrency != null) {
						try {
							final Money price = new Money(bufferContent, myPriceCurrency);
							getOPDSLink().Prices.add(price);
						} catch (MoneyException e) {
							e.printStackTrace();
						}
						myPriceCurrency = null;
					}
					myState = FE_LINK;
				}
				return false;
			case FEL_FORMAT:
				if (ns == XMLNamespaces.DublinCoreTerms && tag == DC_TAG_FORMAT) {
					if (bufferContent != null) {
						getOPDSLink().Formats.add(bufferContent.intern());
					}
					myState = FE_LINK;
				}
				return false;
			case FEC_HACK_SPAN:
				// FIXME: HACK
				myFormattedBuffer.appendText(bufferContent);
				myFormattedBuffer.appendEndTag(tag);
				myFormattedBuffer.appendText("<br/>");
				if (bufferContent != null) {
					getOPDSEntry().addAttribute(KEY_PRICE, bufferContent.intern());
				}
				myState = FE_CONTENT;
				return false;
			case FE_DC_LANGUAGE:
				if (ns == XMLNamespaces.DublinCoreTerms && tag == DC_TAG_LANGUAGE) {
					// FIXME:language can be lost:buffer will be truncated, if there are extension tags inside the <dc:language> tag
					getOPDSEntry().DCLanguage = bufferContent;
					myState = F_ENTRY;
				}
				return false;
			case FE_DC_ISSUED:
				if (ns == XMLNamespaces.DublinCoreTerms && tag == DC_TAG_ISSUED) {
					// FIXME:issued can be lost:buffer will be truncated, if there are extension tags inside the <dc:issued> tag
					if (ATOMDateConstruct.parse(bufferContent, myDCIssued)) {
						getOPDSEntry().DCIssued = myDCIssued;
					}
					myDCIssued = null;
					myState = F_ENTRY;
				}
				return false;
			case FE_DC_PUBLISHER:
				if (ns == XMLNamespaces.DublinCoreTerms && tag == DC_TAG_PUBLISHER) {
					// FIXME:publisher can be lost:buffer will be truncated, if there are extension tags inside the <dc:publisher> tag
					getOPDSEntry().DCPublisher = bufferContent;
					myState = F_ENTRY;
				}
				return false;
			case FE_DC_IDENTIFIER:
				if (ns == XMLNamespaces.DublinCoreTerms && tag == DC_TAG_IDENTIFIER) {
					// FIXME:identifier can be lost:buffer will be truncated, if there are extension tags inside the <dc:publisher> tag
					getOPDSEntry().DCIdentifiers.add(bufferContent);
					myState = F_ENTRY;
				}
				return false;
			case FE_CALIBRE_SERIES:
				if (ns == XMLNamespaces.CalibreMetadata && tag == CALIBRE_TAG_SERIES) {
					getOPDSEntry().SeriesTitle = bufferContent;
					myState = F_ENTRY;
				}
				return false;
			case FE_CALIBRE_SERIES_INDEX:
				if (ns == XMLNamespaces.CalibreMetadata && tag == CALIBRE_TAG_SERIES_INDEX) {
					if (bufferContent != null) {
						try {
							getOPDSEntry().SeriesIndex = Float.parseFloat(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = F_ENTRY;
				}
				return false;
			case OPENSEARCH_TOTALRESULTS:
				if (ns == XMLNamespaces.OpenSearch && tag == OPENSEARCH_TAG_TOTALRESULTS) {
					if (getOPDSFeed() != null && bufferContent != null) {
						try {
							getOPDSFeed().OpensearchTotalResults = Integer.parseInt(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				return false;
			case OPENSEARCH_ITEMSPERPAGE:
				if (ns == XMLNamespaces.OpenSearch && tag == OPENSEARCH_TAG_ITEMSPERPAGE) {
					if (getOPDSFeed() != null && bufferContent != null) {
						try {
							getOPDSFeed().OpensearchItemsPerPage = Integer.parseInt(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				return false;
			case OPENSEARCH_STARTINDEX:
				if (ns == XMLNamespaces.OpenSearch && tag == OPENSEARCH_TAG_STARTINDEX) {
					if (getOPDSFeed() != null && bufferContent != null) {
						try {
							getOPDSFeed().OpensearchStartIndex = Integer.parseInt(bufferContent);
						} catch (NumberFormatException ex) {
						}
					}
					myState = FEED;
				}
				return false;
			case FBREADER_VIEW:
				if (ns == XMLNamespaces.FBReaderCatalogMetadata && tag == FBREADER_TAG_VIEW) {
					if (getOPDSFeed() != null) {
						getOPDSFeed().ViewType = bufferContent;
					}
					myState = FEED;
				}
				return false;
		}
	}
}
