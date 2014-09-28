/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.book;

import java.util.*;
import java.text.DateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Xml;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.view.ZLTextPosition;

class XMLSerializer extends AbstractSerializer {
	@Override
	public String serialize(BookQuery query) {
		final StringBuilder buffer = new StringBuilder();
		appendTag(buffer, "query", false,
			"limit", String.valueOf(query.Limit),
			"page", String.valueOf(query.Page)
		);
		serialize(buffer, query.Filter);
		closeTag(buffer, "query");
		return buffer.toString();
	}

	private void serialize(StringBuilder buffer, Filter filter) {
		if (filter instanceof Filter.Empty) {
			appendTag(buffer, "filter", true,
				"type", "empty"
			);
		} else if (filter instanceof Filter.Not) {
			appendTag(buffer, "not", false);
			serialize(buffer, ((Filter.Not)filter).Base);
			closeTag(buffer, "not");
		} else if (filter instanceof Filter.And) {
			appendTag(buffer, "and", false);
			serialize(buffer, ((Filter.And)filter).First);
			serialize(buffer, ((Filter.And)filter).Second);
			closeTag(buffer, "and");
		} else if (filter instanceof Filter.Or) {
			appendTag(buffer, "or", false);
			serialize(buffer, ((Filter.Or)filter).First);
			serialize(buffer, ((Filter.Or)filter).Second);
			closeTag(buffer, "or");
		} else if (filter instanceof Filter.ByAuthor) {
			final Author author = ((Filter.ByAuthor)filter).Author;
			appendTag(buffer, "filter", true,
				"type", "author",
				"displayName", author.DisplayName,
				"sorkKey", author.SortKey
			);
		} else if (filter instanceof Filter.ByTag) {
			final LinkedList<String> lst = new LinkedList<String>();
			for (Tag t = ((Filter.ByTag)filter).Tag; t != null; t = t.Parent) {
				lst.add(0, t.Name);
			}
			final String[] params = new String[lst.size() * 2 + 2];
			int index = 0;
			params[index++] = "type";
			params[index++] = "tag";
			int num = 0;
			for (String name : lst) {
				params[index++] = "name" + num++;
				params[index++] = name;
			}
			appendTag(buffer, "filter", true, params);
		} else if (filter instanceof Filter.ByLabel) {
			appendTag(buffer, "filter", true,
				"type", "label",
				"name", ((Filter.ByLabel)filter).Label
			);
		} else if (filter instanceof Filter.BySeries) {
			appendTag(buffer, "filter", true,
				"type", "series",
				"title", ((Filter.BySeries)filter).Series.getTitle()
			);
		} else if (filter instanceof Filter.ByPattern) {
			appendTag(buffer, "filter", true,
				"type", "pattern",
				"pattern", ((Filter.ByPattern)filter).Pattern
			);
		} else if (filter instanceof Filter.ByTitlePrefix) {
			appendTag(buffer, "filter", true,
				"type", "title-prefix",
				"prefix", ((Filter.ByTitlePrefix)filter).Prefix
			);
		} else if (filter instanceof Filter.HasBookmark) {
			appendTag(buffer, "filter", true,
				"type", "has-bookmark"
			);
		} else if (filter instanceof Filter.HasPhysicalFile) {
			appendTag(buffer, "filter", true,
				"type", "has-physical-file"
			);
		} else {
			throw new RuntimeException("Unsupported filter type: " + filter.getClass());
		}
	}

