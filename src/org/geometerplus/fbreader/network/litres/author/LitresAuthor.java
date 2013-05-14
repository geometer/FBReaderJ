package org.geometerplus.fbreader.network.litres.author;

import org.geometerplus.fbreader.network.atom.ATOMAuthor;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresAuthor extends ATOMAuthor {
	public String firstName = "";
	public String lastName = "";
	public String artsCount = "";
	public String description = "";
	
	public LitresAuthor(){
		this(new ZLStringMap());
	}
	
	public LitresAuthor(String lastName){
		this(new ZLStringMap());
		this.lastName = lastName;
	}
	
	protected LitresAuthor(ZLStringMap source) {
		super(source);
	}
}
