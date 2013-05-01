package org.geometerplus.fbreader.network.litres.readers;

import java.util.LinkedList;
import java.util.Map;

import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.fbreader.network.atom.ATOMId;
import org.geometerplus.fbreader.network.atom.FormattedBuffer;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationXMLReader;
import org.geometerplus.fbreader.network.litres.LitresBookItem;
import org.geometerplus.fbreader.network.litres.LitresFeedMetadata;
import org.geometerplus.fbreader.network.litres.genre.LitResGenre;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresXMLReader extends LitResAuthenticationXMLReader {
	private int myIndex;

	private String myBookId;
	private String myTitle;
	//private String myLanguage;
	//private String myDate;
	private String mySeriesTitle;
	private int myIndexInSeries;

	private CharSequence mySummary;
	
	private final UrlInfoCollection<UrlInfo> myUrls = new UrlInfoCollection<UrlInfo>();
	private LinkedList<LitResGenre> myGenresTree;
	private Map<String, LitResGenre> myGenresMap;
	private Map<LitResGenre, String> myGenresTitles;
	
	private String myAuthorFirstName;
	private String myAuthorMiddleName;
	private String myAuthorLastName;
	//private LinkedList<OPDSBookItem.AuthorData> myAuthors = new LinkedList<OPDSBookItem.AuthorData>();

	private LinkedList<String> myTags = new LinkedList<String>();
	
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

	protected static final String TAG_CATALOG = "catalit-fb2-books";
	protected static final String TAG_BOOK = "fb2-book";
	protected static final String TAG_TEXT_DESCRIPTION = "text_description";
	protected static final String TAG_HIDDEN = "hidden";
	protected static final String TAG_TITLE_INFO = "title-info";
	protected static final String TAG_GENRE = "genre";
	protected static final String TAG_AUTHOR = "author";
	protected static final String TAG_FIRST_NAME = "first-name";
	protected static final String TAG_MIDDLE_NAME = "middle-name";
	protected static final String TAG_LAST_NAME = "last-name";
	protected static final String TAG_BOOK_TITLE = "book-title";
	protected static final String TAG_ANNOTATION = "annotation";
	protected static final String TAG_DATE = "date";
	protected static final String TAG_SEQUENCE = "sequence";
	protected static final String TAG_LANGUAGE = "lang";

	private int myState = START;
	private final StringBuilder myBuffer = new StringBuilder();
	private FormattedBuffer myAnnotationBuffer = new FormattedBuffer(FormattedBuffer.Type.XHtml);
	private ATOMFeedHandler<LitresFeedMetadata,LitresEntry> myHandler = null;
	private LitresEntry myEntry;
	
	public LitresXMLReader() {
		super("");
	}
	
	public void setHandler(ATOMFeedHandler<LitresFeedMetadata,LitresEntry> handler) {
		myHandler = handler;
	}
	
	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.intern();

		switch (myState) {
			case START:
				if (TAG_CATALOG == tag) {
					myState = CATALOG;
				}
				break;
			case CATALOG:
				if (TAG_BOOK == tag) {
					myEntry = new LitresEntry(new ZLStringMap());
					
					myBookId = attributes.getValue("hub_id");
					myUrls.addInfo(new UrlInfo(
						UrlInfo.Type.Image, attributes.getValue("cover_preview"), MimeType.IMAGE_AUTO
					));

					myUrls.addInfo(new BookUrlInfo(
						UrlInfo.Type.BookConditional,
						BookUrlInfo.Format.FB2_ZIP,
						"https://robot.litres.ru/pages/catalit_download_book/?art=" + myBookId,
						MimeType.APP_FB2_ZIP
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
				myAnnotationBuffer.appendText(myBuffer);
				myAnnotationBuffer.appendStartTag(tag, attributes);
				break;
		}

		myBuffer.delete(0, myBuffer.length());
		return false;
	}


	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.intern();

		switch (myState) {
			case CATALOG:
				if (TAG_CATALOG == tag) {
					myState = START;
				}
				break;
			case BOOK:
				if (TAG_BOOK == tag) {
					myUrls.addInfo(new UrlInfo(
						UrlInfo.Type.SingleEntry,
						"http://data.fbreader.org/catalogs/litres2/full.php5?id=" + myBookId,
						MimeType.APP_ATOM_XML_ENTRY
					));
					
					if(myEntry != null){
						myEntry.Summary = mySummary;
						ATOMId myId = new ATOMId();
						myId.Uri = myBookId;
						myEntry.Id = myId;
						myEntry.myUrls = myUrls;
						myEntry.SeriesTitle = mySeriesTitle;
						myEntry.SeriesIndex = myIndexInSeries;
					}
					
					if(myHandler != null){
						myHandler.processFeedEntry(myEntry);
					}
					
					/*Books.add(new OPDSBookItem(
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
						myUrls
					));*/

					myBookId = myTitle = /*myLanguage = myDate = */mySeriesTitle = null;
					mySummary = null;
					myIndexInSeries = 0;
					//myAuthors.clear();
					myTags.clear();
					myUrls.clear();
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
					myEntry.myAuthors.add(new LitresBookItem.AuthorData(displayName.toString().trim(), myAuthorLastName));
					//myAuthors.add(new OPDSBookItem.AuthorData(displayName.toString().trim(), myAuthorLastName));
					myAuthorFirstName = null;
					myAuthorMiddleName = null;
					myAuthorLastName = null;
					myState = TITLE_INFO;
				}
				break;
			case FIRST_NAME:
				if (TAG_FIRST_NAME == tag) {
					myAuthorFirstName = myBuffer.toString();
					myState = AUTHOR;
				}
				break;
			case MIDDLE_NAME:
				if (TAG_MIDDLE_NAME == tag) {
					myAuthorMiddleName = myBuffer.toString();
					myState = AUTHOR;
				}
				break;
			case LAST_NAME:
				if (TAG_LAST_NAME == tag) {
					System.out.println("[LitresXMLReader] LAST_NAME: "+myBuffer);
					myAuthorLastName = myBuffer.toString();
					myState = AUTHOR;
				}
				break;
			case GENRE:
				if (TAG_GENRE == tag) {
					myState = TITLE_INFO;
					System.out.println("GENRE: "+myBuffer);
					myEntry.myGenre = myBuffer.toString();
				}
				break;
			case BOOK_TITLE:
				if (TAG_BOOK_TITLE == tag) {
					myTitle = myBuffer.toString();
					myEntry.Title = myBuffer.toString();
					myState = TITLE_INFO;
				}
				break;
			case ANNOTATION:
				myAnnotationBuffer.appendText(myBuffer);
				if (TAG_ANNOTATION == tag) {
					mySummary = myAnnotationBuffer.getText();
					myAnnotationBuffer.reset();
					myState = TITLE_INFO;
				} else {
					myAnnotationBuffer.appendEndTag(tag);
				}
				break;
			case DATE:
				if (TAG_DATE == tag) {
					//myDate = myBuffer.toString();
					myState = TITLE_INFO;
				}
				break;
			case LANGUAGE:
				if (TAG_LANGUAGE == tag) {
					//myLanguage = myBuffer.toString();
					myState = TITLE_INFO;
				}
				break;
		}

		myBuffer.delete(0, myBuffer.length());
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int length) {
		myBuffer.append(data, start, length);
	}
}
