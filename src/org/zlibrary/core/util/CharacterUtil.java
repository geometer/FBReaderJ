package org.zlibrary.core.util;

public abstract class CharacterUtil {
	public static boolean isLetter(char ch) {
		return
			(('a' <= ch) && (ch <= 'z')) ||
			(('A' <= ch) && (ch <= 'Z')) ||
			// ' is "letter" (in French, for example)
			(ch == '\'') ||
			// ^ is "letter" (in Esperanto)
			(ch == '^') ||
			// latin1
			((0xC0 <= ch) && (ch <= 0xFF) && (ch != 0xD7) && (ch != 0xF7)) ||
			// extended latin1
			((0x100 <= ch) && (ch <= 0x178)) ||
			// cyrillic
			((0x410 <= ch) && (ch <= 0x44F)) ||
			// cyrillic YO & yo
			(ch == 0x401) || (ch == 0x451);
	}
}
