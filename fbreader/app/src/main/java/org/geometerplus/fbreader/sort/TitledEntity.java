/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.sort;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.os.Build;

import org.fbreader.util.NaturalOrderComparator;

public abstract class TitledEntity<T extends TitledEntity<T>> implements Comparable<T> {
	private static final NaturalOrderComparator ourComparator = new NaturalOrderComparator();

	private String myTitle;
	private String mySortKey;

	public TitledEntity(String title) {
		myTitle = title;
	}

	public String getTitle() {
		return myTitle != null ? myTitle : "";
	}

	public boolean isTitleEmpty() {
		return myTitle == null || "".equals(myTitle);
	}

	public void setTitle(String title) {
		myTitle = title;
		mySortKey = null;
	}

	protected void resetSortKey() {
		mySortKey = null;
	}

	public abstract String getLanguage();

	public String getSortKey() {
		if (null == mySortKey) {
			try {
				mySortKey = trim(myTitle, getLanguage());
			} catch (Throwable t) {
				mySortKey = myTitle;
			}
		}
		return mySortKey;
	}

	@Override
	public int compareTo(T other) {
		return ourComparator.compare(getSortKey(), other.getSortKey());
	}

	private final static Map<String, String[]> ARTICLES = new HashMap<String, String[]>();
	// English articles
	private final static String[] EN_ARTICLES = new String[] {
		"the ", "a ", "an "
	};
	// French articles
	private final static String[] FR_ARTICLES = new String[] {
		"un ", "une ", "le ", "la ", "les ", "du ", "de ",
		"des ", "de la", "l ", "de l "
	};
	// German articles
	private final static String[] GE_ARTICLES = new String[] {
		"das ", "des ", "dem ", "die ", "der ", "den ",
		"ein ", "eine ", "einer ", "einem ", "einen ", "eines "
	};
	// Italian articles
	private final static String[] IT_ARTICLES = new String[] {
		"il ", "lo ", "la ", "l ", "un ", "uno ", "una ",
		"i ", "gli ", "le "
	};
	// Spanish articles
	private final static String[] SP_ARTICLES = new String[] {
		"el ", "la ", "los ", "las ", "un ", "unos ", "una ", "unas "
	};

	static {
		ARTICLES.put("en", EN_ARTICLES);
		ARTICLES.put("fr", FR_ARTICLES);
		ARTICLES.put("de", GE_ARTICLES);
		ARTICLES.put("it", IT_ARTICLES);
		ARTICLES.put("es", SP_ARTICLES);
	}

	private static String trim(String s, String language) {
		if (s == null) {
			return "";
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			s = normalize(s);
		}
		final StringBuilder buffer = new StringBuilder();
		int start = 0;
		if (s.startsWith("M\'") || s.startsWith("Mc")) {
			buffer.append("Mac");
			start = 2;
		}

		boolean afterSpace = false;
		for (int i = start; i < s.length(); ++i) {
			char ch = s.charAt(i);
			// In case it is d' or l', may be it is "I'm", but it's OK.
			if (ch == '\'' || Character.isWhitespace(ch)) {
				ch = ' ';
			}

			switch (Character.getType(ch))	{
				default:
					// we do ignore all other symbols
					break;
				case Character.UPPERCASE_LETTER:
				case Character.TITLECASE_LETTER:
				case Character.OTHER_LETTER:
				case Character.MODIFIER_LETTER:
				case Character.LOWERCASE_LETTER:
				case Character.DECIMAL_DIGIT_NUMBER:
				case Character.LETTER_NUMBER:
				case Character.OTHER_NUMBER:
					buffer.append(Character.toLowerCase(ch));
					afterSpace = false;
					break;
				case Character.SPACE_SEPARATOR:
					if (!afterSpace && buffer.length() > 0) {
						buffer.append(' ');
					}
					afterSpace = true;
					break;
			}
		}

		final String result = buffer.toString();
		if (result.startsWith("a is")) {
			return result;
		}

		if (null != ARTICLES.get(language)) {
			for (String a : ARTICLES.get(language)) {
				if (result.startsWith(a)) {
					return result.substring(a.length());
				}
			}
		}
		return result;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static String normalize(String s) {
		return Normalizer.normalize(s, Normalizer.Form.NFKD);
	}

	public String firstTitleLetter() {
		final String str = getSortKey();
		if (str == null || "".equals(str)) {
			return null;
		}
		return String.valueOf(Character.toUpperCase(str.charAt(0)));
	}
}