	@Override
	public BookQuery deserializeBookQuery(String xml) {
		try {
			final BookQueryDeserializer deserializer = new BookQueryDeserializer();
			Xml.parse(xml, deserializer);
			return deserializer.getQuery();
		} catch (SAXException e) {
			System.err.println(xml);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String serialize(BookmarkQuery query) {
		final StringBuilder buffer = new StringBuilder();
		appendTag(buffer, "query", false,
			"visible", String.valueOf(query.Visible),
			"limit", String.valueOf(query.Limit),
			"page", String.valueOf(query.Page)
		);
		if (query.Book != null) {
			serialize(buffer, query.Book);
		}
		closeTag(buffer, "query");
		return buffer.toString();
	}

	@Override
	public BookmarkQuery deserializeBookmarkQuery(String xml) {
		try {
			final BookmarkQueryDeserializer deserializer = new BookmarkQueryDeserializer();
			Xml.parse(xml, deserializer);
			return deserializer.getQuery();
		} catch (SAXException e) {
			System.err.println(xml);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String serialize(Book book) {
		final StringBuilder buffer = new StringBuilder();
		serialize(buffer, book);
		return buffer.toString();
	}

	private void serialize(StringBuilder buffer, Book book) {
		appendTag(
			buffer, "entry", false,
			"xmlns:dc", XMLNamespaces.DublinCore,
			"xmlns:calibre", XMLNamespaces.CalibreMetadata
		);

		appendTagWithContent(buffer, "id", book.getId());
		appendTagWithContent(buffer, "title", book.getTitle());
		appendTagWithContent(buffer, "dc:language", book.getLanguage());
		appendTagWithContent(buffer, "dc:encoding", book.getEncodingNoDetection());

		for (UID uid : book.uids()) {
			appendTag(
				buffer, "dc:identifier", false,
				"scheme", uid.Type
			);
			buffer.append(uid.Id);
			closeTag(buffer, "dc:identifier");
		}

		for (Author author : book.authors()) {
			appendTag(buffer, "author", false);
			appendTagWithContent(buffer, "uri", author.SortKey);
			appendTagWithContent(buffer, "name", author.DisplayName);
			closeTag(buffer, "author");
		}

		for (Tag tag : book.tags()) {
			appendTag(
				buffer, "category", true,
				"term", tag.toString("/"),
				"label", tag.Name
			);
		}

		for (String label : book.labels()) {
			appendTag(
				buffer, "label", true,
				"name", label
			);
		}

		final SeriesInfo seriesInfo = book.getSeriesInfo();
		if (seriesInfo != null) {
			appendTagWithContent(buffer, "calibre:series", seriesInfo.Series.getTitle());
			if (seriesInfo.Index != null) {
				appendTagWithContent(buffer, "calibre:series_index", seriesInfo.Index.toPlainString());
			}
		}

		if (book.HasBookmark) {
			appendTag(buffer, "has-bookmark", true);
		}

		// TODO: serialize description (?)
		// TODO: serialize cover (?)

		appendTag(
			buffer, "link", true,
			"href", book.File.getUrl(),
			// TODO: real book mimetype
			"type", "application/epub+zip",
			"rel", "http://opds-spec.org/acquisition"
		);

		final RationalNumber progress = book.getProgress();
		if (progress != null) {
			appendTag(
				buffer, "progress", true,
				"numerator", Long.toString(progress.Numerator),
				"denominator", Long.toString(progress.Denominator)
			);
		}

		closeTag(buffer, "entry");
	}

	@Override
	public Book deserializeBook(String xml) {
		try {
			final BookDeserializer deserializer = new BookDeserializer();
			Xml.parse(xml, deserializer);
			return deserializer.getBook();
		} catch (SAXException e) {
			System.err.println(xml);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String serialize(Bookmark bookmark) {
		final StringBuilder buffer = new StringBuilder();
		appendTag(
			buffer, "bookmark", false,
			"id", String.valueOf(bookmark.getId()),
			"visible", String.valueOf(bookmark.IsVisible)
		);
		appendTag(
			buffer, "book", true,
			"id", String.valueOf(bookmark.getBookId()),
			"title", bookmark.getBookTitle()
		);
		appendTagWithContent(buffer, "text", bookmark.getText());
		appendTag(
			buffer, "history", true,
			"date-creation", formatDate(bookmark.getDate(Bookmark.DateType.Creation)),
			"date-modification", formatDate(bookmark.getDate(Bookmark.DateType.Modification)),
			"date-access", formatDate(bookmark.getDate(Bookmark.DateType.Access)),
			"access-count", String.valueOf(bookmark.getAccessCount())
		);
		appendTag(
			buffer, "start", true,
			"model", bookmark.ModelId,
			"paragraph", String.valueOf(bookmark.getParagraphIndex()),
			"element", String.valueOf(bookmark.getElementIndex()),
			"char", String.valueOf(bookmark.getCharIndex())
		);
		final ZLTextPosition end = bookmark.getEnd();
		if (end != null) {
			appendTag(
				buffer, "end", true,
				"paragraph", String.valueOf(end.getParagraphIndex()),
				"element", String.valueOf(end.getElementIndex()),
				"char", String.valueOf(end.getCharIndex())
			);
		} else {
			appendTag(
				buffer, "end", true,
				"length", String.valueOf(bookmark.getLength())
			);
		}
		appendTag(
			buffer, "style", true,
			"id", String.valueOf(bookmark.getStyleId())
		);
		closeTag(buffer, "bookmark");
		return buffer.toString();
	}

	@Override
	public Bookmark deserializeBookmark(String xml) {
		try {
			final BookmarkDeserializer deserializer = new BookmarkDeserializer();
			Xml.parse(xml, deserializer);
			return deserializer.getBookmark();
		} catch (SAXException e) {
			System.err.println(xml);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String serialize(HighlightingStyle style) {
		final StringBuilder buffer = new StringBuilder();
		final ZLColor bgColor = style.getBackgroundColor();
		final ZLColor fgColor = style.getForegroundColor();
		appendTag(buffer, "style", true,
			"id", String.valueOf(style.Id),
			"name", style.getName(),
			"bg-color", bgColor != null ? String.valueOf(bgColor.intValue()) : "-1",
			"fg-color", fgColor != null ? String.valueOf(fgColor.intValue()) : "-1"
		);
		return buffer.toString();
	}

	@Override
	public HighlightingStyle deserializeStyle(String xml) {
		try {
			final StyleDeserializer deserializer = new StyleDeserializer();
			Xml.parse(xml, deserializer);
			return deserializer.getStyle();
		} catch (SAXException e) {
			System.err.println(xml);
			e.printStackTrace();
			return null;
		}
	}

	private static DateFormat ourDateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, Locale.ENGLISH);
	private static String formatDate(Date date) {
		return date != null ? ourDateFormatter.format(date) : null;
	}
	private static Date parseDate(String str) throws SAXException {
		try {
			return str != null ? ourDateFormatter.parse(str) : null;
		} catch (Exception e) {
			throw new SAXException("XML parsing error", e);
		}
	}
	private static Date parseDateSafe(String str) throws SAXException {
		try {
			return str != null ? ourDateFormatter.parse(str) : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static int parseInt(String str) throws SAXException {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new SAXException("XML parsing error", e);
		}
	}
	private static int parseIntSafe(String str, int defaultValue) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	private static long parseLong(String str) throws SAXException {
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			throw new SAXException("XML parsing error", e);
		}
	}
	private static long parseLongSafe(String str, long defaultValue) {
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	private static boolean parseBoolean(String str) throws SAXException {
		try {
			return Boolean.parseBoolean(str);
		} catch (Exception e) {
			throw new SAXException("XML parsing error", e);
		}
	}

	private static void appendTag(StringBuilder buffer, String tag, boolean close, String ... attrs) {
		buffer.append('<').append(tag);
		for (int i = 0; i < attrs.length - 1; i += 2) {
			if (attrs[i + 1] != null) {
				buffer.append(' ')
					.append(escapeForXml(attrs[i])).append("=\"")
					.append(escapeForXml(attrs[i + 1])).append('"');
			}
		}
		if (close) {
			buffer.append('/');
		}
		buffer.append(">\n");
	}

	private static void closeTag(StringBuilder buffer, String tag) {
		buffer.append("</").append(tag).append(">");
	}

	private static void appendTagWithContent(StringBuilder buffer, String tag, String content) {
		if (content != null) {
			buffer
				.append('<').append(tag).append('>')
				.append(escapeForXml(content))
				.append("</").append(tag).append(">\n");
		}
	}

	private static void appendTagWithContent(StringBuilder buffer, String tag, Object content) {
		if (content != null) {
			appendTagWithContent(buffer, tag, String.valueOf(content));
		}
	}

	private static String escapeForXml(String data) {
		if (data.indexOf('&') != -1) {
			data = data.replaceAll("&", "&amp;");
		}
		if (data.indexOf('<') != -1) {
			data = data.replaceAll("<", "&lt;");
		}
		if (data.indexOf('>') != -1) {
			data = data.replaceAll(">", "&gt;");
		}
		if (data.indexOf('\'') != -1) {
			data = data.replaceAll("'", "&apos;");
		}
		if (data.indexOf('"') != -1) {
			data = data.replaceAll("\"", "&quot;");
		}
		return data;
	}

	private static void clear(StringBuilder buffer) {
		buffer.delete(0, buffer.length());
	}

	private static String string(StringBuilder buffer) {
		return buffer.length() != 0 ? buffer.toString() : null;
	}

	private static final class BookDeserializer extends DefaultHandler {
		private static enum State {
			READ_NOTHING,
			READ_ENTRY,
			READ_ID,
			READ_UID,
			READ_TITLE,
			READ_LANGUAGE,
			READ_ENCODING,
			READ_AUTHOR,
			READ_AUTHOR_URI,
			READ_AUTHOR_NAME,
			READ_SERIES_TITLE,
			READ_SERIES_INDEX,
		}

		private State myState = State.READ_NOTHING;

		private long myId = -1;
		private String myUrl;
		private final StringBuilder myTitle = new StringBuilder();
		private final StringBuilder myLanguage = new StringBuilder();
		private final StringBuilder myEncoding = new StringBuilder();
		private String myScheme;
		private final StringBuilder myUid = new StringBuilder();
		private final ArrayList<UID> myUidList = new ArrayList<UID>();
		private final ArrayList<Author> myAuthors = new ArrayList<Author>();
		private final ArrayList<Tag> myTags = new ArrayList<Tag>();
		private final ArrayList<String> myLabels = new ArrayList<String>();
		private final StringBuilder myAuthorSortKey = new StringBuilder();
		private final StringBuilder myAuthorName = new StringBuilder();
		private final StringBuilder mySeriesTitle = new StringBuilder();
		private final StringBuilder mySeriesIndex = new StringBuilder();
		private boolean myHasBookmark;
		private RationalNumber myProgress;

		private Book myBook;

		public Book getBook() {
			return myState == State.READ_NOTHING ? myBook : null;
		}

		@Override
		public void startDocument() {
			myBook = null;

			myId = -1;
			myUrl = null;
			clear(myTitle);
			clear(myLanguage);
			clear(myEncoding);
			clear(mySeriesTitle);
			clear(mySeriesIndex);
			clear(myUid);
			myUidList.clear();
			myAuthors.clear();
			myTags.clear();
			myLabels.clear();
			myHasBookmark = false;
			myProgress = null;

			myState = State.READ_NOTHING;
		}

		@Override
		public void endDocument() {
			if (myId == -1) {
				return;
			}
			myBook = new Book(
				myId,
				ZLFile.createFileByUrl(myUrl),
				string(myTitle),
				string(myEncoding),
				string(myLanguage)
			);
			for (Author author : myAuthors) {
				myBook.addAuthorWithNoCheck(author);
			}
			for (Tag tag : myTags) {
				myBook.addTagWithNoCheck(tag);
			}
			for (String label : myLabels) {
				myBook.addLabelWithNoCheck(label);
			}
			for (UID uid : myUidList) {
				myBook.addUidWithNoCheck(uid);
			}
			myBook.setSeriesInfoWithNoCheck(string(mySeriesTitle), string(mySeriesIndex));
			myBook.setProgressWithNoCheck(myProgress);
			myBook.HasBookmark = myHasBookmark;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			switch (myState) {
				case READ_NOTHING:
					if (!"entry".equals(localName)) {
						throw new SAXException("Unexpected tag " + localName);
					}
					myState = State.READ_ENTRY;
					break;
				case READ_ENTRY:
					if ("id".equals(localName)) {
						myState = State.READ_ID;
					} else if ("title".equals(localName)) {
						myState = State.READ_TITLE;
					} else if ("identifier".equals(localName) && XMLNamespaces.DublinCore.equals(uri)) {
						myState = State.READ_UID;
						myScheme = attributes.getValue("scheme");
					} else if ("language".equals(localName) && XMLNamespaces.DublinCore.equals(uri)) {
						myState = State.READ_LANGUAGE;
					} else if ("encoding".equals(localName) && XMLNamespaces.DublinCore.equals(uri)) {
						myState = State.READ_ENCODING;
					} else if ("author".equals(localName)) {
						myState = State.READ_AUTHOR;
						clear(myAuthorName);
						clear(myAuthorSortKey);
					} else if ("category".equals(localName)) {
						final String term = attributes.getValue("term");
						if (term != null) {
							myTags.add(Tag.getTag(term.split("/")));
						}
					} else if ("label".equals(localName)) {
						final String name = attributes.getValue("name");
						if (name != null) {
							myLabels.add(name);
						}
					} else if ("series".equals(localName) && XMLNamespaces.CalibreMetadata.equals(uri)) {
						myState = State.READ_SERIES_TITLE;
					} else if ("series_index".equals(localName) && XMLNamespaces.CalibreMetadata.equals(uri)) {
						myState = State.READ_SERIES_INDEX;
					} else if ("has-bookmark".equals(localName)) {
						myHasBookmark = true;
					} else if ("link".equals(localName)) {
						// TODO: use "rel" attribute
						myUrl = attributes.getValue("href");
					} else if ("progress".equals(localName)) {
						myProgress = RationalNumber.create(
							parseLong(attributes.getValue("numerator")),
							parseLong(attributes.getValue("denominator"))
						);
					} else {
						throw new SAXException("Unexpected tag " + localName);
					}
					break;
				case READ_AUTHOR:
					if ("uri".equals(localName)) {
						myState = State.READ_AUTHOR_URI;
					} else if ("name".equals(localName)) {
						myState = State.READ_AUTHOR_NAME;
					} else {
						throw new SAXException("Unexpected tag " + localName);
					}
					break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (myState) {
				case READ_NOTHING:
					throw new SAXException("Unexpected closing tag " + localName);
				case READ_ENTRY:
					if ("entry".equals(localName)) {
						myState = State.READ_NOTHING;
					}
					break;
				case READ_AUTHOR_URI:
				case READ_AUTHOR_NAME:
					myState = State.READ_AUTHOR;
					break;
				case READ_AUTHOR:
					if (myAuthorSortKey.length() > 0 && myAuthorName.length() > 0) {
						myAuthors.add(
							new Author(myAuthorName.toString(), myAuthorSortKey.toString())
						);
					}
					myState = State.READ_ENTRY;
					break;
				case READ_UID:
					myUidList.add(new UID(myScheme, myUid.toString()));
					clear(myUid);
					myState = State.READ_ENTRY;
					break;
				default:
					myState = State.READ_ENTRY;
					break;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			switch (myState) {
				case READ_ID:
					myId = parseLongSafe(new String(ch, start, length), -1);
					break;
				case READ_TITLE:
					myTitle.append(ch, start, length);
					break;
				case READ_UID:
					myUid.append(ch, start, length);
					break;
				case READ_LANGUAGE:
					myLanguage.append(ch, start, length);
					break;
				case READ_ENCODING:
					myEncoding.append(ch, start, length);
					break;
				case READ_AUTHOR_URI:
					myAuthorSortKey.append(ch, start, length);
					break;
				case READ_AUTHOR_NAME:
					myAuthorName.append(ch, start, length);
					break;
				case READ_SERIES_TITLE:
					mySeriesTitle.append(ch, start, length);
					break;
				case READ_SERIES_INDEX:
					mySeriesIndex.append(ch, start, length);
					break;
			}
		}
	}

	private static final class BookQueryDeserializer extends DefaultHandler {
		private static enum State {
			READ_QUERY,
			READ_FILTER_NOT,
			READ_FILTER_AND,
			READ_FILTER_OR,
			READ_FILTER_SIMPLE
		}

		private LinkedList<State> myStateStack = new LinkedList<State>();
		private LinkedList<Filter> myFilterStack = new LinkedList<Filter>();
		private Filter myFilter;
		private int myLimit = -1;
		private int myPage = -1;
		private BookQuery myQuery;

		public BookQuery getQuery() {
			return myQuery;
		}

		@Override
		public void startDocument() {
			myStateStack.clear();
		}

		@Override
		public void endDocument() {
			if (myFilter != null && myLimit > 0 && myPage >= 0) {
				myQuery = new BookQuery(myFilter, myLimit, myPage);
			}
		}

		private void setFilterToStack() {
			if (!myFilterStack.isEmpty() && myFilterStack.getLast() == null) {
				myFilterStack.set(myFilterStack.size() - 1, myFilter);
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (myStateStack.isEmpty()) {
				if ("query".equals(localName)) {
					myLimit = parseInt(attributes.getValue("limit"));
					myPage = parseInt(attributes.getValue("page"));
					myStateStack.add(State.READ_QUERY);
				} else {
					throw new SAXException("Unexpected tag " + localName);
				}
			} else {
				if ("filter".equals(localName)) {
					final String type = attributes.getValue("type");
					if ("empty".equals(type)) {
						myFilter = new Filter.Empty();
					} else if ("author".equals(type)) {
						myFilter = new Filter.ByAuthor(new Author(
							attributes.getValue("displayName"),
							attributes.getValue("sorkKey")
						));
					} else if ("tag".equals(type)) {
						final LinkedList<String> names = new LinkedList<String>();
						int num = 0;
						String n;
						while ((n = attributes.getValue("name" + num++)) != null) {
							names.add(n);
						}
						myFilter = new Filter.ByTag(Tag.getTag(names.toArray(new String[names.size()])));
					} else if ("label".equals(type)) {
						myFilter = new Filter.ByLabel(attributes.getValue("name"));
					} else if ("series".equals(type)) {
						myFilter = new Filter.BySeries(new Series(
							attributes.getValue("title")
						));
					} else if ("pattern".equals(type)) {
						myFilter = new Filter.ByPattern(attributes.getValue("pattern"));
					} else if ("title-prefix".equals(type)) {
						myFilter = new Filter.ByTitlePrefix(attributes.getValue("prefix"));
					} else if ("has-bookmark".equals(type)) {
						myFilter = new Filter.HasBookmark();
					} else if ("has-physical-file".equals(type)) {
						myFilter = new Filter.HasPhysicalFile();
					} else {
						// we create empty filter for all other types
						// to keep a door to add new filters in a future
						myFilter = new Filter.Empty();
					}
					myStateStack.add(State.READ_FILTER_SIMPLE);
				} else if ("not".equals(localName)) {
					myFilterStack.add(null);
					myStateStack.add(State.READ_FILTER_NOT);
				} else if ("and".equals(localName)) {
					myFilterStack.add(null);
					myStateStack.add(State.READ_FILTER_AND);
				} else if ("or".equals(localName)) {
					myFilterStack.add(null);
					myStateStack.add(State.READ_FILTER_OR);
				} else {
					throw new SAXException("Unexpected tag " + localName);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (myStateStack.isEmpty()) {
				// should be never thrown
				throw new SAXException("Unexpected end of tag " + localName);
			}
			switch (myStateStack.removeLast()) {
				case READ_QUERY:
					break;
				case READ_FILTER_NOT:
					myFilter = new Filter.Not(myFilterStack.removeLast());
					break;
				case READ_FILTER_AND:
					myFilter = new Filter.And(myFilterStack.removeLast(), myFilter);
					break;
				case READ_FILTER_OR:
					myFilter = new Filter.Or(myFilterStack.removeLast(), myFilter);
					break;
				case READ_FILTER_SIMPLE:
					break;
			}
			setFilterToStack();
		}
	}

	private static final class BookmarkQueryDeserializer extends DefaultHandler {
		private boolean myVisible;
		private int myLimit;
		private int myPage;
		private final BookDeserializer myBookDeserializer = new BookDeserializer();
		private BookmarkQuery myQuery;

		BookmarkQuery getQuery() {
			return myQuery;
		}

		@Override
		public void startDocument() {
			myQuery = null;
			myBookDeserializer.startDocument();
		}

		@Override
		public void endDocument() {
			myBookDeserializer.endDocument();
			myQuery = new BookmarkQuery(myBookDeserializer.getBook(), myVisible, myLimit, myPage);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("query".equals(localName)) {
				myVisible = parseBoolean(attributes.getValue("visible"));
				myLimit = parseInt(attributes.getValue("limit"));
				myPage = parseInt(attributes.getValue("page"));
			} else {
				myBookDeserializer.startElement(uri, localName, qName, attributes);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (!"query".equals(localName)) {
				myBookDeserializer.endElement(uri, localName, qName);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			myBookDeserializer.characters(ch, start, length);
		}
	}

	private static final class BookmarkDeserializer extends DefaultHandler {
		private static enum State {
			READ_NOTHING,
			READ_BOOKMARK,
			READ_TEXT
		}

		private State myState = State.READ_NOTHING;
		private Bookmark myBookmark;

		private long myId = -1;
		private long myBookId;
		private String myBookTitle;
		private final StringBuilder myText = new StringBuilder();
		private Date myCreationDate;
		private Date myModificationDate;
		private Date myAccessDate;
		private int myAccessCount;
		private String myModelId;
		private int myStartParagraphIndex;
		private int myStartElementIndex;
		private int myStartCharIndex;
		private int myEndParagraphIndex;
		private int myEndElementIndex;
		private int myEndCharIndex;
		private boolean myIsVisible;
		private int myStyle;

		public Bookmark getBookmark() {
			return myState == State.READ_NOTHING ? myBookmark : null;
		}

		@Override
		public void startDocument() {
			myBookmark = null;

			myId = -1;
			myBookId = -1;
			myBookTitle = null;
			clear(myText);
			myCreationDate = null;
			myModificationDate = null;
			myAccessDate = null;
			myAccessCount = 0;
			myModelId = null;
			myStartParagraphIndex = 0;
			myStartElementIndex = 0;
			myStartCharIndex = 0;
			myEndParagraphIndex = -1;
			myEndElementIndex = -1;
			myEndCharIndex = -1;
			myIsVisible = false;
			myStyle = 1;

			myState = State.READ_NOTHING;
		}

		@Override
		public void endDocument() {
			if (myBookId == -1) {
				return;
			}
			myBookmark = new Bookmark(
				myId, myBookId, myBookTitle, myText.toString(),
				myCreationDate, myModificationDate, myAccessDate, myAccessCount,
				myModelId,
				myStartParagraphIndex, myStartElementIndex, myStartCharIndex,
				myEndParagraphIndex, myEndElementIndex, myEndCharIndex,
				myIsVisible,
				myStyle
			);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			switch (myState) {
				case READ_NOTHING:
					if (!"bookmark".equals(localName)) {
						throw new SAXException("Unexpected tag " + localName);
					}
					myId = parseLong(attributes.getValue("id"));
					myIsVisible = parseBoolean(attributes.getValue("visible"));
					myState = State.READ_BOOKMARK;
					break;
				case READ_BOOKMARK:
					if ("book".equals(localName)) {
						myBookId = parseLong(attributes.getValue("id"));
						myBookTitle = attributes.getValue("title");
					} else if ("text".equals(localName)) {
						myState = State.READ_TEXT;
					} else if ("history".equals(localName)) {
						myCreationDate = parseDate(attributes.getValue("date-creation"));
						myModificationDate = parseDateSafe(attributes.getValue("date-modification"));
						myAccessDate = parseDateSafe(attributes.getValue("date-access"));
						myAccessCount = parseIntSafe(attributes.getValue("access-count"), 0);
					} else if ("start".equals(localName)) {
						myModelId = attributes.getValue("model");
						myStartParagraphIndex = parseInt(attributes.getValue("paragraph"));
						myStartElementIndex = parseInt(attributes.getValue("element"));
						myStartCharIndex = parseInt(attributes.getValue("char"));
					} else if ("end".equals(localName)) {
						final String para = attributes.getValue("paragraph");
						if (para != null) {
							myEndParagraphIndex = parseInt(para);
							myEndElementIndex = parseInt(attributes.getValue("element"));
							myEndCharIndex = parseInt(attributes.getValue("char"));
						} else {
							myEndParagraphIndex = parseInt(attributes.getValue("length"));
							myEndElementIndex = -1;
							myEndCharIndex = -1;
						}
					} else if ("style".equals(localName)) {
						myStyle = parseInt(attributes.getValue("id"));
					} else {
						throw new SAXException("Unexpected tag " + localName);
					}
					break;
				case READ_TEXT:
					throw new SAXException("Unexpected tag " + localName);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (myState) {
				case READ_NOTHING:
					throw new SAXException("Unexpected closing tag " + localName);
				case READ_BOOKMARK:
					if ("bookmark".equals(localName)) {
						myState = State.READ_NOTHING;
					}
					break;
				case READ_TEXT:
					myState = State.READ_BOOKMARK;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if (myState == State.READ_TEXT) {
				myText.append(ch, start, length);
			}
		}
	}

	private static final class StyleDeserializer extends DefaultHandler {
		private HighlightingStyle myStyle;

		public HighlightingStyle getStyle() {
			return myStyle;
		}

		@Override
		public void startDocument() {
			myStyle = null;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("style".equals(localName)) {
				final int id = parseIntSafe(attributes.getValue("id"), -1);
				if (id != -1) {
					final int bg = parseIntSafe(attributes.getValue("bg-color"), -1);
					final int fg = parseIntSafe(attributes.getValue("fg-color"), -1);
					myStyle = new HighlightingStyle(
						id, attributes.getValue("name"),
						bg != -1 ? new ZLColor(bg) : null,
						fg != -1 ? new ZLColor(fg) : null
					);
				}
			}
		}
	}
}
