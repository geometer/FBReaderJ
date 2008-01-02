package org.zlibrary.core.xml.own;

import java.util.HashMap;
import java.io.*;

import org.zlibrary.core.xml.ZLXMLReader;

final class ZLOwnXMLParser {
	private static final int START_DOCUMENT = 0;
	private static final int START_TAG = 1;
	private static final int END_TAG = 2;
	private static final int TEXT = 3;
	private static final int IGNORABLE_WHITESPACE = 4;
	private static final int PROCESSING_INSTRUCTION = 5;
	private static final int COMMENT = 6;
	private static final int LANGLE = 7;
	private static final int WS_AFTER_START_TAG_NAME = 8;
	private static final int WS_AFTER_END_TAG_NAME = 9;
	private static final int WAIT_EQUALS = 10;
	private static final int WAIT_ATTRIBUTE_VALUE = 11;
	private static final int SLASH = 12;
	private static final int ATTRIBUTE_NAME = 13;
	private static final int ATTRIBUTE_VALUE = 14;
	private static final int ENTITY_REF = 15;

	private static final class StringContainer {
		private char[] myData;
		private int myLength;

		StringContainer(int len) {
			myData = new char[len];
		}

		StringContainer() {
			this(20);
		}

		StringContainer(StringContainer container) {
			final int len = container.myLength;
			final char[] data = new char[len];
			myData = data;
			myLength = len;
			System.arraycopy(container.myData, 0, data, 0, len);
		}

		public void append(char[] buffer, int offset, int count) {
			final int len = myLength;
			char[] data = myData;
			final int newLength = len + count;
			if (data.length < newLength) {
				char[] data0 = new char[newLength];
				if (len > 0) {
					System.arraycopy(data, 0, data0, 0, len);
				}
				data = data0;
				myData = data;
			}
			System.arraycopy(buffer, offset, data, len, count);
			myLength = newLength;
		}

		public void clear() {
			myLength = 0;
		}

		public boolean equals(Object o) {
			final StringContainer container = (StringContainer)o;
			final int len = myLength;
			if (len != container.myLength) {
				return false;
			}
			final char[] data0 = myData;
			final char[] data1 = container.myData;
			for (int i = 0; i < len; ++i) {
				if (data0[i] != data1[i]) {
					return false;
				}
			}
			return true;
		}

		public int hashCode() {
			final int len = myLength;
			final char[] data = myData;
			int code = len * 31;
			if (len > 1) {
				code += data[0];
				code *= 31;
				code += data[1];
				if (len > 2) {
					code *= 31;
					code += data[2];
				}
			} else if (len > 0) {
				code += data[0];
			}
			return code;
		}

		public String toString() {
			return new String(myData, 0, myLength).intern();
		}
	}

