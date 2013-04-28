package org.geometerplus.fbreader.network.litres.readers;

import java.util.LinkedList;

import org.geometerplus.fbreader.network.atom.ATOMEntry;
import org.geometerplus.fbreader.network.litres.LitresBookItem;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresEntry extends ATOMEntry {
	public String SeriesTitle;
	public float SeriesIndex;
	public UrlInfoCollection<UrlInfo> myUrls = new UrlInfoCollection<UrlInfo>();
	public String myBookId;
	public String myGenre;
	public LinkedList<LitresBookItem.AuthorData> myAuthors = new LinkedList<LitresBookItem.AuthorData>();
	protected LitresEntry(ZLStringMap source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

}
