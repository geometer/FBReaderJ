package org.geometerplus.fbreader.book;

/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import java.text.Normalizer;
import java.util.Comparator;

import android.annotation.TargetApi;
import android.os.Build;

public class TitleSort implements Comparator<Book> {
	@Override
	public int compare(Book book1, Book book2) {
		return book1.getSortKey().compareTo(book2.getSortKey());
	}

	private final static String[] ARTICLES = new String[] {
		//English
		"the ", "a ", "an ",
		//French
		"un ", "une ", "le ", "la ", "les ", "du ", "de ", "des ", "l ", "d ",
		//Deutsch
		"das ", "des ", "dem ", "die ", "der ", "den ",
		"ein ", "eine ", "einer ", "einem ", "einen ", "eines "
	};

	public static String trim(String s) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			s = normalize(s);
		}
		final StringBuilder buffer = new StringBuilder();
		boolean afterSpace = false;
		for (int i = 0; i < s.length(); i++) {
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
		for (String a : ARTICLES) {
			if (result.startsWith(a)) {
				return result.substring(a.length());
			}
		}
		return result;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static String normalize(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFKD);
		return s;
	}
}