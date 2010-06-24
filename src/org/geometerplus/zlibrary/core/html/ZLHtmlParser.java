/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.html;

import java.io.*;
import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.core.html.ZLHtmlReader;

final class ZLHtmlParser {
	private static final byte START_DOCUMENT = 0;
	private static final byte START_TAG = 1;
	private static final byte END_TAG = 2;
	private static final byte TEXT = 3;
	//private static final byte IGNORABLE_WHITESPACE = 4;
	//private static final byte PROCESSING_INSTRUCTION = 5;
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
	private static final byte COMMENT_MINUS = 17;
	private static final byte D_ATTRIBUTE_VALUE = 18;
	private static final byte SCRIPT = 19;
	private static final byte ENTITY_REF = 20;
	
	private static ZLByteBuffer unique(HashMap<ZLByteBuffer,ZLByteBuffer> strings, ZLByteBuffer container) {
		ZLByteBuffer s = strings.get(container);
		if (s == null) {
			s = new ZLByteBuffer(container);
			strings.put(s, s);
		}
		container.clear();
		return s;
	}

	private final ZLHtmlReader myReader;
	private final InputStream myStream;

	public ZLHtmlParser(ZLHtmlReader htmlReader, InputStream stream) throws IOException {
		myReader = htmlReader;
		myStream = stream;
	}

	public void doIt() throws IOException {
		final InputStream stream = myStream;
		final ZLHtmlReader htmlReader = myReader;
		byte[] buffer = new byte[8192];
		final ZLByteBuffer tagName = new ZLByteBuffer();
		final ZLByteBuffer attributeName = new ZLByteBuffer();
		final ZLByteBuffer attributeValue = new ZLByteBuffer();
		final ZLByteBuffer entityName = new ZLByteBuffer();
		final HashMap<ZLByteBuffer,ZLByteBuffer> strings = new HashMap<ZLByteBuffer,ZLByteBuffer>();
		final ZLHtmlAttributeMap attributes = new ZLHtmlAttributeMap();
		boolean scriptOpened = false;
		//boolean html = false;
		int bufferOffset = 0;
		int offset = 0;
		
		byte state = START_DOCUMENT;
		while (true) {
			final int count = stream.read(buffer);
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
							while (buffer[++i] != '<') {}
							state = LANGLE;
							break;
						case LANGLE:
							offset = bufferOffset + i;
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
										default:
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
											ZLByteBuffer stringTagName = unique(strings, tagName);
											processStartTag(htmlReader, stringTagName, offset, attributes);
											if (stringTagName.equalsToLCString("script")) {
												scriptOpened = true;
												state = SCRIPT;
												break mainSwitchLabel;
											}
											/*if (stringTagName.equalsToLCString("html")) {
												html = true;
											}*/
										}
										startPosition = i + 1;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										tagName.append(buffer, startPosition, i - startPosition);
										//processFullTag(htmlReader, unique(strings, tagName), attributes);
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
												ZLByteBuffer stringTagName = unique(strings, tagName);
												processEndTag(htmlReader, stringTagName);
												if (scriptOpened){
												}
												if (stringTagName.equalsToLCString("script")) {
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
										ZLByteBuffer stringTagName = unique(strings, tagName);
										processStartTag(htmlReader, stringTagName, offset, attributes);
										if (stringTagName.equalsToLCString("script")) {
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
										ZLByteBuffer stringTagName = unique(strings, tagName);
										processEndTag(htmlReader, stringTagName);
										if (stringTagName.equalsToLCString("script")) {
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
									attributes.put(unique(strings, attributeName), new ZLByteBuffer(attributeValue));
									attributeValue.clear();
								}
								switch (buffer[i]) {
									case ' ':
									case '\'':
									case '"':
										state = WS_AFTER_START_TAG_NAME;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										break mainSwitchLabel;
									case '>':
										ZLByteBuffer stringTagName = unique(strings, tagName);
										
										processStartTag(htmlReader, stringTagName, offset, attributes);
										if (stringTagName.equalsToLCString("script")) {
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
										attributes.put(unique(strings, attributeName), new ZLByteBuffer(attributeValue));
										attributeValue.clear();
										break mainSwitchLabel;
								}
							}
							
						case S_ATTRIBUTE_VALUE:
							while (true) {
								switch (buffer[++i]) {
									case '\'':
										attributeValue.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_START_TAG_NAME;
										attributes.put(unique(strings, attributeName), new ZLByteBuffer(attributeValue));
										attributeValue.clear();
										break mainSwitchLabel;
								}
							}
						case SLASH:
							while (true) {
								switch (buffer[++i]) {
									case ' ':
										break;
									case '>':
										processFullTag(htmlReader, unique(strings, tagName), offset, attributes);
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
											htmlReader.byteDataHandler(buffer, startPosition, i - startPosition);
										}
										state = LANGLE;
										break mainSwitchLabel;
									case '&':
										if (i > startPosition) {
											htmlReader.byteDataHandler(buffer, startPosition, i - startPosition);
										}
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case ENTITY_REF:
							while (true) {
								byte sym = buffer[++i];
								if (sym == ';') {
									entityName.append(buffer, startPosition, i - startPosition);
									state = TEXT;
									startPosition = i + 1;
									htmlReader.entityDataHandler(unique(strings, entityName).toString());
									entityName.clear();
									break mainSwitchLabel;
								} else if ((sym != '#') && !Character.isLetterOrDigit(sym)) {
									entityName.append(buffer, startPosition, i - startPosition);
									state = TEXT;
									startPosition = i;
									htmlReader.byteDataHandler(new byte[] { '&' }, 0, 1);
									htmlReader.byteDataHandler(entityName.myData, 0, entityName.myLength);
									entityName.clear();
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
					case TEXT:
						htmlReader.byteDataHandler(buffer, startPosition, count - startPosition);
						break;
					case ENTITY_REF:
						entityName.append(buffer, startPosition, count - startPosition);
						break;
				}
			}
			bufferOffset += count;
		}
	}

	private static void processFullTag(ZLHtmlReader htmlReader, ZLByteBuffer tagName, int offset, ZLHtmlAttributeMap attributes) {
		String stringTagName = tagName.toString();
		htmlReader.startElementHandler(stringTagName, offset, attributes);
		htmlReader.endElementHandler(stringTagName);
		attributes.clear();
	}

	private static void processStartTag(ZLHtmlReader htmlReader, ZLByteBuffer tagName, int offset, ZLHtmlAttributeMap attributes) {
		htmlReader.startElementHandler(tagName.toString(), offset, attributes);
		attributes.clear();
	}

	private static void processEndTag(ZLHtmlReader htmlReader, ZLByteBuffer tagName) {
		htmlReader.endElementHandler(tagName.toString());
	}
}
