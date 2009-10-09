package org.test.zlibrary.hyphenation;

import java.util.Arrays;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextTeXHyphenationPattern;

import junit.framework.TestCase;

public class TestTextTeXHyphenationPattern extends TestCase {
	public void testCreateEmptyPattern() {
		int length = 0;
		char[] pattern = new char[length];
		ZLTextTeXHyphenationPattern p = new ZLTextTeXHyphenationPattern(pattern, 0, length, true);
		assertEquals(p.getLength(), 0);
		assertEquals(Arrays.toString(p.getSymbols()), "[]");
		assertEquals(Arrays.toString(p.getValues()), "[0]");
	}
	
	public void testCreatePatternEnglish() {
		int length = 3;
		char[] pattern = new char[length];
		pattern[0] = 'a';
		pattern[1] = '2';
		pattern[2] = 'b';
		ZLTextTeXHyphenationPattern p = new ZLTextTeXHyphenationPattern(pattern, 0, length, true);
		assertEquals(p.getLength(), 2);
		assertEquals(Arrays.toString(p.getSymbols()), "[a, b]");
		assertEquals(Arrays.toString(p.getValues()), "[0, 2, 0]");
	}
	
	public void testCreatePatternRussian() {
		int length = 3;
		char[] pattern = new char[length];
		pattern[0] = 'à';
		pattern[1] = '2';
		pattern[2] = 'á';
		ZLTextTeXHyphenationPattern p = new ZLTextTeXHyphenationPattern(pattern, 0, length, true);
		assertEquals(p.getLength(), 2);
		assertEquals(Arrays.toString(p.getSymbols()), "[à, á]");
		assertEquals(Arrays.toString(p.getValues()), "[0, 2, 0]");
	}
}
