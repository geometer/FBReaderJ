package org.fbreader.formats.fb2;

import java.util.*;
import org.zlibrary.core.util.*;

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
	
	public static final byte TITLE_INFO = 26;
	public static final byte BOOK_TITLE = 27;
	public static final byte AUTHOR = 28;
	public static final byte LANG = 29;
	public static final byte FIRST_NAME = 30;
	public static final byte MIDDLE_NAME = 31;
	public static final byte LAST_NAME = 32;
	public static final byte SEQUENCE = 33;
	
	
	private static final HashMap ourTagByName = new HashMap(256, 0.2f);
	private static final Byte ourUnknownTag;

	static {	
		ourTagByName.put("unknown", UNKNOWN);
		ourUnknownTag = (Byte)ourTagByName.get("unknown");
		ourTagByName.put("p", P);
		ourTagByName.put("v", V);
		ourTagByName.put("subtitle", SUBTITLE);
		ourTagByName.put("text-author", TEXT_AUTHOR);
		ourTagByName.put("date", DATE);
		ourTagByName.put("cite", CITE);
		ourTagByName.put("section", SECTION);
		ourTagByName.put("poem", POEM);
		ourTagByName.put("stanza", STANZA);
		ourTagByName.put("epigraph", EPIGRAPH);
		ourTagByName.put("annotation", ANNOTATION);
		ourTagByName.put("coverpage", COVERPAGE);
		ourTagByName.put("a", A);
		ourTagByName.put("empty-line", EMPTY_LINE);
		ourTagByName.put("sup", SUP);
		ourTagByName.put("sub", SUB);
		ourTagByName.put("emphasis", EMPHASIS);
		ourTagByName.put("strong", STRONG);
		ourTagByName.put("code", CODE);
		ourTagByName.put("strikethrough", STRIKETHROUGH);
		ourTagByName.put("title", TITLE);
		ourTagByName.put("title-info", TITLE_INFO);
		ourTagByName.put("body", BODY);
		ourTagByName.put("image", IMAGE);
		ourTagByName.put("binary", BINARY);
		ourTagByName.put("fictionbook", FICTIONBOOK);
		ourTagByName.put("book-title", BOOK_TITLE);
		ourTagByName.put("sequence", SEQUENCE);
		ourTagByName.put("first-name", FIRST_NAME);
		ourTagByName.put("middle-name", MIDDLE_NAME);
		ourTagByName.put("last-name", LAST_NAME);
		ourTagByName.put("book-title", BOOK_TITLE);
		ourTagByName.put("author", AUTHOR);
		ourTagByName.put("lang", LANG);
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
