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

public abstract class BookSerializerUtil {
	private BookSerializerUtil() {
	}

	public static String serialize(Book book) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("<entry>\n");

		appendTagWithContent(buffer, "id", String.valueOf(book.getId()));
		appendTagWithContent(buffer, "title", book.getTitle());
		appendTagWithContent(buffer, "dc:language", book.getLanguage());

		for (Author author : book.authors()) {
			buffer.append("<author>\n");
			appendTagWithContent(buffer, "uri", author.SortKey);
			appendTagWithContent(buffer, "name", author.DisplayName);
			buffer.append("</author>\n");
		}

		for (Tag tag : book.tags()) {
			appendTagWithAttributes(
				buffer, "category",
				"term", tag.toString("/"),
				"label", tag.Name
			);
		}

		appendTagWithAttributes(
			buffer, "link",
			"href", book.File.getUrl(),
			// TODO: real book mimetype
			"type", "application/epub+zip",
			"rel", "http://opds-spec.org/acquisition"
		);

		buffer.append("</entry>\n");
		return buffer.toString();
	}

	public static Book deserialize(String xml) {
		// TODO: implement
		return null;
	}

	private static void appendTagWithContent(StringBuilder buffer, String tag, String content) {
		buffer
			.append('<').append(tag).append('>')
			.append(content)
			.append("</").append(tag).append(">\n");
	}

	private static void appendTagWithAttributes(StringBuilder buffer, String tag, String ... attrs) {
		buffer.append('<').append(tag);
		for (int i = 0; i < attrs.length - 1; i += 2) {
			buffer.append(' ').append(attrs[i]).append("=\"").append(attrs[i + 1]).append('"');
		}
		buffer.append("/>\n");
	}
}
