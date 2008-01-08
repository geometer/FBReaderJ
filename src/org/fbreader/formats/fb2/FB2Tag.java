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

	private static final HashMap ourTagByName = new HashMap(256, 0.2f);
	private static final Byte ourUnknownTag = UNKNOWN;

	private static void addToTable(String name, byte tag) {
		ourTagByName.put(name, tag);
	}

	static {
		ourTagByName.put("unknown", ourUnknownTag);
		addToTable("p", P);
		addToTable("v", V);
		addToTable("subtitle", SUBTITLE);
		addToTable("text-author", TEXT_AUTHOR);
		addToTable("date", DATE);
		addToTable("cite", CITE);
		addToTable("section", SECTION);
		addToTable("poem", POEM);
		addToTable("stanza", STANZA);
		addToTable("epigraph", EPIGRAPH);
		addToTable("annotation", ANNOTATION);
		addToTable("coverpage", COVERPAGE);
		addToTable("a", A);
		addToTable("empty-line", EMPTY_LINE);
		addToTable("sup", SUP);
		addToTable("sub", SUB);
		addToTable("emphasis", EMPHASIS);
		addToTable("strong", STRONG);
		addToTable("code", CODE);
		addToTable("strikethrough", STRIKETHROUGH);
		addToTable("title", TITLE);
		addToTable("body", BODY);
		addToTable("image", IMAGE);
		addToTable("binary", BINARY);
		addToTable("fictionbook", FICTIONBOOK);
	}

	public static byte getTagByName(String name) {
		final HashMap tagByName = ourTagByName;
		Byte num = (Byte)tagByName.get(name);
		if (num == null) {
			final String upperCaseName = name.toLowerCase().intern();
			num = (Byte)tagByName.get(upperCaseName);
			if (num == null) {
				num = ourUnknownTag;
				tagByName.put(upperCaseName, num);
			}
			tagByName.put(name, num);
		}
		return num.byteValue();
	}

	private FB2Tag() {
	}
}
