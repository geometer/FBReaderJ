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

package org.geometerplus.zlibrary.core.xml;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;

final class ZLXMLParser {
	private static final byte START_DOCUMENT = 0;
	private static final byte START_TAG = 1;
	private static final byte END_TAG = 2;
	private static final byte TEXT = 3;
	//private static final byte IGNORABLE_WHITESPACE = 4;
	//private static final byte PROCESSING_INSTRUCTION = 5;
	private static final byte COMMENT = 6; // tag of form <!-- -->
	private static final byte END_OF_COMMENT1 = 7;
	private static final byte END_OF_COMMENT2 = 8;
	private static final byte EXCL_TAG = 9; // tag of form <! >
	private static final byte EXCL_TAG_START = 10;
	private static final byte Q_TAG = 11; // tag of form <? ?>
	private static final byte END_OF_Q_TAG = 12;
	private static final byte LANGLE = 13;
	private static final byte WS_AFTER_START_TAG_NAME = 14;
	private static final byte WS_AFTER_ATTRIBUTE_VALUE = 15;
	//private static final byte WS_AFTER_END_TAG_NAME = 16;
	private static final byte WAIT_EQUALS = 17;
	private static final byte WAIT_ATTRIBUTE_VALUE = 18;
	private static final byte SLASH = 19;
	private static final byte ATTRIBUTE_NAME = 20;
	private static final byte ATTRIBUTE_VALUE_QUOT = 21;
	private static final byte ATTRIBUTE_VALUE_APOS = 22;
	private static final byte ENTITY_REF = 23;
	private static final byte CDATA = 24; // <![CDATA[...]]>
	private static final byte END_OF_CDATA1 = 25;
	private static final byte END_OF_CDATA2 = 26;

	private static String convertToString(Map<ZLMutableString,String> strings, ZLMutableString container) {
		String s = strings.get(container);
		if (s == null) {
			s = container.toString();
			strings.put(new ZLMutableString(container), s);
		}
		container.clear();
		return s;
	}

	private final InputStreamReader myStreamReader;
	private final ZLXMLReader myXMLReader;
	private final boolean myProcessNamespaces;

	private static HashMap<Integer,Queue<char[]>> ourBufferPool = new HashMap<Integer,Queue<char[]>>();
	private static Queue<ZLMutableString> ourStringPool = new LinkedList<ZLMutableString>();

	private static synchronized char[] getBuffer(int bufferSize) {
		Queue<char[]> queue = ourBufferPool.get(bufferSize);
		if (queue != null) {
			char[] buffer = queue.poll();
			if (buffer != null) {
				return buffer;
			}
		}
		return new char[bufferSize];
	}

	private static synchronized void storeBuffer(char[] buffer) {
		Queue<char[]> queue = ourBufferPool.get(buffer.length);
		if (queue == null) {
			queue = new LinkedList<char[]>();
			ourBufferPool.put(buffer.length, queue);
		}
		queue.add(buffer);
	}

	private static synchronized ZLMutableString getMutableString() {
		ZLMutableString string = ourStringPool.poll();
		return (string != null) ? string : new ZLMutableString();
	}

	private static synchronized void storeString(ZLMutableString string) {
		ourStringPool.add(string);
	}

	private final char[] myBuffer;
	private int myBufferDescriptionLength;
	private final ZLMutableString myTagName = getMutableString();
	private final ZLMutableString myCData = getMutableString();
	private final ZLMutableString myAttributeName = getMutableString();
	private final ZLMutableString myAttributeValue = getMutableString();
	private final ZLMutableString myEntityName = getMutableString();

	void finish() {
		storeBuffer(myBuffer);
		storeString(myTagName);
		storeString(myAttributeName);
		storeString(myAttributeValue);
		storeString(myEntityName);
	}

