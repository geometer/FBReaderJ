package org.geometerplus.fbreader.network.litres.readers;

import org.geometerplus.fbreader.network.authentication.litres.LitResXMLReader;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;


public class LitresAuthorsXMLReader extends LitResXMLReader {
	protected static final String TAG_MAIN = "main";
	protected static final String TAG_SUBJECT = "subject";
	protected static final String TAG_FIRST_NAME = "first-name";
	protected static final String TAG_LAST_NAME = "last-name";
	
	private LitresAuthorEntry myAuthor;
	
	public LitresAuthorsXMLReader() {
		super();
	}
	
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if(TAG_SUBJECT == tag){
			myAuthor = new LitresAuthorEntry(attributes);
		}
		if (TAG_MAIN == tag) {
			
		}
		return false;
	}
	
	public boolean endElementHandler(String tag) {
		if(TAG_FIRST_NAME == tag){
			myAuthor.authorData.firstName = myBuffer.toString().trim();
		}
		if(TAG_LAST_NAME == tag){
			myAuthor.authorData.lastName = myBuffer.toString().trim();
		}
		if (TAG_SUBJECT == tag) {
			if(myHandler != null){
				//System.out.println("MAIN not null "+myAuthor.toString());
				myHandler.processFeedEntry(myAuthor);
			}
		}
		
		
		myBuffer.delete(0, myBuffer.length());
		return false;
	}
}
