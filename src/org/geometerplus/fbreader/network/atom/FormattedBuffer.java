/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

import org.geometerplus.fbreader.network.HtmlUtil;

public class FormattedBuffer {
	public static enum Type {
		Text,
		Html,
		XHtml
	};

	private Type myType;
	private StringBuilder myBuffer = new StringBuilder();

	public FormattedBuffer(Type type) {
		myType = type;
	}

	public FormattedBuffer() {
		this(Type.Text);
	}

	public void appendText(CharSequence text) {
		if (text != null) {
			myBuffer.append(text);
		}
	}

	public void appendText(char[] data, int start, int length) {
		myBuffer.append(data, start, length);
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

	public void reset(Type type) {
		myType = type;
		reset();
	}

	public void reset() {
		myBuffer.delete(0, myBuffer.length());
	}

	public CharSequence getText() {
		final String text = myBuffer.toString();

		switch (myType) {
			case Html:
			case XHtml:
				return HtmlUtil.getHtmlText(text);
			default:
				return text;
		}
	}

	@Override
	public String toString() {
		return myBuffer.toString();
	}
}
