/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.util;

public abstract class ZLSearchUtil {
	private ZLSearchUtil() {
	}

	public static final class Result {
		public final int Start;
		public final int Length;

		Result(int start, int length) {
			Start = start;
			Length = length;
		}
	}

	public static Result find(char[] text, int offset, int length, final ZLSearchPattern pattern) {
		return find(text, offset, length, pattern, 0);
	}

	public static Result find(char[] text, int offset, int length, final ZLSearchPattern pattern, int pos) {
		if (pos < 0) {
			pos = 0;
		}
		final char[] lower = pattern.LowerCasePattern;
		final int patternLength = lower.length;
		final int end = offset + length;
		final int lastStart = end - patternLength;
		if (pattern.IgnoreCase) {
			final char[] upper = pattern.UpperCasePattern;
			final char firstCharLower = lower[0];
			final char firstCharUpper = upper[0];
			for (int i = offset + pos; i <= lastStart; ++i) {
				final char current = text[i];
				if (current == firstCharLower || current == firstCharUpper) {
					int j = 1;
					int k = i + 1;
					for (; j < patternLength; ++k) {
						final char symbol = text[k];
						if (symbol == '\u200b') {
							if (patternLength - j > end - k) {
								break;
							} else {
								continue;
							}
						}
						if (lower[j] != symbol && upper[j] != symbol) {
							break;
						}
						++j;
					}
					if (j == patternLength) {
						return new Result(i - offset, k - i);
					}
				}
			}
		} else {
			final char firstChar = lower[0];
			for (int i = offset + pos; i <= lastStart; i++) {
				if (text[i] == firstChar) {
					int j = 1;
					int k = i + 1;
					for (; j < patternLength; ++k) {
						final char symbol = text[k];
						if (symbol == '\u200b') {
							if (patternLength - j > end - k) {
								break;
							} else {
								continue;
							}
						}
						if (lower[j] != text[k]) {
							break;
						}
						++j;
					}
					if (j >= patternLength) {
						return new Result(i - offset, k - i);
					}
				}
			}
		}
		return null;
	}
}
