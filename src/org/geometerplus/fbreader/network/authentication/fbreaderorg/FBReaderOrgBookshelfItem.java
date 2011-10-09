package org.geometerplus.fbreader.network.authentication.fbreaderorg;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.opds.OPDSCatalogItem;
import org.geometerplus.fbreader.network.opds.OPDSNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public class FBReaderOrgBookshelfItem extends OPDSCatalogItem {
	public FBReaderOrgBookshelfItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		super((OPDSNetworkLink)link, title, summary, urls, Accessibility.SIGNED_IN, FLAGS_DEFAULT, null);
	}
}