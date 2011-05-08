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
	private static enum Type {
		Text,
		Html,
		XHtml
	};

	private Type myType;
	private StringBuilder myBuffer = new StringBuilder();

	public void setupTextContent(String type) {
		if (ATOMConstants.TYPE_HTML.equals(type) || MimeType.TEXT_HTML.Name.equals(type)) {
			myType = Type.Html;
		} else if (ATOMConstants.TYPE_XHTML.equals(type) || MimeType.TEXT_XHTML.Name.equals(type)) {
			myType = Type.XHtml;
		} else {
			myType = Type.Text;
		}
		myBuffer.delete(0, myBuffer.length());
	}

	public void appendText(String text) {
		if (text != null) {
			myBuffer.append(text);
		}
	}

	public CharSequence getText() {
		final String text = myBuffer.toString();
		myBuffer.delete(0, myBuffer.length());

		switch (myType) {
			case Html:
			case XHtml:
				return Html.fromHtml(text);
			default:
				return text;
		}
	}

	public void appendStartTag(String tag, ZLStringMap attributes) {
		myBuffer.append("<").append(tag);
		for (int i = 0; i < attributes.getSize(); ++i) {
			final String key = attributes.getKey(i);
			final String value = attributes.getValue(key);
			myBuffer.append(" ").append(key).append("=\"");
			if (value != null) {
				myBuffer.append(value);
			}
			myBuffer.append("\"");
		}
		myBuffer.append(">");
	}

	public void appendEndTag(String tag) {
		myBuffer.append("</").append(tag).append(">");
	}
}