	public ZLXMLParser(ZLXMLReader xmlReader, InputStream stream, int bufferSize) throws IOException {
		myXMLReader = xmlReader;
		myProcessNamespaces = xmlReader.processNamespaces();

		String encoding = "utf-8";
		final char[] buffer = getBuffer(bufferSize);
		myBuffer = buffer;
		boolean found = false;
		int len = 0;
		while (len < 256) {
			char c = (char)stream.read();
			buffer[len++] = c;
			if (c == '>') {
				found = true;
				break;
			}
		}
		myBufferDescriptionLength = len;
		if (found) {
			final String xmlDescription = new String(buffer, 0, len).trim();
			if (xmlDescription.startsWith("<?xml") && xmlDescription.endsWith("?>")) {
				myBufferDescriptionLength = 0;
				int index = xmlDescription.indexOf("encoding");
				if (index > 0) {
					int startIndex = xmlDescription.indexOf('"', index);
					if (startIndex > 0) {
						int endIndex = xmlDescription.indexOf('"', startIndex + 1);
						if (endIndex > 0) {
							encoding = xmlDescription.substring(startIndex + 1, endIndex);
						}
					}
				}
			}
		}

		myStreamReader = new InputStreamReader(stream, encoding);
	}

	private static char[] getEntityValue(HashMap<String,char[]> entityMap, String name) {
		char[] value = entityMap.get(name);
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

	private static ConcurrentHashMap<List<String>,HashMap<String,char[]>> ourDTDMaps = 
		new ConcurrentHashMap<List<String>,HashMap<String,char[]>>(); // FIXME: concurrency violation

	static HashMap<String,char[]> getDTDMap(List<String> dtdList) throws IOException {
		HashMap<String,char[]> entityMap = ourDTDMaps.get(dtdList);
		if (entityMap == null) {
			entityMap = new HashMap<String,char[]>();
			entityMap.put("amp", new char[] { '&' });
			entityMap.put("apos", new char[] { '\'' });
			entityMap.put("gt", new char[] { '>' });
			entityMap.put("lt", new char[] { '<' });
			entityMap.put("quot", new char[] { '\"' });
			for (String fileName : dtdList) {
				final InputStream stream = ZLResourceFile.createResourceFile(fileName).getInputStream();
				if (stream != null) {
					new ZLDTDParser().doIt(stream, entityMap);
				}
			}
			ourDTDMaps.put(dtdList, entityMap);
		}
		return entityMap;
	}

	private final static ConcurrentHashMap<ZLMutableString,String> ourStringMap = 
		new ConcurrentHashMap<ZLMutableString,String>();

	void doIt() throws IOException {
		final ZLXMLReader xmlReader = myXMLReader;
		final HashMap<String,char[]> entityMap = getDTDMap(xmlReader.externalDTDs());
		xmlReader.addExternalEntities(entityMap);
		final InputStreamReader streamReader = myStreamReader;
		final boolean processNamespaces = myProcessNamespaces;
		HashMap<String,String> oldNamespaceMap = processNamespaces ? new HashMap<String,String>() : null;
		HashMap<String,String> currentNamespaceMap = null;
		final ArrayList<HashMap<String,String>> namespaceMapStack = new ArrayList<HashMap<String,String>>();
		char[] buffer = myBuffer;
		final ZLMutableString tagName = myTagName;
		final ZLMutableString cData = myCData;
		final ZLMutableString attributeName = myAttributeName;
		final ZLMutableString attributeValue = myAttributeValue;
		final boolean dontCacheAttributeValues = xmlReader.dontCacheAttributeValues();
		final ZLMutableString entityName = myEntityName;
		final Map<ZLMutableString,String> strings = ourStringMap;//new HashMap();
		final ZLStringMap attributes = new ZLStringMap();
		String[] tagStack = new String[10];
		int tagStackSize = 0;

		byte state = START_DOCUMENT;
		byte savedState = START_DOCUMENT;
		while (true) {
			int count;
			if (myBufferDescriptionLength > 0) {
				count = myBufferDescriptionLength;
				myBufferDescriptionLength = 0;
			} else {
				count = streamReader.read(buffer);
			}
			if (count <= 0) {
				streamReader.close();
				return;
			}
			int startPosition = 0;
			if (count < buffer.length) {
				//buffer = ZLArrayUtils.createCopy(buffer, count, count);
				startPosition = buffer.length - count;
				System.arraycopy(buffer, 0, buffer, startPosition, count);
				count = buffer.length;
			}
			try {
				for (int i = startPosition - 1;;) {
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
									state = EXCL_TAG_START;
									break;
								case '?':
									state = Q_TAG;
									break;
								default:
									state = START_TAG;
									startPosition = i;
									break;
							}
							break;
						case EXCL_TAG_START:
							switch (buffer[++i]) {
								case '-':
									state = COMMENT;
									break;
								case '[':
									state = CDATA;
									startPosition = i + 1;
									break;
								default:
									state = EXCL_TAG;
									break;
							}
							break;
						case EXCL_TAG:
							while (true) {
								switch (buffer[++i]) {
									case '>':
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case CDATA:
							while (true) {
								switch (buffer[++i]) {
									case ']':
										state = END_OF_CDATA1;
										break mainSwitchLabel;
								}
							}
						case END_OF_CDATA1:
							if (buffer[++i] == ']') {
								state = END_OF_CDATA2;
							} else {
								state = CDATA;
							}
							break;
						case END_OF_CDATA2:
							if (buffer[++i] == '>') {
								cData.append(buffer, startPosition, i - startPosition);
								int len = cData.myLength;
								if (len > 8) {
									char data[] = cData.myData;
									if (new String(data, 0, 6).equals("CDATA[")) {
										xmlReader.characterDataHandler(data, 6, len - 8);
									}
								}
								cData.clear();
								state = TEXT;
								startPosition = i + 1;
							} else {
								state = CDATA;
							}
							break;
						case COMMENT:
							while (true) {
								switch (buffer[++i]) {
									case '-':
										state = END_OF_COMMENT1;
										break mainSwitchLabel;
								}
							}
						case END_OF_COMMENT1:
							if (buffer[++i] == '-') {
								state = END_OF_COMMENT2;
							} else {
								state = COMMENT;
							}
							break mainSwitchLabel;
						case END_OF_COMMENT2:
							switch (buffer[++i]) {
								case '>':
									state = TEXT;
									startPosition = i + 1;
									break;
								case '-':
									break;
								default:
									state = COMMENT;
									break;
							}
							break;
						case Q_TAG:
							while (true) {
								switch (buffer[++i]) {
									case '?':
										state = END_OF_Q_TAG;
										break mainSwitchLabel;
								}
							}
						case END_OF_Q_TAG:
							if (buffer[++i] == '>') {
								state = TEXT;
								startPosition = i + 1;
							} else {
								state = Q_TAG;
							}
							break mainSwitchLabel;
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
											if (tagStackSize == tagStack.length) {
												tagStack = ZLArrayUtils.createCopy(tagStack, tagStackSize, tagStackSize << 1);
											}
											tagStack[tagStackSize++] = stringTagName;
											if (processNamespaces) {
												if (currentNamespaceMap != null) {
													oldNamespaceMap = currentNamespaceMap;
												}
												namespaceMapStack.add(currentNamespaceMap);
											}
											if (processStartTag(xmlReader, stringTagName, attributes, currentNamespaceMap)) {
												streamReader.close();
												return;
											}
											currentNamespaceMap = null;
										}
										startPosition = i + 1;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										tagName.append(buffer, startPosition, i - startPosition);
										if (processFullTag(xmlReader, convertToString(strings, tagName), attributes)) {
											streamReader.close();
											return;
										}
										currentNamespaceMap = null;
										break mainSwitchLabel;
									case '&':
										savedState = START_TAG;
										tagName.append(buffer, startPosition, i - startPosition);
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case WS_AFTER_START_TAG_NAME:
							switch (buffer[++i]) {
								case '>':
									{
										String stringTagName = convertToString(strings, tagName);
										if (tagStackSize == tagStack.length) {
											tagStack = ZLArrayUtils.createCopy(tagStack, tagStackSize, tagStackSize << 1);
										}
										tagStack[tagStackSize++] = stringTagName;
										if (processNamespaces) {
											if (currentNamespaceMap != null) {
												oldNamespaceMap = currentNamespaceMap;
											}
											namespaceMapStack.add(currentNamespaceMap);
										}
										if (processStartTag(xmlReader, stringTagName, attributes, currentNamespaceMap)) {
											streamReader.close();
											return;
										}
										currentNamespaceMap = null;
									}
									state = TEXT;
									startPosition = i + 1;
									break;
								case '/':
									state = SLASH;
									if (processFullTag(xmlReader, convertToString(strings, tagName), attributes)) {
										streamReader.close();
										return;
									}
									currentNamespaceMap = null;
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
									case '"':
										state = ATTRIBUTE_VALUE_QUOT;
										startPosition = i + 1;
										break mainSwitchLabel;
									case '\'':
										state = ATTRIBUTE_VALUE_APOS;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case WS_AFTER_ATTRIBUTE_VALUE:
							switch (buffer[++i]) {
								case 0x0008:
								case 0x0009:
								case 0x000A:
								case 0x000B:
								case 0x000C:
								case 0x000D:
								case ' ':
									state = WS_AFTER_START_TAG_NAME;
									break;
								case '/':
								case '>':
									state = WS_AFTER_START_TAG_NAME;
									--i;
									break;
								case '"':
									if (i != 0) {
										attributeValue.append(buffer, i - 1, 1);
									}
									break mainSwitchLabel;
								default:
									state = ATTRIBUTE_NAME;
									break mainSwitchLabel;
							}
							final String aName = convertToString(strings, attributeName);
							if (processNamespaces && aName.startsWith("xmlns:")) {
								if (currentNamespaceMap == null) {
									currentNamespaceMap = new HashMap<String,String>(oldNamespaceMap);
								}
								currentNamespaceMap.put(aName.substring(6), attributeValue.toString());
								attributeValue.clear();
							} else if (dontCacheAttributeValues) {
								attributes.put(aName, attributeValue.toString());
								attributeValue.clear();
							} else {
								attributes.put(aName, convertToString(strings, attributeValue));
							}
							break;
						case ATTRIBUTE_VALUE_QUOT:
							while (true) {
								switch (buffer[++i]) {
									case '"':
										attributeValue.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_ATTRIBUTE_VALUE;
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = ATTRIBUTE_VALUE_QUOT;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case ATTRIBUTE_VALUE_APOS:
							while (true) {
								switch (buffer[++i]) {
									case '\'':
										attributeValue.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_ATTRIBUTE_VALUE;
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = ATTRIBUTE_VALUE_APOS;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case ENTITY_REF:
							while (true) {
								switch (buffer[++i]) {
									case ';':
										entityName.append(buffer, startPosition, i - startPosition);
										state = savedState;
										startPosition = i + 1;
										final char[] value = getEntityValue(entityMap, convertToString(strings, entityName));
										if ((value != null) && (value.length != 0)) {
											switch (state) {
												case ATTRIBUTE_VALUE_QUOT:
												case ATTRIBUTE_VALUE_APOS:
													attributeValue.append(value, 0, value.length);
													break;
												case ATTRIBUTE_NAME:
													attributeName.append(value, 0, value.length);
													break;
												case START_TAG:
												//case END_TAG:
													tagName.append(value, 0, value.length);
													break;
												case TEXT:
													xmlReader.characterDataHandler(value, 0, value.length);
													break;
											}
										}
										break mainSwitchLabel;
								}
							}
						case SLASH:
							while (true) {
								switch (buffer[++i]) {
									case '>':
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						case END_TAG:
							while (true) {
								switch (buffer[++i]) {
									case '>':
										//tagName.append(buffer, startPosition, i - startPosition);
										if (tagStackSize > 0) {
											if (processNamespaces &&
													(namespaceMapStack.remove(tagStackSize - 1) != null)) {
												for (int j = namespaceMapStack.size() - 1; j >= 0; --j) {
													HashMap<String,String> element = namespaceMapStack.get(j);
													if (element != null) {
														oldNamespaceMap = element;
														currentNamespaceMap = oldNamespaceMap;
														break;
													}
												}
											}
											if (processEndTag(xmlReader, tagStack[--tagStackSize], currentNamespaceMap)) {
												streamReader.close();
												return;
											}
											currentNamespaceMap = null;
										}
										//processEndTag(xmlReader, convertToString(strings, tagName), currentNamespaceMap);
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
									//case '&':
										//tagName.append(buffer, startPosition, i - startPosition);
										//savedState = END_TAG;
										//state = ENTITY_REF;
										//startPosition = i + 1;
										//break mainSwitchLabel;
									//case 0x0008:
									//case 0x0009:
									//case 0x000A:
									//case 0x000B:
									//case 0x000C:
									//case 0x000D:
									//case ' ':
										//tagName.append(buffer, startPosition, i - startPosition);
										//state = WS_AFTER_END_TAG_NAME;
										//break mainSwitchLabel;
								}
							}
						/*
						case WS_AFTER_END_TAG_NAME:
							while (true) {
								switch (buffer[++i]) {
									case '>':
										state = TEXT;
										if (tagStackSize > 0) {
											processEndTag(xmlReader, tagStack[--tagStackSize], currentNamespaceMap);
										}
										//processEndTag(xmlReader, convertToString(strings, tagName), currentNamespaceMap);
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}
						*/
						case TEXT:
							while (true) {
								switch (buffer[++i]) {
									case '<':
										if (i > startPosition) {
											xmlReader.characterDataHandlerFinal(buffer, startPosition, i - startPosition);
										}
										state = LANGLE;
										break mainSwitchLabel;
									case '&':
										if (i > startPosition) {
											xmlReader.characterDataHandler(buffer, startPosition, i - startPosition);
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
				if (count > startPosition) {
					switch (state) {
						case START_TAG:
						//case END_TAG:
							tagName.append(buffer, startPosition, count - startPosition);
							break;
						case ATTRIBUTE_NAME:
							attributeName.append(buffer, startPosition, count - startPosition);
							break;
						case ATTRIBUTE_VALUE_QUOT:
						case ATTRIBUTE_VALUE_APOS:
							attributeValue.append(buffer, startPosition, count - startPosition);
							break;
						case ENTITY_REF:
							entityName.append(buffer, startPosition, count - startPosition);
							break;
						case CDATA:
						case END_OF_CDATA1:
						case END_OF_CDATA2:
							cData.append(buffer, startPosition, count - startPosition);
							break;
						case TEXT:
							xmlReader.characterDataHandler(buffer, startPosition, count - startPosition);
							break;
					}
				}
			}
		}
	}

	private static boolean processFullTag(ZLXMLReader xmlReader, String tagName, ZLStringMap attributes) {
		if (xmlReader.startElementHandler(tagName, attributes)) {
			return true;
		}
		if (xmlReader.endElementHandler(tagName)) {
			return true;
		}
		attributes.clear();
		return false;
	}

	private static boolean processStartTag(ZLXMLReader xmlReader, String tagName, ZLStringMap attributes, HashMap<String,String> currentNamespaceMap) {
		if (xmlReader.startElementHandler(tagName, attributes)) {
			return true;
		}
		if (currentNamespaceMap != null) {
			xmlReader.namespaceMapChangedHandler(currentNamespaceMap);
		}
		attributes.clear();
		return false;
	}

	private static boolean processEndTag(ZLXMLReader xmlReader, String tagName, HashMap<String,String> currentNamespaceMap) {
		if (currentNamespaceMap != null) {
			xmlReader.namespaceMapChangedHandler(currentNamespaceMap);
		}
		return xmlReader.endElementHandler(tagName);
	}
}
