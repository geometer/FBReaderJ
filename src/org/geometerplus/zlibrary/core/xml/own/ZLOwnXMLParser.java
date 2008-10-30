/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.xml.own;

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;

final class ZLOwnXMLParser {
	private static final byte START_DOCUMENT = 0;
	private static final byte START_TAG = 1;
	private static final byte END_TAG = 2;
	private static final byte TEXT = 3;
	private static final byte IGNORABLE_WHITESPACE = 4;
	private static final byte PROCESSING_INSTRUCTION = 5;
	private static final byte COMMENT = 6;
	private static final byte LANGLE = 7;
	private static final byte WS_AFTER_START_TAG_NAME = 8;
	private static final byte WS_AFTER_ATTRIBUTE_VALUE = 9;
	//private static final byte WS_AFTER_END_TAG_NAME = 9;
	private static final byte WAIT_EQUALS = 10;
	private static final byte WAIT_ATTRIBUTE_VALUE = 11;
	private static final byte SLASH = 12;
	private static final byte ATTRIBUTE_NAME = 13;
	private static final byte ATTRIBUTE_VALUE = 14;
	private static final byte ENTITY_REF = 15;

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
	private final ZLXMLReader myXMLReader;
	private final boolean myProcessNamespaces;

	private final char[] myBuffer = new char[65536];

	public ZLOwnXMLParser(ZLXMLReader xmlReader, InputStream stream) throws IOException {
		myXMLReader = xmlReader;
		myProcessNamespaces = xmlReader.processNamespaces();

		String encoding = "utf-8";
		final char[] buffer = myBuffer;
		int len;
		for (len = 0; len < 256; ++len) {
			char c = (char)stream.read();
			if (c == '>') {
				break;
			}
			buffer[len] = c;
		}
		if (len < 256) {
			String xmlDescription = new String(buffer, 0, len + 1);
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

		myStreamReader = new InputStreamReader(stream, encoding);
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

	private static HashMap ourDTDMaps = new HashMap();

	private static HashMap getDTDMap(ArrayList dtdList) throws IOException {
		HashMap entityMap = (HashMap)ourDTDMaps.get(dtdList);
		if (entityMap == null) {
			entityMap = new HashMap();
			entityMap.put("amp", new char[] { '&' });
			entityMap.put("apos", new char[] { '\'' });
			entityMap.put("gt", new char[] { '>' });
			entityMap.put("lt", new char[] { '<' });
			entityMap.put("quot", new char[] { '\"' });
			final int dtdListSize = dtdList.size();
			for (int i = 0; i < dtdListSize; ++i) {
				InputStream stream = new ZLFile((String)dtdList.get(i)).getInputStream();
				if (stream != null) {
					new ZLOwnDTDParser().doIt(stream, entityMap);
				}
			}
			ourDTDMaps.put(dtdList, entityMap);
		}
		return entityMap;
	}

	public void doIt() throws IOException {
		final ZLXMLReader xmlReader = myXMLReader;
		final HashMap entityMap = getDTDMap(xmlReader.externalDTDs());
		final InputStreamReader streamReader = myStreamReader;
		final boolean processNamespaces = myProcessNamespaces;
		HashMap oldNamespaceMap = processNamespaces ? new HashMap() : null;
		HashMap currentNamespaceMap = null;
		final ArrayList namespaceMapStack = new ArrayList();
		char[] buffer = myBuffer;
		final ZLMutableString tagName = new ZLMutableString();
		final ZLMutableString attributeName = new ZLMutableString();
		final ZLMutableString attributeValue = new ZLMutableString();
		final boolean dontCacheAttributeValues = xmlReader.dontCacheAttributeValues();
		final ZLMutableString entityName = new ZLMutableString();
		final HashMap strings = new HashMap();
		final ZLStringMap attributes = new ZLStringMap();
		String[] tagStack = new String[10];
		int tagStackSize = 0;

		byte state = START_DOCUMENT;
		byte savedState = START_DOCUMENT;
		while (true) {
			int count = streamReader.read(buffer);
			if (count <= 0) {
				streamReader.close();
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
								case '?':
									state = COMMENT;
									break;
								default:
									state = START_TAG;
									startPosition = i;
									break;
							}
							break;
						case COMMENT:
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
										state = ATTRIBUTE_VALUE;
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
									attributeValue.append(buffer, i - 1, 1);
									break mainSwitchLabel;
								default:
									state = ATTRIBUTE_VALUE;
									break mainSwitchLabel;
							}
							final String aName = convertToString(strings, attributeName);
							if (processNamespaces && aName.startsWith("xmlns:")) {
								if (currentNamespaceMap == null) {
									currentNamespaceMap = new HashMap(oldNamespaceMap);
								}
								currentNamespaceMap.put(attributeValue.toString(), aName.substring(6));
								attributeValue.clear();
							} else if (dontCacheAttributeValues) {
								attributes.put(aName, attributeValue.toString());
								attributeValue.clear();
							} else {
								attributes.put(aName, convertToString(strings, attributeValue));
							}
							break;
						case ATTRIBUTE_VALUE:
							while (true) {
								switch (buffer[++i]) {
									case '"':
										attributeValue.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_ATTRIBUTE_VALUE;
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = ATTRIBUTE_VALUE;
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
												case ATTRIBUTE_VALUE:
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
													Object element = namespaceMapStack.get(j);
													if (element != null) {
														oldNamespaceMap = (HashMap)element;
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
						case ATTRIBUTE_VALUE:
							attributeValue.append(buffer, startPosition, count - startPosition);
							break;
						case ENTITY_REF:
							entityName.append(buffer, startPosition, count - startPosition);
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

	private static boolean processStartTag(ZLXMLReader xmlReader, String tagName, ZLStringMap attributes, HashMap currentNamespaceMap) {
		if (xmlReader.startElementHandler(tagName, attributes)) {
			return true;
		}
		if (currentNamespaceMap != null) {
			xmlReader.namespaceListChangedHandler(currentNamespaceMap);
		}
		attributes.clear();
		return false;
	}

	private static boolean processEndTag(ZLXMLReader xmlReader, String tagName, HashMap currentNamespaceMap) {
		if (currentNamespaceMap != null) {
			xmlReader.namespaceListChangedHandler(currentNamespaceMap);
		}
		return xmlReader.endElementHandler(tagName);
	}
}
