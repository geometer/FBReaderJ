/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.html.own;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.html.ZLHtmlReader;

final class ZLOwnHtmlParser {
	private static final byte START_DOCUMENT = 0;
	private static final byte START_TAG = 1;
	private static final byte END_TAG = 2;
	private static final byte TEXT = 3;
	private static final byte IGNORABLE_WHITESPACE = 4;
	private static final byte PROCESSING_INSTRUCTION = 5;
	private static final byte COMMENT = 6;
	private static final byte LANGLE = 7;
	private static final byte WS_AFTER_START_TAG_NAME = 8;
	private static final byte WS_AFTER_END_TAG_NAME = 9;
	private static final byte WAIT_EQUALS = 10;
	private static final byte WAIT_ATTRIBUTE_VALUE = 11;
	private static final byte SLASH = 12;
	private static final byte ATTRIBUTE_NAME = 13;
	private static final byte S_ATTRIBUTE_VALUE = 14;
	private static final byte DEFAULT_ATTRIBUTE_VALUE = 15;
	private static final byte ENTITY_REF = 16;
	private static final byte COMMENT_MINUS = 17;
	private static final byte D_ATTRIBUTE_VALUE = 18;
	private static final byte SCRIPT = 19;
	
	private static String convertToString(HashMap strings, ZLMutableString contatiner) {
		String s = (String)strings.get(contatiner);
		if (s == null) {
			s = contatiner.toString();
			strings.put(new ZLMutableString(contatiner), s);
		}
		contatiner.clear();
		return s;
	}

	private final InputStreamReader myStreamReader;
	private final ZLHtmlReader myReader;

	private final char[] myBuffer = new char[8192];

	/*private boolean isCorrectHtmlIdentificatorSymbol(char s) {
		return !(
					(s != '?') && (s != ' ') && (s != '\t') && (s != '\n')
				 && (s != ' ') && (s != '\\') && (s != '/') && (s != '+')
				);
	}*/
	
	public ZLOwnHtmlParser(ZLHtmlReader htmlReader, InputStream stream) throws IOException {
		myReader = htmlReader;

		String encoding = "cp1251";
		int observeChars = 1024;
		stream.mark(observeChars);
		final char[] buffer = myBuffer;
		int len;
		for (len = 0; len < observeChars; ++len) {
			char c = (char)stream.read();
			buffer[len] = c;
		}
		//if (len < 384) {
			StringBuffer description = new StringBuffer(new String(buffer, 0, observeChars));
			int index = description.indexOf("encoding");
			if (index <= 0) {
				index = description.indexOf("charset");
			}
			if (index > 0) {
				int startIndex;
				for (startIndex = description.indexOf("=", index) + 1; 
					!Character.isLetter(description.charAt(startIndex));
					startIndex++) {
				}
				if (startIndex > 0) {
					//TODO это плохое определение кодировки
					int endIndex = Math.min(description.indexOf("\"", startIndex + 1),
									Math.min(description.indexOf(" ", startIndex + 1), 
									description.indexOf(">", startIndex + 1)));
					if (endIndex > 0) {
						encoding = description.substring(startIndex, endIndex);
					}
				}
			}
		//}

		stream.reset();
		InputStreamReader test;
		try {
			test = new InputStreamReader(stream, encoding);
		} catch (UnsupportedEncodingException e) {
			test = new InputStreamReader(stream, "cp1251");
		}
		myStreamReader = test;
	}

	private static char[] getEntityValue(HashMap entityMap, String name) {
		char[] value = (char[])entityMap.get(name);
		if (value == null) {
			if ((name.length() > 0) && (name.charAt(0) == '#')) {
				try {
					int number;
					if (name.charAt(1) == 'x') {
						number = Integer.parseInt(name.substring(2), 16);
					} else {
						number = Integer.parseInt(name.substring(1));
					}
					value = new char[] { (char)number };
					entityMap.put(name, value);
				} catch (NumberFormatException e) {
				}
			}
		}
		return value;
	}

