package org.zlibrary.core.xml.own;

import java.util.TreeMap;
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

	private static final class ComparableString implements Comparable<ComparableString> {
		private CharSequence mySequence;

		ComparableString(CharSequence sequence) {
			mySequence = sequence;
		}

		ComparableString() {
		}

		void setSequence(CharSequence sequence) {
			mySequence = sequence;
		}

		public String toString() {
			return mySequence.toString();
		}

		public int compareTo(ComparableString string) {
			CharSequence s = mySequence;
			CharSequence s1 = string.mySequence;
			int len = s.length();
			int len1 = s1.length();
			if (len != len1) {
				return len - len1;
			}
			for (int i = 0; i < len; ++i) {
				int c = s.charAt(i);
				int c1 = s1.charAt(i);
				if (c != c1) {
					return c - c1;
				}
			}
			return 0;
		}
	}

	private static final TreeMap<ComparableString,String> ourStrings = new TreeMap<ComparableString,String>();
	private static final ComparableString ourPattern = new ComparableString();

	private static String convertToString(StringBuilder builder) {
		final TreeMap<ComparableString,String> strings = ourStrings;
		final ComparableString pattern = ourPattern;
		pattern.setSequence(builder);
		String s = strings.get(pattern);
		if (s == null) {
			s = builder.toString();
			strings.put(new ComparableString(s), s);
		}
		builder.delete(0, builder.length());
		return s;
	}

	private static void appendToName(StringBuilder name, char[] buffer, int startOffset, int endOffset) {
		name.append(buffer, startOffset, endOffset - startOffset);
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
		final StringBuilder tagName = new StringBuilder();
		final StringBuilder attributeName = new StringBuilder();
		final StringBuilder attributeValue = new StringBuilder();
		final StringBuilder entityName = new StringBuilder();
		final HashMap<String,String> attributes = new HashMap<String,String>();

		int state = START_DOCUMENT;
		int savedState = START_DOCUMENT;
		while (true) {
			int count = streamReader.read(buffer);
			if (count <= 0) {
				ourStrings.clear();
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
						while (true) {
							if (Character.isWhitespace(c)) {
								state = WS_AFTER_START_TAG_NAME;
								appendToName(tagName, buffer, startPosition, i);
								break;
							} else if (c == '>') {
								appendToName(tagName, buffer, startPosition, i);
								processStartTag(xmlReader, tagName, attributes);
								state = TEXT;
								startPosition = i + 1;
								break;
							} else if (c == '/') {
								state = SLASH;
								appendToName(tagName, buffer, startPosition, i);
								processFullTag(xmlReader, tagName, attributes);
								break;
							} else if (c == '&') {
								savedState = START_TAG;
								state = ENTITY_REF;
								startPosition = i + 1;
								break;
							} else if (++i == count) {
								appendToName(tagName, buffer, startPosition, i);
								break;
							}
							c = buffer[i];
						}
						break;
					case WS_AFTER_START_TAG_NAME:
						if (c == '>') {
							processStartTag(xmlReader, tagName, attributes);
							state = TEXT;
							startPosition = i + 1;
						} else if (c == '/') {
							state = SLASH;
							processFullTag(xmlReader, tagName, attributes);
						} else if (!Character.isWhitespace(c)) {
							state = ATTRIBUTE_NAME;
							startPosition = i;
						}
						break;
					case ATTRIBUTE_NAME:
						while (true) {
							if (c == '=') {
								state = WAIT_ATTRIBUTE_VALUE;
								break;
							} else if (c == '&') {
								savedState = ATTRIBUTE_NAME;
								state = ENTITY_REF;
								startPosition = i + 1;
								break;
							} else if (Character.isWhitespace(c)) {
								state = WAIT_EQUALS;
								break;
							} else if (++i == count) {
								break;
							}
							c = buffer[i];
						}
						appendToName(attributeName, buffer, startPosition, i);
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
						appendToName(attributeValue, buffer, startPosition, i);
						if (c == '"') {
							state = WS_AFTER_START_TAG_NAME;
							attributes.put(convertToString(attributeName), convertToString(attributeValue));
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
						appendToName(entityName, buffer, startPosition, i);
						if (c == ';') {
							state = savedState;
							startPosition = i + 1;
							final String name = convertToString(entityName);
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
										appendToName(attributeValue, value, 0, value.length);
										break;
									case ATTRIBUTE_NAME:
										appendToName(attributeName, value, 0, value.length);
										break;
									case START_TAG:
									case END_TAG:
										appendToName(tagName, value, 0, value.length);
										break;
									case TEXT:
										processText(xmlReader, value, 0, value.length);
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
						while (true) {
							if (c == '>') {
								appendToName(tagName, buffer, startPosition, i);
								processEndTag(xmlReader, tagName);
								state = TEXT;
								startPosition = i + 1;
								break;
							} else if (c == '&') {
								savedState = END_TAG;
								state = ENTITY_REF;
								startPosition = i + 1;
								break;
							} else if (Character.isWhitespace(c)) {
								appendToName(tagName, buffer, startPosition, i);
								state = WS_AFTER_END_TAG_NAME;
								break;
							} else if (++i == count) {
								appendToName(tagName, buffer, startPosition, i);
								break;
							}
							c = buffer[i];
						}
						break;
					case WS_AFTER_END_TAG_NAME:
						if (c == '>') {
							state = TEXT;
							processEndTag(xmlReader, tagName);
							startPosition = i + 1;
						}
						break;
					case TEXT:
						while ((c != '<') && (c != '&') && (++i < count)) {
							c = buffer[i];
						}
						processText(xmlReader, buffer, startPosition, i);
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

	private static void processFullTag(ZLXMLReader xmlReader, StringBuilder tagName, HashMap<String,String> attributes) {
		final String s = convertToString(tagName);
		xmlReader.startElementHandler(s, attributes);
		xmlReader.endElementHandler(s);
		attributes.clear();
	}

	private static void processStartTag(ZLXMLReader xmlReader, StringBuilder tagName, HashMap<String,String> attributes) {
		xmlReader.startElementHandler(convertToString(tagName), attributes);
		attributes.clear();
	}

	private static void processEndTag(ZLXMLReader xmlReader, StringBuilder tagName) {
		xmlReader.endElementHandler(convertToString(tagName));
	}

	private static void processText(ZLXMLReader xmlReader, char[] buffer, int startOffset, int endOffset) {
		if (endOffset > startOffset) {
			xmlReader.characterDataHandler(buffer, startOffset, endOffset - startOffset);
		}
	}
}
