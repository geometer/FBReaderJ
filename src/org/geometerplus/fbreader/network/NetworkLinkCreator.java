package org.geometerplus.fbreader.network;

import org.geometerplus.fbreader.network.opds.OPDSPredefinedNetworkLink;
import org.geometerplus.fbreader.network.rss.RSSNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoWithDate;

public class NetworkLinkCreator {
	
	public NetworkLinkCreator() {
	}
	
	public AbstractNetworkLink createOPDSLink(int id, String predifinedId, String siteName, CharSequence title, CharSequence summary, String language, UrlInfoCollection<UrlInfoWithDate> infos){
		return new OPDSPredefinedNetworkLink(
				id,
				predifinedId,
				siteName,
				title.toString(),
				summary != null ? summary.toString() : null,
				language,
				infos
			);
	}
	
	public AbstractNetworkLink createRSSLink(int id, String siteName, CharSequence title, CharSequence summary, String language, UrlInfoCollection<UrlInfoWithDate> infos){
		return new RSSNetworkLink(
				id,
				siteName,
				title.toString(),
				summary != null ? summary.toString() : null,
				language,
				infos
			);
	}
}
