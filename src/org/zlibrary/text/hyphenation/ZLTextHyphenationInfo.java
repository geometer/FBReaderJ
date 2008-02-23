package org.zlibrary.text.hyphenation;

import java.util.*;
import org.zlibrary.core.util.*;

public class ZLTextHyphenationInfo {
	private boolean[] myMask;

	public ZLTextHyphenationInfo(int length) {
		myMask = new boolean[length - 1];
		for (int i = 0; i < myMask.length; i++) {
			myMask[i] = false;
		}
	}

	public boolean isHyphenationPossible(int position) {
		return (position < myMask.length && myMask[position]);
	}

	public boolean[] getMask() {
		return myMask;
	}
}
