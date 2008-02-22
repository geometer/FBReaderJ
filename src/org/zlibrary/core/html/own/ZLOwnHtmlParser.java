package org.zlibrary.core.html.own;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.util.ZLArrayUtils;
import org.zlibrary.core.html.ZLStringMap;
import org.zlibrary.core.html.ZLHtmlReader;

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
	//private static final byte WS_AFTER_END_TAG_NAME = 9;
	private static final byte WAIT_EQUALS = 10;
	private static final byte WAIT_ATTRIBUTE_VALUE = 11;
	private static final byte SLASH = 12;
	private static final byte ATTRIBUTE_NAME = 13;
	private static final byte ATTRIBUTE_VALUE = 14;
	private static final byte DEFAULT_ATTRIBUTE_VALUE = 15;
	private static final byte ENTITY_REF = 16;

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

		String encoding = "UTF-8";
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
				int startIndex = xmlDescription.indexOf('=', index) + 1;
				if (startIndex > 0) {
					int endIndex = Math.min(xmlDescription.indexOf('"', startIndex + 1),
								Math.min(xmlDescription.indexOf(' ', startIndex + 1), 
										xmlDescription.indexOf('>', startIndex + 1)));
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

	public void doIt() throws IOException {
		final HashMap entityMap = new HashMap();
		entityMap.put("amp", new char[] { '&' });
		entityMap.put("apos", new char[] { '\'' });
		entityMap.put("gt", new char[] { '>' });
		entityMap.put("lt", new char[] { '<' });
		entityMap.put("quot", new char[] { '\"' });

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
		String[] tagStack = new String[10];
		int tagStackSize = 0;

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
											processStartTag(htmlReader, stringTagName, attributes);
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
						case WS_AFTER_START_TAG_NAME:
							switch (buffer[++i]) {
								case '>':
									{
										String stringTagName = convertToString(strings, tagName);
										if (tagStackSize == tagStack.length) {
											tagStack = ZLArrayUtils.createCopy(tagStack, tagStackSize, tagStackSize << 1);
										}
										tagStack[tagStackSize++] = stringTagName;
										processStartTag(htmlReader, stringTagName, attributes);
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
									case '"' :
										state = ATTRIBUTE_VALUE;
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
								if ((buffer[i] == ' ') || (buffer[i] == '"') || (buffer[i] == '>')) {
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
									case '"':
										state = WS_AFTER_START_TAG_NAME;
										break mainSwitchLabel;
									case '&':
										attributeValue.append(buffer, startPosition, i - startPosition);
										savedState = ATTRIBUTE_VALUE;
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										break mainSwitchLabel;
									case '>':
										String stringTagName = convertToString(strings, tagName);
										if (tagStackSize == tagStack.length) {
											tagStack = ZLArrayUtils.createCopy(tagStack, tagStackSize, tagStackSize << 1);
										}
										tagStack[tagStackSize++] = stringTagName;
										processStartTag(htmlReader, stringTagName, attributes);
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
							}	
						case ATTRIBUTE_VALUE:
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
										if (value != null) {
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
													htmlReader.characterDataHandler(value, 0, value.length);
													break;
											}
										}
										break mainSwitchLabel;
								}
							}
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
						case END_TAG:
							while (true) {
								switch (buffer[++i]) {
									case '>':
										if (tagStackSize > 0) {
											processEndTag(htmlReader, tagStack[--tagStackSize]);
										}
										state = TEXT;
										startPosition = i + 1;
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
