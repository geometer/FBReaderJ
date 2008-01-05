package org.zlibrary.core.xml.own;

import java.util.HashMap;
import java.io.*;

import org.zlibrary.core.xml.ZLXMLReader;

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
	private static final byte WS_AFTER_END_TAG_NAME = 9;
	private static final byte WAIT_EQUALS = 10;
	private static final byte WAIT_ATTRIBUTE_VALUE = 11;
	private static final byte SLASH = 12;
	private static final byte ATTRIBUTE_NAME = 13;
	private static final byte ATTRIBUTE_VALUE = 14;
	private static final byte ENTITY_REF = 15;

	private static String convertToString(HashMap<ZLMutableString,String> strings, ZLMutableString contatiner) {
		String s = strings.get(contatiner);
		if (s == null) {
			s = contatiner.toString();
			strings.put(new ZLMutableString(contatiner), s);
		}
		contatiner.clear();
		return s;
	}

	private final InputStreamReader myStreamReader;
	private final ZLXMLReader myXMLReader;

	private final char[] myBuffer = new char[8192];

	public ZLOwnXMLParser(ZLXMLReader xmlReader, InputStream stream) throws IOException {
		myXMLReader = xmlReader;

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

	private static final class StringMap implements ZLXMLReader.StringMap {
		private int mySize;
		private String[] myKeys = new String[10];
		private String[] myValues = new String[10];

		public int getSize() {
			return mySize;
		}

		public String getKey(int index) {
			return myKeys[index];
		}

		public String getValue(String key) {
			final int size = mySize;
			if (size > 0) {
				key = key.intern();
				final String[] keys = myKeys;
				for (int i = 0; i < size; ++i) {
					if (key == keys[i]) {
						return myValues[i];
					}
				}
			}
			return null;
		}

		public void clear() {
			mySize = 0;
		}

		public void put(String key, String value) {
			final int size = mySize++;
			String[] keys = myKeys;
			if (keys.length == size) {
				keys = new String[2 * size];
				System.arraycopy(myKeys, 0, keys, 0, size);
				myKeys = keys;
				final String[] values = new String[2 * size];
				System.arraycopy(myValues, 0, values, 0, size);
				myValues = values;
			}
			keys[size] = key;
			myValues[size] = value;
		}
	}

	public void doIt() throws IOException {
		final HashMap<String,char[]> entityMap = new HashMap<String,char[]>();
		entityMap.put("amp", new char[] { '&' });
		entityMap.put("apos", new char[] { '\'' });
		entityMap.put("gt", new char[] { '>' });
		entityMap.put("lt", new char[] { '<' });
		entityMap.put("quot", new char[] { '\"' });

		final InputStreamReader streamReader = myStreamReader;
		final ZLXMLReader xmlReader = myXMLReader;
		char[] buffer = myBuffer;
		final ZLMutableString tagName = new ZLMutableString();
		final ZLMutableString attributeName = new ZLMutableString();
		final ZLMutableString attributeValue = new ZLMutableString();
		final boolean dontCacheAttributeValues = xmlReader.dontCacheAttributeValues();
		final ZLMutableString entityName = new ZLMutableString();
		final HashMap<ZLMutableString,String> strings = new HashMap<ZLMutableString,String>();
		final StringMap attributes = new StringMap();

		byte state = START_DOCUMENT;
		byte savedState = START_DOCUMENT;
		while (true) {
			int count = streamReader.read(buffer);
			if (count <= 0) {
				return;
			}
			if (count < buffer.length) {
				final char[] shortBuffer = new char[count];
				System.arraycopy(buffer, 0, shortBuffer, 0, count);
				buffer = shortBuffer;
			}
			int startPosition = 0;
			try {
				for (int i = 0; ; ++i) {
					char c = buffer[i];
mainSwitchLabel:
					switch (state) {
						case START_DOCUMENT:
							while (c != '<') {
								c = buffer[++i];
							}
							state = LANGLE;
							startPosition = i + 1;
							break;
						case LANGLE:
							switch (c) {
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
							while (c != '>') {
								c = buffer[++i];
							}
							state = TEXT;
							startPosition = i + 1;
							break;
						case START_TAG:
							while (true) {
								switch (c) {
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
										processStartTag(xmlReader, convertToString(strings, tagName), attributes);
										startPosition = i + 1;
										break mainSwitchLabel;
									case '/':
										state = SLASH;
										tagName.append(buffer, startPosition, i - startPosition);
										processFullTag(xmlReader, convertToString(strings, tagName), attributes);
										break mainSwitchLabel;
									case '&':
										savedState = START_TAG;
										tagName.append(buffer, startPosition, i - startPosition);
										state = ENTITY_REF;
										startPosition = i + 1;
										break mainSwitchLabel;
								}
								c = buffer[++i];
							}
						case WS_AFTER_START_TAG_NAME:
							switch (c) {
								case '>':
									processStartTag(xmlReader, convertToString(strings, tagName), attributes);
									state = TEXT;
									startPosition = i + 1;
									break;
								case '/':
									state = SLASH;
									processFullTag(xmlReader, convertToString(strings, tagName), attributes);
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
								switch (c) {
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
								c = buffer[++i];
							}
						case WAIT_EQUALS:
							while (c != '=') {
								c = buffer[++i];
							}
							state = WAIT_ATTRIBUTE_VALUE;
							break;
						case WAIT_ATTRIBUTE_VALUE:
							while (c != '"') {
								c = buffer[++i];
							}
							state = ATTRIBUTE_VALUE;
							startPosition = i + 1;
							break;
						case ATTRIBUTE_VALUE:
							while (true) {
								switch (c) {
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
								c = buffer[++i];
							}
						case ENTITY_REF:
							while (c != ';') {
								c = buffer[++i];
							}
							entityName.append(buffer, startPosition, i - startPosition);
							state = savedState;
							startPosition = i + 1;
							final String name = convertToString(strings, entityName);
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
							if (value != null) {
								switch (state) {
									case ATTRIBUTE_VALUE:
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
										xmlReader.characterDataHandler(value, 0, value.length);
										break;
								}
							}
							break;
						case SLASH:
							while (c != '>') {
								c = buffer[++i];
							}
							state = TEXT;
							startPosition = i + 1;
							break;
						case END_TAG:
							while (true) {
								switch (c) {
									case '>':
										tagName.append(buffer, startPosition, i - startPosition);
										processEndTag(xmlReader, convertToString(strings, tagName));
										state = TEXT;
										startPosition = i + 1;
										break mainSwitchLabel;
									case '&':
										tagName.append(buffer, startPosition, i - startPosition);
										savedState = END_TAG;
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
										tagName.append(buffer, startPosition, i - startPosition);
										state = WS_AFTER_END_TAG_NAME;
										break mainSwitchLabel;
								}
								c = buffer[++i];
							}
						case WS_AFTER_END_TAG_NAME:
							while (c != '>') {
								c = buffer[++i];
							}
							state = TEXT;
							processEndTag(xmlReader, convertToString(strings, tagName));
							startPosition = i + 1;
							break;
						case TEXT:
							while (true) {
								switch (c) {
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
								c = buffer[++i];
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

	private static void processFullTag(ZLXMLReader xmlReader, String tagName, StringMap attributes) {
		xmlReader.startElementHandler(tagName, attributes);
		xmlReader.endElementHandler(tagName);
		attributes.clear();
	}

	private static void processStartTag(ZLXMLReader xmlReader, String tagName, StringMap attributes) {
		xmlReader.startElementHandler(tagName, attributes);
		attributes.clear();
	}

	private static void processEndTag(ZLXMLReader xmlReader, String tagName) {
		xmlReader.endElementHandler(tagName);
	}
}
