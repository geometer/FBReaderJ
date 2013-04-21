package org.geometerplus.fbreader.network.litres;

import java.util.List;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public class LitreBookItem extends NetworkBookItem {

	public LitreBookItem(LitresNetworkLink link, String id, int index,
			CharSequence title, CharSequence summary, List<AuthorData> authors,
			List<String> tags, String seriesTitle, float indexInSeries,
			UrlInfoCollection<?> urls) {
		super(link, id, index, title, summary, authors, tags, seriesTitle,
				indexInSeries, urls);
		// TODO Auto-generated constructor stub
	}

}
