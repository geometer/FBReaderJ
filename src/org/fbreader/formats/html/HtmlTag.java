package org.fbreader.formats.html;

import java.util.*;
import org.zlibrary.core.util.*;

final class HtmlTag {
	public static final byte HTML = -1;
	public static final byte UNKNOWN = 0;
	public static final byte HEAD = 1;
	public static final byte BODY = 2;
	public static final byte TITLE = 3;
	public static final byte P = 4;
	public static final byte H1 = 5;
	public static final byte H2 = 6;
	public static final byte H3 = 7;
	public static final byte H4 = 8;
	public static final byte H5 = 9;
	public static final byte H6 = 10;
	public static final byte A = 11;
	public static final byte B = 12;
	public static final byte I = 13;
	public static final byte BR = 14;
	public static final byte STRONG = 15;
	public static final byte IMG = 16;
	public static final byte SCRIPT = 17;
	public static final byte OL = 18;
	public static final byte UL = 19;
	public static final byte LI = 20;
	public static final byte SELECT = 21;
	public static final byte DIV = 22;
	public static final byte TR = 23;
	public static final byte STYLE = 24;
	
	private static final HashMap ourTagByName = new HashMap(256, 0.2f);
	private static final Byte ourUnknownTag;

	static {
		ourTagByName.put("unknown", UNKNOWN);
		ourUnknownTag = (Byte)ourTagByName.get("unknown");
		ourTagByName.put("html", HTML);
		ourTagByName.put("head", HEAD);
		ourTagByName.put("body", BODY);
		ourTagByName.put("title", TITLE);
		ourTagByName.put("p", P);
		ourTagByName.put("h1", H1);
		ourTagByName.put("h2", H2);
		ourTagByName.put("h3", H3);
		ourTagByName.put("h4", H4);
		ourTagByName.put("h5", H5);
		ourTagByName.put("h6", H6);
		ourTagByName.put("a", A);
		ourTagByName.put("b", B);
		ourTagByName.put("i", I);
		ourTagByName.put("br", BR);
		ourTagByName.put("strong", STRONG);
		ourTagByName.put("img", IMG);
		ourTagByName.put("script", SCRIPT);
		ourTagByName.put("ol", OL);
		ourTagByName.put("ul", UL);
		ourTagByName.put("li", LI);
		ourTagByName.put("select", SELECT);
		ourTagByName.put("tr", TR);
		ourTagByName.put("style", STYLE);
	}

	public static byte getTagByName(String name) {
		final HashMap tagByName = ourTagByName;
		Byte num = (Byte)tagByName.get(name);
		if (num == null) {
			final String lowerCaseName = name.toLowerCase().intern();
			num = (Byte)tagByName.get(lowerCaseName);
			if (num == null) {
				num = ourUnknownTag;
				tagByName.put(lowerCaseName, num);
			}
			tagByName.put(name, num);
		}
		return num.byteValue();
	}
}
