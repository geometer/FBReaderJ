/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.formats.html;

import java.util.*;

public final class HtmlTag {
	public static final byte UNKNOWN = 0;
	public static final byte HTML = 1;
	public static final byte HEAD = 2;
	public static final byte BODY = 3;
	public static final byte TITLE = 4;
	public static final byte P = 5;
	public static final byte H1 = 6;
	public static final byte H2 = 7;
	public static final byte H3 = 8;
	public static final byte H4 = 9;
	public static final byte H5 = 10;
	public static final byte H6 = 11;
	public static final byte A = 12;
	public static final byte B = 13;
	public static final byte I = 14;
	public static final byte BR = 15;
	public static final byte STRONG = 16;
	public static final byte IMG = 17;
	public static final byte SCRIPT = 18;
	public static final byte OL = 19;
	public static final byte UL = 20;
	public static final byte LI = 21;
	public static final byte SELECT = 22;
	public static final byte DIV = 23;
	public static final byte TR = 24;
	public static final byte STYLE = 25;
	
	public static final byte S = 26;
	public static final byte SUB = 27;
	public static final byte SUP = 28;
	public static final byte PRE = 29;
	public static final byte CODE = 30;
	public static final byte EM = 31;
	public static final byte DFN = 32;
	public static final byte CITE = 33;

	public static final byte HR = 34;
	
	// mobipocket specific tags
	public static final byte REFERENCE = 35;
	public static final byte GUIDE = 36;

	public static final byte TAG_NUMBER = 37;
	
	private static final HashMap<String,Byte> ourTagByName = new HashMap<String,Byte>(256, 0.2f);
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
		ourTagByName.put("s", S);
		ourTagByName.put("sub", SUB);
		ourTagByName.put("sup", SUP);
		ourTagByName.put("pre", PRE);
		ourTagByName.put("code", CODE);
		ourTagByName.put("em", EM);
		ourTagByName.put("def", DFN);
		ourTagByName.put("cite", CITE);
		ourTagByName.put("hr", HR);
		ourTagByName.put("guide", GUIDE);
		ourTagByName.put("reference", REFERENCE);
	}

	public static byte getTagByName(String name) {
		final HashMap<String,Byte> tagByName = ourTagByName;
		Byte num = tagByName.get(name);
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
