package org.geometerplus.fbreader.network.litres.readers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.geometerplus.fbreader.network.litres.genre.LitResGenre;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;


public class LitresGenreXMLReader extends LitresXMLReader {
	private LinkedList<LitResGenre> myGenresTree = new LinkedList<LitResGenre>();
	private Map<String, LitResGenre> myGenresMap = new HashMap<String, LitResGenre>();
	private LinkedList<LitResGenre> myStack = new LinkedList<LitResGenre>();
	private boolean myDontPopStack;
	
	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if (TAG_GENRE == tag) {
			
			String id = attributes.getValue("id");
			String title = attributes.getValue("title");
			String token = attributes.getValue("token");
			String idStr = "";
			if(id != null){
				idStr = id;
			}
			String titleStr = "";
			if(title != null){
				titleStr = title;
			}
			String tokenStr = "";
			if(token != null){
				tokenStr = token;
			}
			
			saveGenre(new LitResGenre(idStr, titleStr), tokenStr);
		}
		return false;
	}
	@Override
	public boolean endElementHandler(String tag) {
		if (TAG_GENRE == tag) {
			if (!myDontPopStack) {
				if(myStack.size()>0){
					myStack.pop();
				}
			}
			myDontPopStack = false;
		}
		return false;
	}
	
	private void saveGenre(LitResGenre genre, String token) {
		if (myStack.isEmpty()) {
			myGenresTree.push(genre);
		} else {
			myStack.getLast().Children.push(genre);
		}
		if (genre.Id.isEmpty()) {
			myStack.push(genre);
		} else {
			myDontPopStack = true;
			if (!token.isEmpty()) {
				myGenresMap.put(token, genre);
			}
		}
		System.out.println("myGenresMap: "+myGenresMap.size()+", "+myGenresTree.size());
	}
	
	public LinkedList<LitResGenre> getStack(){
		return myStack;
	}
	
	public Map<String, LitResGenre> getGenresMap(){
		return myGenresMap;
	}
	
	public LinkedList<LitResGenre> getGenresTree(){
		return myGenresTree;
	}
}
