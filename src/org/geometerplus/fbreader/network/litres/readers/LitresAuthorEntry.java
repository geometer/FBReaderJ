package org.geometerplus.fbreader.network.litres.readers;

import org.geometerplus.fbreader.network.atom.ATOMId;
import org.geometerplus.fbreader.network.litres.author.LitresAuthor;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresAuthorEntry extends LitresEntry {
	public LitresAuthor authorData;
	protected LitresAuthorEntry(ZLStringMap source) {
		super(source);
		
		this.authorData = new LitresAuthor();
		ATOMId myId = new ATOMId();
		myId.Uri = source.getValue("id");
		this.Id = myId;
		
	}

}
