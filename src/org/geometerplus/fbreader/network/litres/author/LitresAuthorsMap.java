package org.geometerplus.fbreader.network.litres.author;

import java.util.LinkedList;

public class LitresAuthorsMap {
	private static LitresAuthorsMap ourInstance;
	boolean myInitialized;
	LinkedList<LitresAuthor> myAuthorsTree = new LinkedList<LitresAuthor>();
	 
	public static LitresAuthorsMap Instance() {
		if (ourInstance == null) {
			ourInstance = new LitresAuthorsMap();
		}
		return ourInstance;
	}

	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance = null;
		}
	}
	
	private LitresAuthorsMap() {
		buildInitAuthors();
	}
	
	public LinkedList<LitresAuthor> authorsTree(){
		return myAuthorsTree;
	}
	
	private void buildInitAuthors(){
		LitresAuthor e = new LitresAuthor();
		//TODO: Refactor
		for(char alphabet = 'Z'; alphabet >= 'A';alphabet--){
			char rr[] = {alphabet};
			e = new LitresAuthor(new String(rr));
			myAuthorsTree.push(e);
		}
		for(char alphabet2 = 'Я'; alphabet2 >= 'А';alphabet2--){
			char rr[] = {alphabet2};
			e = new LitresAuthor(new String(rr));
			myAuthorsTree.push(e);
		}
	}
}
