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
		for(char letter = 'Я'; letter >= 'А';letter--){
			//char letterCh[] = {letter};
			e = new LitresAuthor(new String(new char []{letter}));
			e.description = "Авторы на букву "+e.lastName;
			myAuthorsTree.push(e);
		}
	}
}
