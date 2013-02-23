package org.geometerplus.fbreader.network.rss;

import org.geometerplus.fbreader.network.atom.ATOMEntry;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class RSSItem extends ATOMEntry {
	
	public String SeriesTitle;
	public float SeriesIndex;
	
	protected RSSItem(ZLStringMap attributes) {
		super(attributes);
	}
}
