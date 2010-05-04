/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.authentication.litres;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.HtmlToString;


class LitResXMLReader extends LitResAuthenticationXMLReader {

	public final NetworkLink Link;
	public final List<NetworkLibraryItem> Books;

	private int myIndex;

	private String myBookId;
	private String myTitle;
	private String myLanguage;
	private String myDate;
	private String mySeriesTitle;
	private int myIndexInSeries;

	private String mySummary;

	private String myCover;

	private String myAuthorFirstName;
	private String myAuthorMiddleName;
	private String myAuthorLastName;
	private LinkedList<NetworkBookItem.AuthorData> myAuthors = new LinkedList<NetworkBookItem.AuthorData>();

	private LinkedList<String> myTags = new LinkedList<String>();
	private HashMap<Integer, String> myURLByType = new HashMap<Integer, String>(); // TODO: remove
	private LinkedList<BookReference> myReferences = new LinkedList<BookReference>();

	public LitResXMLReader(NetworkLink link, List<NetworkLibraryItem> books) {
		super(link.SiteName);
		Link = link;
		Books = books;
	}


	private static final int START = 0;
	private static final int CATALOG = 1;
	private static final int BOOK = 2;
	private static final int BOOK_DESCRIPTION = 3;
	private static final int HIDDEN = 4;
	private static final int TITLE_INFO = 5;
	private static final int GENRE = 6;
	private static final int AUTHOR = 7;
	private static final int FIRST_NAME = 8;
	private static final int MIDDLE_NAME = 9;
	private static final int LAST_NAME = 10;
	private static final int BOOK_TITLE = 11;
	private static final int ANNOTATION = 12;
	private static final int DATE = 13;
	private static final int LANGUAGE = 14;

	private static final String TAG_CATALOG = "catalit-fb2-books";
	private static final String TAG_BOOK = "fb2-book";
	private static final String TAG_TEXT_DESCRIPTION = "text_description";
	private static final String TAG_HIDDEN = "hidden";
	private static final String TAG_TITLE_INFO = "title-info";
	private static final String TAG_GENRE = "genre";
	private static final String TAG_AUTHOR = "author";
	private static final String TAG_FIRST_NAME = "first-name";
	private static final String TAG_MIDDLE_NAME = "middle-name";
	private static final String TAG_LAST_NAME = "last-name";
	private static final String TAG_BOOK_TITLE = "book-title";
	private static final String TAG_ANNOTATION = "annotation";
	private static final String TAG_DATE = "date";
	private static final String TAG_SEQUENCE = "sequence";
	private static final String TAG_LANGUAGE = "lang";

	private int myState = START;
	private final StringBuilder myBuffer = new StringBuilder();
	private HtmlToString myHtmlToString = new HtmlToString();

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.intern();

		final char[] bufferContentArray = myBuffer.toString().trim().toCharArray();
		final String bufferContent;
		if (bufferContentArray.length == 0) {
			bufferContent = null;
		} else {
			bufferContent = new String(bufferContentArray);
		}
		myBuffer.delete(0, myBuffer.length());

