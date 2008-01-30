package org.zlibrary.text.view.impl;

import java.util.*;
import org.zlibrary.core.util.*;

final class ZLTextLineInfoVector extends ArrayList {
	ZLTextLineInfo getInfo(int index) {
		return (ZLTextLineInfo)super.get(index);
	}
}
