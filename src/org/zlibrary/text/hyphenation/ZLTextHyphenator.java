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
		StringBuffer pattern = new StringBuffer();
		ArrayList isLetter = new ArrayList();
		pattern.append(' ');
		for (int i = word.Offset; i < word.Offset + word.Length; i++) {
			char symbol = word.Data[i];
			boolean letter = Character.isLetter(symbol);
			isLetter.add(letter);
			pattern.append(letter ? Character.toLowerCase(symbol) : ' ');
		}
		pattern.append(' ');

		ZLTextHyphenationInfo info = new ZLTextHyphenationInfo(word.Length + 2);
		hyphenate(pattern, info.getMask(), word.Length + 2);
		for (int i = 0; i < word.Length + 1; i++) {
			if ((i < 2) || (i > word.Length - 2)) {
				info.getMask().set(i, false);
			} else if (word.Data[word.Offset + i - 1] == '-') {
				info.getMask().set(i, (i >= 3)
					&& (Boolean) isLetter.get(i - 3) 
					&& (Boolean) isLetter.get(i - 2) 
					&& (Boolean) isLetter.get(i) 
					&& (Boolean) isLetter.get(i + 1));
			} else {
				info.getMask().set(i, (Boolean) info.getMask().get(i) 
					&& (Boolean) isLetter.get(i - 2) 
					&& (Boolean) isLetter.get(i - 1) 
					&& (Boolean) isLetter.get(i) 
					&& (Boolean) isLetter.get(i + 1));
			}
		}

		return info;
	}

	public abstract String getBreakingAlgorithm();

	protected abstract void hyphenate(StringBuffer ucs2String, ArrayList mask, int length);
}
