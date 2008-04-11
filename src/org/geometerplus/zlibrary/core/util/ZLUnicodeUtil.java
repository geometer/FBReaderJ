package org.geometerplus.zlibrary.core.util;

public class ZLUnicodeUtil {
	public static int utf8Length(byte[] buffer, int str, int len) {
		int last = str + len;
		int counter = 0;
		while (str < last) {
			if ((buffer[str] & 0x80) == 0) {
				++str;
			} else if ((buffer[str] & 0x20) == 0) {
				str += 2;
			} else if ((buffer[str] & 0x10) == 0) {
				str += 3;
			} else {
				str += 4;
			}
			++counter;
		}
		return counter;
	}
}
