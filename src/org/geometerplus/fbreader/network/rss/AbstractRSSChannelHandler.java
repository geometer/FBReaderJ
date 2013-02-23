package org.geometerplus.fbreader.network.rss;

import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public abstract class AbstractRSSChannelHandler implements ATOMFeedHandler<RSSChannelMetadata, RSSItem> {
	public RSSChannelMetadata createFeed(ZLStringMap attributes) {
		return new RSSChannelMetadata(attributes);
	}

	public RSSItem createEntry(ZLStringMap attributes) {
		return new RSSItem(attributes);
	}

	public RSSLink createLink(ZLStringMap attributes) {
		return new RSSLink(attributes);
	}
}
