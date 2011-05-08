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

package org.geometerplus.fbreader.network.atom;

import android.text.Html;

import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;
import org.geometerplus.fbreader.network.atom.ATOMConstants;

public class HtmlToString {
	private String myTextType;
	private StringBuilder myTextContent = new StringBuilder();

	public void setupTextContent(String type) {
		if (type == null) {
			myTextType = ATOMConstants.TYPE_DEFAULT;
		} else {
			myTextType = type;
		}
		myTextContent.delete(0, myTextContent.length());
	}

	public void appendText(String text) {
		if (text != null) {
			myTextContent.append(text);
		}
	}

	public String getText() {
		char[] contentArray = myTextContent.toString().trim().toCharArray();
		String result;
		if (contentArray.length == 0) {
			result = null;
		} else {
			result = new String(contentArray);
		}
		if (result != null) {
			if (ATOMConstants.TYPE_HTML.equals(myTextType) ||
				ATOMConstants.TYPE_XHTML.equals(myTextType) ||
				MimeType.TEXT_HTML.Name.equals(myTextType) ||
				MimeType.TEXT_XHTML.Name.equals(myTextType)) {
				result = Html.fromHtml(new String(contentArray)).toString();
			}
		}
		myTextType = null;
		myTextContent.delete(0, myTextContent.length());
		return result;
	}

	public void appendStartTag(String tag, ZLStringMap attributes) {
		myTextContent.append("<").append(tag);
		for (int i = 0; i < attributes.getSize(); ++i) {
			final String key = attributes.getKey(i);
			final String value = attributes.getValue(key);
			myTextContent.append(" ").append(key).append("=\"");
			if (value != null) {
				myTextContent.append(value);
			}
			myTextContent.append("\"");
		}
		myTextContent.append(">");
	}

	public void appendEndTag(String tag) {
		myTextContent.append("</").append(tag).append(">");
	}
}
