package org.fbreader.formats.fb2;

import java.util.HashMap;

final class FB2Tag {
	public static final byte UNKNOWN = 0;
	public static final byte P = 1;
	public static final byte V = 2;
	public static final byte SUBTITLE = 3;
	public static final byte TEXT_AUTHOR = 4;
	public static final byte DATE = 5;
	public static final byte CITE = 6;
	public static final byte SECTION = 7;
	public static final byte POEM = 8;
	public static final byte STANZA = 9;
	public static final byte EPIGRAPH = 10;
	public static final byte ANNOTATION = 11;
	public static final byte COVERPAGE = 12;
	public static final byte A = 13;
	public static final byte EMPTY_LINE = 14;
	public static final byte SUP = 15;
	public static final byte SUB = 16;
	public static final byte EMPHASIS = 17;
	public static final byte STRONG = 18;
	public static final byte CODE = 19;
	public static final byte STRIKETHROUGH = 20;
	public static final byte TITLE = 21;
	public static final byte BODY = 22;
	public static final byte IMAGE = 23;
	public static final byte BINARY = 24;

	private static final HashMap<String,Byte> ourTagByName = new HashMap<String,Byte>();

	static {
		ourTagByName.put("UNKNOWN", UNKNOWN);
		ourTagByName.put("P", P);
		ourTagByName.put("V", V);
		ourTagByName.put("SUBTITLE", SUBTITLE);
		ourTagByName.put("TEXT-AUTHOR", TEXT_AUTHOR);
		ourTagByName.put("DATE", DATE);
		ourTagByName.put("CITE", CITE);
		ourTagByName.put("SECTION", SECTION);
		ourTagByName.put("POEM", POEM);
		ourTagByName.put("STANZA", STANZA);
		ourTagByName.put("EPIGRAPH", EPIGRAPH);
		ourTagByName.put("ANNOTATION", ANNOTATION);
		ourTagByName.put("COVERPAGE", COVERPAGE);
		ourTagByName.put("A", A);
		ourTagByName.put("EMPTY-LINE", EMPTY_LINE);
		ourTagByName.put("SUP", SUP);
		ourTagByName.put("SUB", SUB);
		ourTagByName.put("EMPHASIS", EMPHASIS);
		ourTagByName.put("STRONG", STRONG);
		ourTagByName.put("CODE", CODE);
		ourTagByName.put("STRIKETHROUGH", STRIKETHROUGH);
		ourTagByName.put("TITLE", TITLE);
		ourTagByName.put("BODY", BODY);
		ourTagByName.put("IMAGE", IMAGE);
		ourTagByName.put("BINARY", BINARY);
	}

	public static byte getTagByName(String name) {
		Byte num = ourTagByName.get(name);
		if (num == null) {
			final String upperCaseName = name.toUpperCase();
			num = ourTagByName.get(upperCaseName);
			if (num == null) {
				num = UNKNOWN;
				ourTagByName.put(upperCaseName, num);
			}
			ourTagByName.put(name, num);
		}
		return num;
	}

	private FB2Tag() {
	}
}
