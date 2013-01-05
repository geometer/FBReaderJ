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

	private static void appendTagWithContent(StringBuilder buffer, String tag, String content) {
		buffer
			.append('<').append(tag).append('>')
			.append(content)
			.append("</").append(tag).append(">\n");
	}

	public static String serialize(Book book) {
		// TODO: implement
		final StringBuilder buffer = new StringBuilder();
		buffer.append("<entry>\n");
		/*
		// TODO: write book id
		appendTagWithContent(buffer, "id", id);
		*/

		appendTagWithContent(buffer, "title", book.getTitle());

		// TODO: write language (if defined)
		//appendTagWithContent(buffer, "dc:language", "2-letter-code");

		for (Author author : book.authors()) {
			buffer.append("<author>\n");
			appendTagWithContent(buffer, "uri", author.SortKey);
			appendTagWithContent(buffer, "name", author.DisplayName);
			buffer.append("</author>\n");
		}

		/*
		const std::vector<shared_ptr<Tag> > &tags = book.tags();
		for (std::vector<shared_ptr<Tag> >::const_iterator it = tags.begin(); it != tags.end(); ++it) {
			const std::string category = (*it)->fullName();
			writer.addTag("category", true);
			writer.addAttribute("term", category);
			writer.addAttribute("label", category);
		}
		*/

		buffer
			.append("<link")
			.append(" href='").append(book.File.getUrl()).append("'")
			// TODO: real book mimetype
			.append(" type='").append("application/epub+zip").append("'")
			.append(" rel='").append("http://opds-spec.org/acquisition").append("'")
			.append("/>\n");

		buffer.append("</entry>\n");
		return buffer.toString();
	}

	public static Book deserialize(String xml) {
		// TODO: implement
		return null;
	}
}
