package org.zlibrary.text.hyphenation;

import java.util.*;
import org.zlibrary.core.util.*;

public class ZLTextHyphenationInfo {
	private ArrayList myMask;

	public ZLTextHyphenationInfo(int length) {
		myMask = new ArrayList(length - 1);
		for (int i = 0; i < myMask.size(); i++) {
			myMask.set(i, false);
		}
	}

	public boolean isHyphenationPossible(int position) {
		return (position < myMask.size() && ((Boolean) myMask.get(position)));
	}

	public ArrayList getMask() {
		return myMask;
	}
}
