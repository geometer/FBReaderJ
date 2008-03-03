package org.test.zlibrary.hyphenation;

import java.util.Arrays;

import org.zlibrary.text.hyphenation.ZLTextTeXHyphenator;

import junit.framework.TestCase;

public class TestTextTeXHyphenator extends TestCase {

        public void testAddPattern() {
		ZLTextTeXHyphenator hyphenator = new ZLTextTeXHyphenator();
		hyphenator.load("en");
		String s = "griffon";
		int length = s.length();
		boolean[] mask = new boolean[length];
		char[] buf = s.toCharArray();
		hyphenator.hyphenate(buf, mask, length);
		System.err.println(Arrays.toString(mask));
   	}
}
