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

package org.geometerplus.fbreader.network.opds;

import java.util.HashMap;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.geometerplus.zlibrary.core.html.*;

import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;

import org.geometerplus.fbreader.network.atom.ATOMConstants;


public class HtmlToString {

	private String myLastOpenedTag;
	private String myTextType;
	private StringBuilder myTextContent = new StringBuilder();

	private HtmlToStringReader myHtmlToStringReader = new HtmlToStringReader();

	public void setupTextContent(String type) {
		if (type == null) {
			myTextType = ATOMConstants.TYPE_DEFAULT;
		} else {
			myTextType = type;
		}
		myTextContent.delete(0, myTextContent.length());
	}

	public String finishTextContent(String bufferContent) {
		if (bufferContent != null) {
			myTextContent.append(bufferContent);
		}
		char[] contentArray = myTextContent.toString().trim().toCharArray();
		String result;
		if (contentArray.length == 0) {
			result = null;
		} else {
			result = new String(contentArray);
		}
		if (result != null) {
			if (myTextType == ATOMConstants.TYPE_HTML || myTextType == ATOMConstants.TYPE_XHTML
					|| myTextType == "text/html" || myTextType == "text/xhtml") {
				myHtmlToStringReader.readFromString(result);
				result = myHtmlToStringReader.getString();
			}
		}
		myTextType = null;
		myTextContent.delete(0, myTextContent.length());
		return result;
	}

	public void processTextContent(boolean closeTag, String tag, ZLStringMap attributes, String bufferContent) {
		if (myTextType == ATOMConstants.TYPE_XHTML || myTextType == "text/xhtml") {
			if (bufferContent != null) {
				myTextContent.append(bufferContent);
			}
			if (closeTag) {
				final int index = myTextContent.length() - 1;
				if (tag == myLastOpenedTag && bufferContent == null && myTextContent.charAt(index) == '>') {
					myTextContent.insert(index, '/'); // TODO: Is it necessary in HTML???????
				} else {
					myTextContent.append("</").append(tag).append(">");
				}
				myLastOpenedTag = null;
			} else {
				myLastOpenedTag = tag;
				StringBuilder buffer = new StringBuilder("<").append(tag);
				for (int i = 0; i < attributes.getSize(); ++i) {
					final String key = attributes.getKey(i);
					final String value = attributes.getValue(key);
					buffer.append(" ").append(key).append("=\"");
					if (value != null) {
						buffer.append(value);
					}
					buffer.append("\"");
				}
				buffer.append(" >");
				myTextContent.append(buffer.toString());
			}
		} else {
			if (bufferContent != null) {
				myTextContent.append(bufferContent);
			}
		}
	}

	private static class HtmlToStringReader implements ZLHtmlReader {

		private StringBuilder myBuffer = new StringBuilder();
		private byte[] myByteData;
		private int myByteDataLength;
		private HashMap<String,char[]> myEntityMap;

		public void readFromString(String htmlString) {
			final StringBuilder html = new StringBuilder();
			html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")
				.append("<html><head>")
				.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />")
				.append("<title></title>")
				.append("</head><body>")
				.append(htmlString)
				.append("</body></html>");
			final byte[] bytes;
			try {
				bytes = html.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException("It's impossible!!! UTF-8 charset is not supported!!!", ex);
			}
			ZLHtmlProcessor.read(this, new ByteArrayInputStream(bytes));
		}

		public String getString() {
			return new String(myBuffer.toString().trim().toCharArray());
		}


		public void startDocumentHandler() {
			myBuffer.delete(0, myBuffer.length());
			myByteDataLength = 0;
		}

		public void endDocumentHandler() {
			processByteData();
		}

		public void startElementHandler(String tag, int offset, ZLHtmlAttributeMap attributes) {
			processByteData();
			tag = tag.toLowerCase().intern();
			if (tag == "br") {
				if (myBuffer.length() > 0) {
					myBuffer.append('\n');
				}
			} else if (tag == "hr") {
				if (myBuffer.length() > 0) {
					if (myBuffer.charAt(myBuffer.length() - 1) != '\n') {
						myBuffer.append('\n');
					}
					myBuffer.append('\n');
				}
			}
		}

		public void endElementHandler(String tag) {
			processByteData();
			tag = tag.toLowerCase().intern();
			if (tag == "p") {
				if (myBuffer.length() > 0) {
					myBuffer.append('\n');
				}
			}
		}

		private void processByteData() {
			if (myByteDataLength == 0) {
				return;
			}
			final String data;
			try {
				data = new String(myByteData, 0, myByteDataLength, "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException("It's impossible!!! UTF-8 charset is not supported!!!", ex);
			}
			myByteDataLength = 0;
			if (data.length() == 0) {
				return;
			}
			if (myBuffer.length() > 0 && !Character.isWhitespace(myBuffer.charAt(myBuffer.length() - 1))) {
				myBuffer.append(' ');
			}
			int index = 0;
			while (index < data.length() && Character.isWhitespace(data.charAt(index))) {
				++index;
			}
			boolean lastSpace = false;
			while (index < data.length()) {
				final char ch = data.charAt(index++);
				if (Character.isWhitespace(ch)) {
					lastSpace = true;
				} else {
					if (lastSpace) {
						myBuffer.append(' ');
						lastSpace = false;
					}
					myBuffer.append(ch);
				}
			}
		}

		public void entityDataHandler(String entity) {
			processByteData();

			if (entity.length() == 0) {
				return;
			}

			if (myEntityMap == null) {
				myEntityMap = new HashMap<String,char[]>(ZLXMLProcessor.getEntityMap(XHTMLReader.xhtmlDTDs()));
			}
			char[] data = myEntityMap.get(entity);
			if (data == null) {
				if (entity.charAt(0) == '#') {
					try {
						int number;
						if (entity.charAt(1) == 'x') {
							number = Integer.parseInt(entity.substring(2), 16);
						} else {
							number = Integer.parseInt(entity.substring(1));
						}
						data = new char[] { (char)number };
					} catch (NumberFormatException e) {
					}
				}
				if (data == null) {
					data = new char[0];
				}
				myEntityMap.put(entity, data);
			}
//System.err.println("FBREADER -- ENTITY: &" + entity + "; --> " + new String(data));
			myBuffer.append(data);
		}

		public void byteDataHandler(byte[] data, int start, int length) {
			if (length <= 0) {
				return;
			}
			if (myByteData == null) {
				myByteData = new byte[length];
				System.arraycopy(data, start, myByteData, 0, length);
				myByteDataLength = length;
			} else {
				if (myByteData.length < myByteDataLength + length) {
					final byte[] oldData = myByteData;
					myByteData = new byte[myByteDataLength + length];
					System.arraycopy(oldData, 0, myByteData, 0, myByteDataLength);
				}
				System.arraycopy(data, start, myByteData, myByteDataLength, length);
				myByteDataLength += length;
			}
		}
	}
}
