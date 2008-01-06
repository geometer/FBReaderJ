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

	private static final HashMap<String,Byte> ourTagByName = new HashMap<String,Byte>(256, 0.2f);
	private static final Byte ourUnknownTag = UNKNOWN;

	static {
		final HashMap<String,Byte> tagByName = ourTagByName;
		tagByName.put("unknown", ourUnknownTag);
		tagByName.put("p", P);
		tagByName.put("v", V);
		tagByName.put("subtitle", SUBTITLE);
		tagByName.put("text-author", TEXT_AUTHOR);
		tagByName.put("date", DATE);
		tagByName.put("cite", CITE);
		tagByName.put("section", SECTION);
		tagByName.put("poem", POEM);
		tagByName.put("stanza", STANZA);
		tagByName.put("epigraph", EPIGRAPH);
		tagByName.put("annotation", ANNOTATION);
		tagByName.put("coverpage", COVERPAGE);
		tagByName.put("a", A);
		tagByName.put("empty-line", EMPTY_LINE);
		tagByName.put("sup", SUP);
		tagByName.put("sub", SUB);
		tagByName.put("emphasis", EMPHASIS);
		tagByName.put("strong", STRONG);
		tagByName.put("code", CODE);
		tagByName.put("strikethrough", STRIKETHROUGH);
		tagByName.put("title", TITLE);
		tagByName.put("body", BODY);
		tagByName.put("image", IMAGE);
		tagByName.put("binary", BINARY);
		tagByName.put("fictionbook", FICTIONBOOK);
	}

	public static byte getTagByName(String name) {
		final HashMap<String,Byte> tagByName = ourTagByName;
		Byte num = tagByName.get(name);
		if (num == null) {
			final String upperCaseName = name.toLowerCase().intern();
			num = tagByName.get(upperCaseName);
			if (num == null) {
				num = ourUnknownTag;
				tagByName.put(upperCaseName, num);
			}
			tagByName.put(name, num);
		}
		return num;
	}

	private FB2Tag() {
	}
}
