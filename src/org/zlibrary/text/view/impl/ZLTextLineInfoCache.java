package org.zlibrary.text.view.impl;

import java.util.HashMap;

final class ZLTextLineInfoCache extends HashMap<ZLTextLineInfo,ZLTextLineInfo> {
	public void put(ZLTextLineInfo info) {
		super.put(info, info);
	}
}
