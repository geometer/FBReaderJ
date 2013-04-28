package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.atom.ATOMCategory;
import org.geometerplus.fbreader.network.litres.readers.LitresEntry;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public class LitresBookItem extends NetworkBookItem {

	LitresBookItem(INetworkLink link, String id, int index,
			CharSequence title, CharSequence summary, List<AuthorData> authors,
			List<String> tags, String seriesTitle, float indexInSeries,
			UrlInfoCollection<?> urls) {
		super(link, id, index, title, summary, authors, tags, seriesTitle,
				indexInSeries, urls);
	}
	
	public LitresBookItem(INetworkLink link, LitresEntry entry, String baseUrl, int index) {
		this(
			link, entry.Id.Uri, index,
			entry.Title, getAnnotation(entry),
			entry.myAuthors, getTags(entry),
			entry.SeriesTitle, entry.SeriesIndex,
			entry.myUrls
		);
	}
	 
	private static List<String> getTags(LitresEntry entry) {
			final LinkedList<String> tags = new LinkedList<String>();
			for (ATOMCategory category : entry.Categories) {
				String label = category.getLabel();
				if (label == null) {
					label = category.getTerm();
				}
				if (label != null) {
					tags.add(label);
				}
			}
			return tags;
	}
	 
	private static CharSequence getAnnotation(LitresEntry entry) {
			if (entry.Content != null) {
				return entry.Content;
			}
			if (entry.Summary != null) {
				return entry.Summary;
			}
			return null;
	}
}
