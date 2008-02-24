package org.zlibrary.text.hyphenation;

import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.text.view.impl.ZLTextWord; 

public abstract class ZLTextHyphenator {
	protected static ZLTextHyphenator ourInstance;
	
	public static ZLTextHyphenator getInstance() {
		if (ourInstance == null) {
			ourInstance = new ZLTextTeXHyphenator();
		}
		return ourInstance;
	}
	

	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance.unload();
			ourInstance = null;
		}
	}

	protected ZLTextHyphenator() {
	}

	public abstract void load(final String language);
	public abstract void unload();

	public ZLTextHyphenationInfo getInfo(final ZLTextWord word) {
		final int len = word.Length;
		final boolean[] isLetter = new boolean[len];
		final char[] pattern = new char[len + 2];
		final char[] data = word.Data;
		pattern[0] = ' ';
		for (int i = 0, j = word.Offset; i < len; ++i, ++j) {
			char symbol = data[j];
			boolean flag = CharacterUtil.isLetter(symbol);
			isLetter[i] = flag;
			pattern[i + 1] = flag ? Character.toLowerCase(symbol) : ' ';
		}
		pattern[len + 1] = ' ';

		ZLTextHyphenationInfo info = new ZLTextHyphenationInfo(word.Length + 2);
		hyphenate(pattern, info.getMask(), word.Length + 2);
		for (int i = 0; i < word.Length + 1; i++) {
			if ((i < 2) || (i > word.Length - 2)) {
				info.getMask()[i] = false;
			} else if (word.Data[word.Offset + i - 1] == '-') {
				info.getMask()[i] = (i >= 3)
					&& isLetter[i - 3] 
					&& isLetter[i - 2] 
					&& isLetter[i] 
					&& isLetter[i + 1];
			} else {
				info.getMask()[i] = info.getMask()[i] 
					&& isLetter[i - 2] 
					&& isLetter[i - 1] 
					&& isLetter[i] 
					&& isLetter[i + 1];
			}
		}

		return info;
	}

	protected abstract void hyphenate(char[] stringToHyphenate, boolean[] mask, int length);
}
