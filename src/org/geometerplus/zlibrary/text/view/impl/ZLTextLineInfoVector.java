package org.geometerplus.zlibrary.text.view.impl;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

final class ZLTextLineInfoVector extends ArrayList {
	ZLTextLineInfo getInfo(int index) {
		return (ZLTextLineInfo)super.get(index);
	}
}