	private static String convertToString(HashMap<StringContainer,String> strings, StringContainer contatiner) {
		String s = strings.get(contatiner);
		if (s == null) {
			s = contatiner.toString();
			strings.put(new StringContainer(contatiner), s);
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
			buffer[len] = (char)stream.read();
			if (buffer[len] == '>') {
				break;
			}
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
		final char[] buffer = myBuffer;
		final StringContainer tagName = new StringContainer();
		final StringContainer attributeName = new StringContainer();
		final StringContainer attributeValue = new StringContainer();
		final StringContainer entityName = new StringContainer();
		final HashMap<StringContainer,String> strings = new HashMap<StringContainer,String>();
		final StringMap attributes = new StringMap();

		int state = START_DOCUMENT;
		int savedState = START_DOCUMENT;
		while (true) {
			int count = streamReader.read(buffer);
			if (count <= 0) {
				return;
			}
			int startPosition = 0;
			for (int i = 0; i < count; ++i) {
				char c = buffer[i];
				switch (state) {
					case START_DOCUMENT:
						while ((c != '<') && (++i < count)) {
							c = buffer[i];
						}
						if (c == '<') {
							state = LANGLE;
							startPosition = i + 1;
						}
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
						while ((c != '>') && (++i < count)) {
							c = buffer[i];
						}
						if (c == '>') {
							state = TEXT;
							startPosition = i + 1;
						}
						break;
					case START_TAG:
startTagLabel:
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
									break startTagLabel;
								case '>':
									state = TEXT;
									tagName.append(buffer, startPosition, i - startPosition);
									processStartTag(xmlReader, convertToString(strings, tagName), attributes);
									startPosition = i + 1;
									break startTagLabel;
								case '/':
									state = SLASH;
									tagName.append(buffer, startPosition, i - startPosition);
									processFullTag(xmlReader, convertToString(strings, tagName), attributes);
									break startTagLabel;
								case '&':
									savedState = START_TAG;
									state = ENTITY_REF;
									startPosition = i + 1;
									break startTagLabel;
								default:
									if (++i == count) {
										tagName.append(buffer, startPosition, i - startPosition);
										break startTagLabel;
									}
									break;
							}
							c = buffer[i];
						}
						break;
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
attributeNameLabel:
						while (true) {
							switch (c) {
								case '=':
									state = WAIT_ATTRIBUTE_VALUE;
									break attributeNameLabel;
								case '&':
									savedState = ATTRIBUTE_NAME;
									state = ENTITY_REF;
									startPosition = i + 1;
									break attributeNameLabel;
								case 0x0008:
								case 0x0009:
								case 0x000A:
								case 0x000B:
								case 0x000C:
								case 0x000D:
								case ' ':
									state = WAIT_EQUALS;
									break attributeNameLabel;
								default:
									if (++i == count) {
										break attributeNameLabel;
									}
									break;
							}
							c = buffer[i];
						}
						attributeName.append(buffer, startPosition, i - startPosition);
						break;
					case WAIT_EQUALS:
						if (c == '=') {
							state = WAIT_ATTRIBUTE_VALUE;
						}
						break;
					case WAIT_ATTRIBUTE_VALUE:
						if (c == '"') {
							state = ATTRIBUTE_VALUE;
							startPosition = i + 1;
						}
						break;
					case ATTRIBUTE_VALUE:
						while ((c != '"') && (c != '&') && (++i < count)) {
							c = buffer[i];
						}
						attributeValue.append(buffer, startPosition, i - startPosition);
						if (c == '"') {
							state = WS_AFTER_START_TAG_NAME;
							attributes.put(convertToString(strings, attributeName), convertToString(strings, attributeValue));
						} else if (c == '&') {
							savedState = ATTRIBUTE_VALUE;
							state = ENTITY_REF;
							startPosition = i + 1;
						}
						break;
					case ENTITY_REF:
						while ((c != ';') && (++i < count)) {
							c = buffer[i];
						}
						entityName.append(buffer, startPosition, i - startPosition);
						if (c == ';') {
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
						}
						break;
					case SLASH:
						if (c == '>') {
							state = TEXT;
							startPosition = i + 1;
						}
						break;
					case END_TAG:
endTagLabel:
						while (true) {
							switch (c) {
								case '>':
									tagName.append(buffer, startPosition, i - startPosition);
									processEndTag(xmlReader, convertToString(strings, tagName));
									state = TEXT;
									startPosition = i + 1;
									break endTagLabel;
								case '&':
									savedState = END_TAG;
									state = ENTITY_REF;
									startPosition = i + 1;
									break endTagLabel;
								case 0x0008:
								case 0x0009:
								case 0x000A:
								case 0x000B:
								case 0x000C:
								case 0x000D:
								case ' ':
									tagName.append(buffer, startPosition, i - startPosition);
									state = WS_AFTER_END_TAG_NAME;
									break endTagLabel;
								default:
									if (++i == count) {
										tagName.append(buffer, startPosition, i - startPosition);
										break endTagLabel;
									}
									break;
							}
							c = buffer[i];
						}
						break;
					case WS_AFTER_END_TAG_NAME:
						if (c == '>') {
							state = TEXT;
							processEndTag(xmlReader, convertToString(strings, tagName));
							startPosition = i + 1;
						}
						break;
					case TEXT:
						while ((c != '<') && (c != '&') && (++i < count)) {
							c = buffer[i];
						}
						if (i > startPosition) {
							if (c == '<') {
								xmlReader.characterDataHandlerFinal(buffer, startPosition, i - startPosition);
							} else {
								xmlReader.characterDataHandler(buffer, startPosition, i - startPosition);
							}
						}
						if (c == '<') {
							state = LANGLE;
						} else if (c == '&') {
							savedState = TEXT;
							state = ENTITY_REF;
							startPosition = i + 1;
						}
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
