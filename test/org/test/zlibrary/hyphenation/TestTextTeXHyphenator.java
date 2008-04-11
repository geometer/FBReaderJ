package org.test.zlibrary.hyphenation;

import java.util.Arrays;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextTeXHyphenator;

import junit.framework.TestCase;

public class TestTextTeXHyphenator extends TestCase {

        public void testTrivialHyphenation() {
		ZLTextTeXHyphenator hyphenator = new ZLTextTeXHyphenator();
		hyphenator.load("en", "./data/hyphenationPatterns/en.pattern");
		String s = "short";
		int length = s.length();
		boolean[] mask = new boolean[length];
		char[] buf = s.toCharArray();
		hyphenator.hyphenate(buf, mask, length);
   		boolean[] right = new boolean[length];
		assertEquals(Arrays.toString(mask), Arrays.toString(right));
	}
        
	public void testLongWordHyphenation() {
		ZLTextTeXHyphenator hyphenator = new ZLTextTeXHyphenator();
		hyphenator.load("en", "./data/hyphenationPatterns/en.pattern");
		String s = "internationalization";
		int length = s.length();
		boolean[] mask = new boolean[length];
		char[] buf = s.toCharArray();
		hyphenator.hyphenate(buf, mask, length);
   		boolean[] right = new boolean[length];
  		right[1] = true;
  		right[4] = true;
  		right[6] = true;
  		right[10] = true;
  		right[12] = true;
  		right[15] = true;
		assertEquals(Arrays.toString(mask), Arrays.toString(right));
	}

	public void testHyphenationWithHyphen() {
		ZLTextTeXHyphenator hyphenator = new ZLTextTeXHyphenator();
		hyphenator.load("en", "./data/hyphenationPatterns/en.pattern");
		String s = "grif-fon";
		int length = s.length();
		boolean[] mask = new boolean[length];
		char[] buf = s.toCharArray();
		hyphenator.hyphenate(buf, mask, length);
   		boolean[] right = new boolean[length];
		right[4] = true;
		assertEquals(Arrays.toString(mask), Arrays.toString(right));

   	}

}
