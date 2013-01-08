/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.library;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public abstract class BookSerializerUtil {
	private BookSerializerUtil() {
	}

	public static String serialize(Book book) {
		final StringBuilder buffer = new StringBuilder();
		appendTagWithAttributes(
			buffer, "entry", false,
			"xmlns:dc", XMLNamespaces.DublinCore,
			"xmlns:calibre", XMLNamespaces.CalibreMetadata
		);

		appendTagWithContent(buffer, "id", String.valueOf(book.getId()));
		appendTagWithContent(buffer, "title", book.getTitle());
		appendTagWithContent(buffer, "dc:language", book.getLanguage());
		appendTagWithContent(buffer, "dc:encoding", book.getEncodingNoDetection());

		for (Author author : book.authors()) {
			buffer.append("<author>\n");
			appendTagWithContent(buffer, "uri", author.SortKey);
			appendTagWithContent(buffer, "name", author.DisplayName);
			buffer.append("</author>\n");
		}

		for (Tag tag : book.tags()) {
			appendTagWithAttributes(
				buffer, "category", true,
				"term", tag.toString("/"),
				"label", tag.Name
			);
		}

		final SeriesInfo seriesInfo = book.getSeriesInfo();
		if (seriesInfo != null) {
			appendTagWithContent(buffer, "calibre:series", seriesInfo.Title);
			if (seriesInfo.Index != null) {
				appendTagWithContent(buffer, "calibre:series_index", String.valueOf(seriesInfo.Index));
			}
		}
		// TODO: serialize description (?)
		// TODO: serialize cover (?)

		appendTagWithAttributes(
			buffer, "link", true,
			"href", book.File.getUrl(),
			// TODO: real book mimetype
			"type", "application/epub+zip",
			"rel", "http://opds-spec.org/acquisition"
		);

		buffer.append("</entry>\n");
		return buffer.toString();
	}

	public static Book deserialize(String xml) {
		final Deserializer deserializer = new Deserializer();
		deserializer.readQuietly(xml);
		return deserializer.getBook();
	}

	private static void appendTagWithContent(StringBuilder buffer, String tag, String content) {
		if (content != null) {
			buffer
				.append('<').append(tag).append('>')
				.append(escapeForXml(content))
				.append("</").append(tag).append(">\n");
		}
	}

	private static void appendTagWithAttributes(StringBuilder buffer, String tag, boolean close, String ... attrs) {
		buffer.append('<').append(tag);
		for (int i = 0; i < attrs.length - 1; i += 2) {
			buffer.append(' ')
				.append(escapeForXml(attrs[i])).append("=\"")
				.append(escapeForXml(attrs[i + 1])).append('"');
		}
		if (close) {
			buffer.append('/');
		}
		buffer.append(">\n");
	}

	private static String escapeForXml(String data) {
		if (data.indexOf('&') != -1) {
			data = data.replaceAll("&", "&amp;");
		}
		if (data.indexOf('<') != -1) {
			data = data.replaceAll("&", "&lt;");
		}
		if (data.indexOf('>') != -1) {
			data = data.replaceAll("&", "&gt;");
		}
		if (data.indexOf('\'') != -1) {
			data = data.replaceAll("&", "&apos;");
		}
		if (data.indexOf('"') != -1) {
			data = data.replaceAll("&", "&quot;");
		}
		return data;
	}

	private static final class Deserializer extends ZLXMLReaderAdapter {
		private static enum State {
			READ_NOTHING,
			READ_ENTRY,
			READ_ID,
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
		private StringBuilder myTitle = new StringBuilder();
		private StringBuilder myLanguage = new StringBuilder();
		private StringBuilder myEncoding = new StringBuilder();
		private ArrayList<Author> myAuthors = new ArrayList<Author>();
		private ArrayList<Tag> myTags = new ArrayList<Tag>();
		private StringBuilder myAuthorSortKey = new StringBuilder();
		private StringBuilder myAuthorName = new StringBuilder();
		private StringBuilder mySeriesTitle = new StringBuilder();
		private StringBuilder mySeriesIndex = new StringBuilder();

		private Book myBook;

		public Book getBook() {
			return myState == State.READ_NOTHING ? myBook : null;
		}

		private static void clear(StringBuilder buffer) {
			buffer.delete(0, buffer.length());
		}

		private static String string(StringBuilder buffer) {
			return buffer.length() != 0 ? buffer.toString() : null;
		}

		@Override
		public void startDocumentHandler() {
			myBook = null;

			myId = -1;
			myUrl = null;
			clear(myTitle);
			clear(myLanguage);
			clear(myEncoding);
			clear(mySeriesTitle);
			clear(mySeriesIndex);
			myAuthors.clear();
			myTags.clear();

			myState = State.READ_NOTHING;
		}

		@Override
		public void endDocumentHandler() {
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
			myBook.setSeriesInfoWithNoCheck(string(mySeriesTitle), string(mySeriesIndex));
		}

		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			switch (myState) {
				case READ_NOTHING:
					if (!"entry".equals(tag)) {
						return true;
					}
					myState = State.READ_ENTRY;
					break;
				case READ_ENTRY:
					if ("id".equals(tag)) {
						myState = State.READ_ID;
					} else if ("title".equals(tag)) {
						myState = State.READ_TITLE;
					} else if ("dc:language".equals(tag)) {
						myState = State.READ_LANGUAGE;
					} else if ("dc:encoding".equals(tag)) {
						myState = State.READ_ENCODING;
					} else if ("author".equals(tag)) {
						myState = State.READ_AUTHOR;
						clear(myAuthorName);
						clear(myAuthorSortKey);
					} else if ("category".equals(tag)) {
						final String term = attributes.getValue("term");
						if (term != null) {
							myTags.add(Tag.getTag(term.split("/")));
						}
					} else if ("calibre:series".equals(tag)) {
						myState = State.READ_SERIES_TITLE;
					} else if ("calibre:series_index".equals(tag)) {
						myState = State.READ_SERIES_INDEX;
					} else if ("link".equals(tag)) {
						// TODO: use "rel" attribute
						myUrl = attributes.getValue("href");
					} else {
						return true;
					}
					break;
				case READ_AUTHOR:
					if ("uri".equals(tag)) {
						myState = State.READ_AUTHOR_URI;
					} else if ("name".equals(tag)) {
						myState = State.READ_AUTHOR_NAME;
					} else {
						return true;
					}
					break;
			}
			return false;
		}
		
		@Override
		public boolean endElementHandler(String tag) {
			switch (myState) {
				case READ_NOTHING:
					return true;
				case READ_ENTRY:
					if ("entry".equals(tag)) {
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
				default:
					myState = State.READ_ENTRY;
					break;
			}
			return false;
		}
		
		@Override
		public void characterDataHandler(char[] ch, int start, int length) {
			switch (myState) {
				case READ_ID:
					try {
						myId = Long.parseLong(new String(ch, start, length));
					} catch (NumberFormatException e) {
					}
					break;
				case READ_TITLE:
					myTitle.append(ch, start, length);
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
}
