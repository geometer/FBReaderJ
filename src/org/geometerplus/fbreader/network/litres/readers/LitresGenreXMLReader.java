/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network.litres.readers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.geometerplus.fbreader.network.litres.genre.LitresGenre;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;


public class LitresGenreXMLReader extends LitresXMLReader {
	private LinkedList<LitresGenre> myGenresTree = new LinkedList<LitresGenre>();
	private Map<String, LitresGenre> myGenresMap = new HashMap<String, LitresGenre>();
	private LinkedList<LitresGenre> myStack = new LinkedList<LitresGenre>();
	private boolean myDontPopStack;
	private int topGenreIndex = 0;
	
	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if (TAG_GENRE == tag) {
			
			String id = attributes.getValue("id");
			String title = attributes.getValue("title");
			String token = attributes.getValue("token");
			String idStr = "";
			boolean isParent = false;
			if(id != null){
				idStr = id;
			}else{
				isParent = true;
				topGenreIndex++;
				idStr = "0"+topGenreIndex;
			}
			String titleStr = "";
			if(title != null){
				titleStr = title;
			}
			String tokenStr = "";
			if(token != null){
				tokenStr = token;
			}
			
			saveGenre(new LitresGenre(idStr, titleStr, isParent), tokenStr);
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
	
	private void saveGenre(LitresGenre genre, String token) {
		if (myStack.isEmpty()) {
			myGenresTree.push(genre);
		} else {
			myStack.getLast().getChildren().push(genre);
		}
		if (genre.isParent()) {
			myStack.push(genre);
		} else {
			myDontPopStack = true;
			if (!token.isEmpty()) {
				myGenresMap.put(token, genre);
			}
		}
	}
	
	public LinkedList<LitresGenre> getStack(){
		return myStack;
	}
	
	public Map<String, LitresGenre> getGenresMap(){
		return myGenresMap;
	}
	
	public LinkedList<LitresGenre> getGenresTree(){
		return myGenresTree;
	}
}
