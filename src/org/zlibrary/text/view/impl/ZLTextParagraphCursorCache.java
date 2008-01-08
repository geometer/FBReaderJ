package org.zlibrary.text.view.impl;

import java.lang.ref.WeakReference;
import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.text.model.ZLTextModel;

class ZLTextParagraphCursorCache {
	private final static class Key {
		private final ZLTextModel myModel;
		private final int myIndex;

		public Key(ZLTextModel model, int index) {
			myModel = model;
			myIndex = index;
		}

		public boolean equals(Object o) {
			Key k = (Key)o;
			return (myModel == k.myModel) && (myIndex == k.myIndex);
		}

		public int hashCode() {
			return myModel.hashCode() + myIndex;
		}
	}

	private static final HashMap ourMap = new HashMap();

	public static void put(ZLTextModel model, int index, ZLTextParagraphCursor cursor) {
		ourMap.put(new Key(model, index), new WeakReference(cursor));
	}

	public static ZLTextParagraphCursor get(ZLTextModel model, int index) {
		WeakReference ref = (WeakReference)ourMap.get(new Key(model, index));
		return (ref != null) ? (ZLTextParagraphCursor)ref.get() : null;
	}

	public static void clear() {
		ourMap.clear();
	}
}
