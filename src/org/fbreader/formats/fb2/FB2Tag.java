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
	public static final byte FICTIONBOOK = 25;

	private static final HashMap<String,Byte> ourTagByName = new HashMap<String,Byte>(64);

	static {
		final HashMap<String,Byte> tagByName = ourTagByName;
		tagByName.put("UNKNOWN", UNKNOWN);
		tagByName.put("P", P);
		tagByName.put("V", V);
		tagByName.put("SUBTITLE", SUBTITLE);
		tagByName.put("TEXT-AUTHOR", TEXT_AUTHOR);
		tagByName.put("DATE", DATE);
		tagByName.put("CITE", CITE);
		tagByName.put("SECTION", SECTION);
		tagByName.put("POEM", POEM);
		tagByName.put("STANZA", STANZA);
		tagByName.put("EPIGRAPH", EPIGRAPH);
		tagByName.put("ANNOTATION", ANNOTATION);
		tagByName.put("COVERPAGE", COVERPAGE);
		tagByName.put("A", A);
		tagByName.put("EMPTY-LINE", EMPTY_LINE);
		tagByName.put("SUP", SUP);
		tagByName.put("SUB", SUB);
		tagByName.put("EMPHASIS", EMPHASIS);
		tagByName.put("STRONG", STRONG);
		tagByName.put("CODE", CODE);
		tagByName.put("STRIKETHROUGH", STRIKETHROUGH);
		tagByName.put("TITLE", TITLE);
		tagByName.put("BODY", BODY);
		tagByName.put("IMAGE", IMAGE);
		tagByName.put("BINARY", BINARY);
		tagByName.put("FICTIONBOOK", FICTIONBOOK);
	}

	public static byte getTagByName(String name) {
		final HashMap<String,Byte> tagByName = ourTagByName;
		Byte num = tagByName.get(name);
		if (num == null) {
			final String upperCaseName = name.toUpperCase().intern();
			num = tagByName.get(upperCaseName);
			if (num == null) {
				num = UNKNOWN;
				tagByName.put(upperCaseName, num);
			}
			tagByName.put(name, num);
		}
		return num;
	}

	private FB2Tag() {
	}
}
