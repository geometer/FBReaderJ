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

package org.geometerplus.fbreader.network.litres.genre;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.geometerplus.fbreader.network.authentication.litres.LitResNetworkRequest;
import org.geometerplus.fbreader.network.litres.LitresUtil;
import org.geometerplus.fbreader.network.litres.readers.LitresGenreXMLReader;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

public class LitResGenreMap {
	private static LitResGenreMap ourInstance;
	boolean myInitialized;
	LinkedList<LitResGenre> myGenresTree = new LinkedList<LitResGenre>();
	Map<String, LitResGenre> myGenresMap = new HashMap<String, LitResGenre>();;
	Map<LitResGenre, String> myGenresTitles = new HashMap<LitResGenre, String>();
	
	public static LitResGenreMap Instance() {
		if (ourInstance == null) {
			ourInstance = new LitResGenreMap();
		}
		return ourInstance;
	}

	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance = null;
		}
	}
	
	private LitResGenreMap() {
	}
	

	public Map<String, LitResGenre> genresMap() throws ZLNetworkException {
		validateGenres();
		return myGenresMap;
	}

	public LinkedList<LitResGenre> genresTree() throws ZLNetworkException {
		validateGenres();
		return myGenresTree;
	}

	public Map<LitResGenre, String> genresTitles() throws ZLNetworkException {
		validateGenres();
		return myGenresTitles;
	}
	

	private void validateGenres() throws ZLNetworkException {
		if (!myInitialized) {
			if (loadGenres()) {
				buildGenresTitles(myGenresTree, "");
				myInitialized = true;
			}
		}
	}
	
	boolean loadGenres() throws ZLNetworkException {
		
		final String url = LitresUtil.url("pages/catalit_genres/");
		
		myGenresTree.clear();
		myGenresMap.clear();
		myGenresTitles.clear();
		
		try {
			final LitresGenreXMLReader xmlReader = new LitresGenreXMLReader();
			final LitResNetworkRequest request = new LitResNetworkRequest(url, xmlReader);
			ZLNetworkManager.Instance().perform(request);
			myGenresTree = xmlReader.getGenresTree();
			myGenresMap = xmlReader.getGenresMap();
		} catch (ZLNetworkException e) {
			throw e;
		}
		
		return true;
	}
	
	void buildGenresTitles(final LinkedList<LitResGenre> genres, final String titlePrefix) {
		for(LitResGenre genre : genres){
			String title = titlePrefix.equals("") ? (genre.Title) : (titlePrefix + "/" + genre.Title);
			if (genre.Id.equals("")) {
				buildGenresTitles(genre.Children, title);
			} else {
				myGenresTitles.put(genre, title);
			}
		}
	}
}
