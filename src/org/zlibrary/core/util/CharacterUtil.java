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

	public static char toLowerCase(char ch) {
		if (ch < 'A') return ch;
		if (ch <= 'Z') return (char)(ch + 'a' - 'A');

		// latin1
		if (ch < 0xC0) return ch;
		if (ch == 0xD7) return ch;
		if (ch <= 0xDE) return (char)(ch + 0x20);

		// extended latin1
		if (ch < 0x100) return ch;
		if (ch <= 0x178) return ((ch & 0x01) == 0x01) ? ch : (char)(ch + 1);

		// cyrillic
		if (ch < 0x410) return ch;
		if (ch <= 0x42F) return (char)(ch + 0x20);

		// cyrillic yo
		if (ch == 0x401) return 0x451;

		return ch;
	}
}