	public void doIt() throws IOException {
		final HashMap entityMap = new HashMap();
		entityMap.put("amp", new char[] { '&' });
		entityMap.put("apos", new char[] { '\'' });
		entityMap.put("gt", new char[] { '>' });
		entityMap.put("lt", new char[] { '<' });
		entityMap.put("quot", new char[] { '\"' });
		entityMap.put("rarr", new char[] { '-', '>'});
		entityMap.put("larr", new char[] { '<', '-'});

		final InputStreamReader streamReader = myStreamReader;
		final ZLHtmlReader htmlReader = myReader;
		char[] buffer = myBuffer;
		final ZLMutableString tagName = new ZLMutableString();
		final ZLMutableString attributeName = new ZLMutableString();
		final ZLMutableString attributeValue = new ZLMutableString();
		final boolean dontCacheAttributeValues = htmlReader.dontCacheAttributeValues();
		final ZLMutableString entityName = new ZLMutableString();
		final HashMap strings = new HashMap();
		final ZLStringMap attributes = new ZLStringMap();
		boolean scriptOpened = false;
		boolean html = false;
		
		byte state = START_DOCUMENT;
		byte savedState = START_DOCUMENT;
		while (true) {
			int count = streamReader.read(buffer);
			if (count <= 0) {
				return;
			}
			if (count < buffer.length) {
				buffer = ZLArrayUtils.createCopy(buffer, count, count);
			}
			int startPosition = 0;
			try {
				for (int i = -1;;) {
					
mainSwitchLabel:	
	
					switch (state) {
						case START_DOCUMENT:
							while (true) {
								switch (buffer[++i]) {
									case '<':
										state = LANGLE;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
							
						case LANGLE:
							switch (buffer[++i]) {
								case '/':
									state = END_TAG;
									startPosition = i + 1;
									break;
								case '!':
								{
									switch (buffer[++i]) {
										case '-':
											state = COMMENT_MINUS;
											i--;
											break;
										default :
											state = COMMENT;
											break;
									}
								}
								case '?':
									state = COMMENT;
									break;
								default:
									state = START_TAG;
									startPosition = i;
									break;
							}
							break;
							
						case SCRIPT:
							while (true) {
								if (buffer[++i] == '<') {
									if (buffer[++i] == '/') {
										state = END_TAG;
										startPosition = i + 1;
										break mainSwitchLabel;
									}
								}
							}
							
						case COMMENT_MINUS:
						{
							int minusCounter = 0;
							while (minusCounter != 2) {
								switch (buffer[++i]) {
									case '-':
										minusCounter++;
										break;
									default :
										minusCounter = 0;
										break;
								}
							}
							switch (buffer[++i]) {
								case '>':
									state = TEXT;
									startPosition = i + 1;
									break mainSwitchLabel;
							}
						}
						
						case COMMENT :
							while (true) {
								switch (buffer[++i]) {
									case '>':
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
							
						case START_TAG:
							while (true) {
								switch (buffer[++i]) {
									case 0x0008:
									case 0x0009:
									case 0x000A:
									case 0x000B:
									case 0x000C:
									case 0x000D:
									case ' ':
										state = WS_AFTER_START_TAG_NAME;
										tagName.append(buffer, startPosition, i - startPosition);
										break mainSwitchLabel;
									case '>':
										state = TEXT;
										tagName.append(buffer, startPosition, i - startPosition);
										{
											String stringTagName = convertToString(strings, tagName);
											processStartTag(htmlReader, stringTagName, attributes);
											if ("script".equals(stringTagName.toLowerCase())) {
												scriptOpened = true;
												state = SCRIPT;
												break mainSwitchLabel;
											}
											if ("html".equals(stringTagName.toLowerCase())) {
												html = true;
											}
										}
										startPosition = i + 1;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										tagName.append(buffer, startPosition, i - startPosition);
										//processFullTag(htmlReader, convertToString(strings, tagName), attributes);
										break mainSwitchLabel;
									case '&':
										savedState = START_TAG;
										tagName.append(buffer, startPosition, i - startPosition);
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case END_TAG:
							while (true) {
								switch (buffer[++i]) {
									case 0x0008:
									case 0x0009:
									case 0x000A:
									case 0x000B:
									case 0x000C:
									case 0x000D:
									case ' ':
										state = WS_AFTER_END_TAG_NAME;
										tagName.append(buffer, startPosition, i - startPosition);
										break mainSwitchLabel;
									case '>':

											tagName.append(buffer, startPosition, i - startPosition);
											{
												String stringTagName = convertToString(strings, tagName);
												processEndTag(htmlReader, stringTagName);
												if (scriptOpened){
												}
												if ("script".equals(stringTagName.toLowerCase())) {
													scriptOpened = false;
												}
											}
										if (scriptOpened){
											state = SCRIPT;
										} else {
											state = TEXT;
											startPosition = i + 1;
										}
										break mainSwitchLabel;
								}
							}
						case WS_AFTER_START_TAG_NAME:
							switch (buffer[++i]) {
								case '>':
									{
										String stringTagName = convertToString(strings, tagName);
										processStartTag(htmlReader, stringTagName, attributes);
										if ("script".equals(stringTagName.toLowerCase())) {
											scriptOpened = true;
											state = SCRIPT;
											break mainSwitchLabel;
										}
									}
									state = TEXT;
									startPosition = i + 1;
									break;
								case '/':
									state = SLASH;
									break;
								case 0x0008:
								case 0x0009:
								case 0x000A:
								case 0x000B:
								case 0x000C:
								case 0x000D:
								case ' ':
									break;
								default:
									state = ATTRIBUTE_NAME;
									startPosition = i;
									break;
							}
							break;
							
						case WS_AFTER_END_TAG_NAME:
							switch (buffer[++i]) {
								case '>':
									{
										String stringTagName = convertToString(strings, tagName);
										processEndTag(htmlReader, stringTagName);
										if ("script".equals(stringTagName.toLowerCase())) {
											scriptOpened = false;
										}
									}
									if (scriptOpened){
										state = SCRIPT;
									} else {
										state = TEXT;
										startPosition = i + 1;
									}
									break;
							}
							break;
							
						case ATTRIBUTE_NAME:
							while (true) {
								switch (buffer[++i]) {
									case '=':
										attributeName.append(buffer, startPosition, i - startPosition);
										state = WAIT_ATTRIBUTE_VALUE;
										break mainSwitchLabel;
									case '&':
										attributeName.append(buffer, startPosition, i - startPosition);
										savedState = ATTRIBUTE_NAME;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
									case 0x0008:
									case 0x0009:
									case 0x000A:
									case 0x000B:
									case 0x000C:
									case 0x000D:
									case ' ':
										attributeName.append(buffer, startPosition, i - startPosition);
										state = WAIT_EQUALS;
										break mainSwitchLabel;
								}
							}
						case WAIT_EQUALS:
							while (true) {
								switch (buffer[++i]) {
									case '=':
										state = WAIT_ATTRIBUTE_VALUE;
										break mainSwitchLabel;
								}
							}
						case WAIT_ATTRIBUTE_VALUE:
							while (true) {
								switch (buffer[++i]) {
									case ' ' :
										break;
									case '\t' :
										break;
									case '\n' : 
										break;
									case '\'':
										state = S_ATTRIBUTE_VALUE;
										startPosition = i + 1;
										break mainSwitchLabel;
									case '"' :
										state = D_ATTRIBUTE_VALUE;
										startPosition = i + 1;
										break mainSwitchLabel;
									default :
										state = DEFAULT_ATTRIBUTE_VALUE;
										startPosition = i;
										break mainSwitchLabel;
								}
							}
						case DEFAULT_ATTRIBUTE_VALUE:
							while (true) {
								i++;
								if ((buffer[i] == ' ') || (buffer[i] == '\'') 
									|| (buffer[i] == '"') || (buffer[i] == '>')) {
									attributeValue.append(buffer, startPosition, i - startPosition);
									if (dontCacheAttributeValues) {
										attributes.put(convertToString(strings, attributeName), attributeValue.toString());
										attributeValue.clear();
									} else {
										attributes.put(convertToString(strings, attributeName), convertToString(strings, attributeValue));
									}
								}
								switch (buffer[i]) {
									case ' ':
									case '\'':
									case '"':
										state = WS_AFTER_START_TAG_NAME;
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = DEFAULT_ATTRIBUTE_VALUE;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										break mainSwitchLabel;
									case '>':
										String stringTagName = convertToString(strings, tagName);
										
										processStartTag(htmlReader, stringTagName, attributes);
										if ("script".equals(stringTagName.toLowerCase())) {
											scriptOpened = true;
											state = SCRIPT;
											break mainSwitchLabel;
										}
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}	
						case D_ATTRIBUTE_VALUE:
							while (true) {
								switch (buffer[++i]) {
									case '"':
										attributeValue.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_START_TAG_NAME;
										if (dontCacheAttributeValues) {
											attributes.put(convertToString(strings, attributeName), attributeValue.toString());
											attributeValue.clear();
										} else {
											attributes.put(convertToString(strings, attributeName), convertToString(strings, attributeValue));
										}
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = D_ATTRIBUTE_VALUE;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
							
						case S_ATTRIBUTE_VALUE:
							while (true) {
								switch (buffer[++i]) {
									case '\'':
										attributeValue.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_START_TAG_NAME;
										if (dontCacheAttributeValues) {
											attributes.put(convertToString(strings, attributeName), attributeValue.toString());
											attributeValue.clear();
										} else {
											attributes.put(convertToString(strings, attributeName), convertToString(strings, attributeValue));
										}
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = S_ATTRIBUTE_VALUE;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case ENTITY_REF:
							int savePosition = i;
							while (i - savePosition < 6) {
								switch (buffer[++i]) {
									case ';':
										
										entityName.append(buffer, startPosition, i - startPosition);
										state = savedState;
										startPosition = i + 1;
										final char[] value = getEntityValue(entityMap, convertToString(strings, entityName));
										if (value != null) {
											switch (state) {
												case S_ATTRIBUTE_VALUE:
												case D_ATTRIBUTE_VALUE:
													attributeValue.append(value, 0, value.length);
													break;
												case ATTRIBUTE_NAME:
													attributeName.append(value, 0, value.length);
													break;
												case START_TAG:
												case END_TAG:
													tagName.append(value, 0, value.length);
													break;
												case TEXT:
													htmlReader.characterDataHandler(value, 0, value.length);
													break;
											}
										}
										break mainSwitchLabel;
								}
							}
							i = savePosition;
							state = savedState;
						case SLASH:
							while (true) {
								switch (buffer[++i]) {
									case ' ':
										break;
									case '>':
										processFullTag(htmlReader, convertToString(strings, tagName), attributes);
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
									default :
										state = DEFAULT_ATTRIBUTE_VALUE;
										break mainSwitchLabel;
								}
							}
						case TEXT:
							while (true) {
								switch (buffer[++i]) {
									case '<':
										if (i > startPosition) {
											htmlReader.characterDataHandlerFinal(buffer, startPosition, i - startPosition);
										}
										state = LANGLE;
										break mainSwitchLabel;
									case '&':
										if (i > startPosition) {
											htmlReader.characterDataHandler(buffer, startPosition, i - startPosition);
										}
										savedState = TEXT;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				switch (state) {
					case START_TAG:
					case END_TAG:
						tagName.append(buffer, startPosition, count - startPosition);
						break;
					case ATTRIBUTE_NAME:
						attributeName.append(buffer, startPosition, count - startPosition);
						break;
					case S_ATTRIBUTE_VALUE:
					case D_ATTRIBUTE_VALUE:
						attributeValue.append(buffer, startPosition, count - startPosition);
						break;
					case ENTITY_REF:
						entityName.append(buffer, startPosition, count - startPosition);
						break;
					case TEXT:
						htmlReader.characterDataHandler(buffer, startPosition, count - startPosition);
						break;
				}
			}
		}
	}

	private static void processFullTag(ZLHtmlReader htmlReader, String tagName, ZLStringMap attributes) {
		htmlReader.startElementHandler(tagName, attributes);
		htmlReader.endElementHandler(tagName);
		attributes.clear();
	}

	private static void processStartTag(ZLHtmlReader htmlReader, String tagName, ZLStringMap attributes) {
		htmlReader.startElementHandler(tagName, attributes);
		attributes.clear();
	}

	private static void processEndTag(ZLHtmlReader htmlReader, String tagName) {
		htmlReader.endElementHandler(tagName);
	}
}
