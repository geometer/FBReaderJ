package org.zlibrary.core.image;

import java.util.*;
import org.zlibrary.core.util.*;

public final class ZLImageMap extends HashMap {
	public ZLImage getImage(String id) {
		return (ZLImage)super.get(id);
	}
}
