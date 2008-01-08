package org.zlibrary.text.view.impl;

import java.util.*;
import org.zlibrary.core.util.*;

final class ZLTextLineInfoCache extends HashMap {
	public void put(ZLTextLineInfo info) {
		super.put(info, info);
	}

	public ZLTextLineInfo getInfo(ZLTextLineInfo info) {
		return (ZLTextLineInfo)super.get(info);
	}
}