		switch(myState) {
		case START:
			if (TAG_CATALOG == tag) {
				myState = CATALOG;
			}
			break;
		case CATALOG:
			if (TAG_BOOK == tag) {
				myBookId = attributes.getValue("hub_id");
				myCover = attributes.getValue("cover_preview");

				final String url = attributes.getValue("url");
				if (url != null) {
					myURLByType.put(NetworkCatalogItem.URL_HTML_PAGE, url);
				}

				myReferences.add(new BookReference(
					"https://robot.litres.ru/pages/catalit_download_book/?art=" + myBookId,
					BookReference.Format.FB2_ZIP,
					BookReference.Type.DOWNLOAD_FULL_CONDITIONAL
				));
				myState = BOOK;
			}
			break;
		case BOOK:
			if (TAG_TEXT_DESCRIPTION == tag) {
				myState = BOOK_DESCRIPTION;
			}
			break;
		case BOOK_DESCRIPTION:
			if (TAG_HIDDEN == tag) {
				myState = HIDDEN;
			}
			break;
		case HIDDEN:
			if (TAG_TITLE_INFO == tag) {
				myState = TITLE_INFO;
			}
			break;
		case TITLE_INFO:
			if (TAG_GENRE == tag) {
				myState = GENRE;
			} else if (TAG_AUTHOR == tag) {
				myState = AUTHOR;
			} else if (TAG_BOOK_TITLE == tag) {
				myState = BOOK_TITLE;
			} else if (TAG_ANNOTATION == tag) {
				myHtmlToString.setupTextContent("text/xhtml");
				myState = ANNOTATION;
			} else if (TAG_DATE == tag) {
				myState = DATE;
			} else if (TAG_LANGUAGE == tag) {
				myState = LANGUAGE;
			} else if (TAG_SEQUENCE == tag) {
				mySeriesTitle = attributes.getValue("name");
				if (mySeriesTitle != null) {
					myIndexInSeries = 0;
					final String indexInSeries = attributes.getValue("number");
					if (indexInSeries != null) {
						try {
							myIndexInSeries = Integer.parseInt(indexInSeries);
						} catch (NumberFormatException e) {
						}
					}
				}
				//myState = SEQUENCE; // handled through attributes without state
			}
			break;
		case AUTHOR:
			if (TAG_FIRST_NAME == tag) {
				myState = FIRST_NAME;
			} else if (TAG_MIDDLE_NAME == tag) {
				myState = MIDDLE_NAME;
			} else if (TAG_LAST_NAME == tag) {
				myState = LAST_NAME;
			}
			break;
		case ANNOTATION:
			myHtmlToString.processTextContent(false, tag, attributes, bufferContent);
			break;
		}
		return false;
	}


	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.intern();

		final char[] bufferContentArray = myBuffer.toString().trim().toCharArray();
		final String bufferContent;
		if (bufferContentArray.length == 0) {
			bufferContent = null;
		} else {
			bufferContent = new String(bufferContentArray);
		}
		myBuffer.delete(0, myBuffer.length());

		switch (myState) {
		case CATALOG:
			if (TAG_CATALOG == tag) {
				myState = START;
			}
			break;
		case BOOK:
			if (TAG_BOOK == tag) {

				Books.add(new NetworkBookItem(
					Link,
					myBookId,
					myIndex++,
					myTitle,
					mySummary,
					//myLanguage,
					//myDate,
					myAuthors,
					myTags,
					mySeriesTitle,
					myIndexInSeries,
					myCover,
					myReferences
				));

				myBookId = myTitle = myLanguage = myDate = mySeriesTitle = mySummary = myCover = null;
				myIndexInSeries = 0;
				myAuthors.clear();
				myTags.clear();
				myURLByType.clear();
				myReferences.clear();
				myState = CATALOG;
			}
			break;
		case BOOK_DESCRIPTION:
			if (TAG_TEXT_DESCRIPTION == tag) {
				myState = BOOK;
			}
			break;
		case HIDDEN:
			if (TAG_HIDDEN == tag) {
				myState = BOOK_DESCRIPTION;
			}
			break;
		case TITLE_INFO:
			if (TAG_TITLE_INFO == tag) {
				myState = HIDDEN;
			}
			break;
		case AUTHOR:
			if (TAG_AUTHOR == tag) {
				StringBuilder displayName = new StringBuilder();
				if (myAuthorFirstName != null) {
					displayName.append(myAuthorFirstName).append(" ");
				}
				if (myAuthorMiddleName != null) {
					displayName.append(myAuthorMiddleName).append(" ");
				}
				if (myAuthorLastName != null) {
					displayName.append(myAuthorLastName).append(" ");
				}
				myAuthors.add(new NetworkBookItem.AuthorData(displayName.toString().trim(), myAuthorLastName));
				myAuthorFirstName = null;
				myAuthorMiddleName = null;
				myAuthorLastName = null;
				myState = TITLE_INFO;
			}
			break;
		case FIRST_NAME:
			if (TAG_FIRST_NAME == tag) {
				myAuthorFirstName = bufferContent;
				myState = AUTHOR;
			}
			break;
		case MIDDLE_NAME:
			if (TAG_MIDDLE_NAME == tag) {
				myAuthorMiddleName = bufferContent;
				myState = AUTHOR;
			}
			break;
		case LAST_NAME:
			if (TAG_LAST_NAME == tag) {
				myAuthorLastName = bufferContent;
				myState = AUTHOR;
			}
			break;
		case GENRE:
			if (TAG_GENRE == tag) {
				/*if (bufferContent != null) {
					const std::map<std::string,shared_ptr<LitResGenre> > &genresMap =
						LitResGenreMap::Instance().genresMap();
					const std::map<shared_ptr<LitResGenre>,std::string> &genresTitles =
						LitResGenreMap::Instance().genresTitles();

					std::map<std::string, shared_ptr<LitResGenre> >::const_iterator it = genresMap.find(bufferContent);
					if (it != genresMap.end()) {
						std::map<shared_ptr<LitResGenre>, std::string>::const_iterator jt = genresTitles.find(it->second);
						if (jt != genresTitles.end()) {
							myTags.push_back(jt->second);
						}
					}
				}*/
				myState = TITLE_INFO;
			}
			break;
		case BOOK_TITLE:
			if (TAG_BOOK_TITLE == tag) {
				myTitle = bufferContent;
				myState = TITLE_INFO;
			}
			break;
		case ANNOTATION:
			if (TAG_ANNOTATION == tag) {
				mySummary = myHtmlToString.finishTextContent(bufferContent);
				myState = TITLE_INFO;
			} else {
				myHtmlToString.processTextContent(true, tag, null, bufferContent);
			}
			break;
		case DATE:
			if (TAG_DATE == tag) {
				myDate = bufferContent;
				myState = TITLE_INFO;
			}
			break;
		case LANGUAGE:
			if (TAG_LANGUAGE == tag) {
				myLanguage = bufferContent;
				myState = TITLE_INFO;
			}
			break;
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int length) {
		myBuffer.append(data, start, length);
	}
}
