package org.zlibrary.text.view.impl;

import java.util.ArrayList;

final class ZLTextLineInfoVector extends ArrayList {
	ZLTextLineInfo getInfo(int index) {
		return (ZLTextLineInfo)super.get(index);
	}
}
