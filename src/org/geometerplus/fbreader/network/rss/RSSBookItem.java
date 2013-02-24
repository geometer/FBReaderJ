package org.geometerplus.fbreader.network.rss;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.atom.ATOMAuthor;
import org.geometerplus.fbreader.network.atom.ATOMCategory;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;

public class RSSBookItem extends NetworkBookItem {
	
	public RSSBookItem(RSSNetworkLink link, String id, int index,
			CharSequence title, CharSequence summary,
			List<AuthorData> authors, List<String> tags,
			String seriesTitle, float indexInSeries,
			UrlInfoCollection<?> urls) {
			super(link, id, index,
				title, summary,
				authors, tags,
				seriesTitle, indexInSeries,
				urls
			);
		}
	
	RSSBookItem(RSSNetworkLink networkLink, RSSItem entry, String baseUrl, int index) {
		this(networkLink, entry.Id.Uri, index,
			entry.Title, getAnnotation(entry),
			getAuthors(entry), getTags(entry), null, 0, null
		);
	}
	
	private static CharSequence getAnnotation(RSSItem entry) {
		if (entry.Content != null) {
			return entry.Content;
		}
		if (entry.Summary != null) {
			return entry.Summary;
		}
		return null;
	}
	
	private static List<AuthorData> getAuthors(RSSItem entry) {
		final LinkedList<AuthorData> authors = new LinkedList<AuthorData>();
		for (ATOMAuthor author: entry.Authors) {
			authors.add(new AuthorData(author.Name, null));
		}
		return authors;
	}
	
	private static List<String> getTags(RSSItem entry) {
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
}
