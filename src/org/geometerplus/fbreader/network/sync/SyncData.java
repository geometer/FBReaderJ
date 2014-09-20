/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.sync;

import java.util.*;

import org.json.simple.JSONValue;

import org.geometerplus.zlibrary.core.options.*;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.IBookCollection;

public class SyncData {
	public final static class ServerBookInfo {
		public final List<String> Hashes;
		public final String Title;
		public final boolean Downloadable;

		private ServerBookInfo(List<String> hashes, String title, boolean downloadable) {
			Hashes = Collections.unmodifiableList(hashes);
			Title = title;
			Downloadable = downloadable;
		}
	}

	private final ZLIntegerOption myGeneration =
		new ZLIntegerOption("SyncData", "Generation", -1);
	private final ZLStringOption myCurrentBookHash =
		new ZLStringOption("SyncData", "CurrentBookHash", "");
	private final ZLStringOption myCurrentBookTimestamp =
		new ZLStringOption("SyncData", "CurrentBookTimestamp", "");
	private final ZLStringListOption myServerBookHashes =
		new ZLStringListOption("SyncData", "ServerBookHashes", Collections.<String>emptyList(), ";");
	private final ZLStringOption myServerBookTitle =
		new ZLStringOption("SyncData", "ServerBookTitle", "");

	private Map<String,Object> position2Map(ZLTextFixedPosition.WithTimestamp pos) {
		final Map<String,Object> map = new HashMap<String,Object>();
		map.put("para", pos.ParagraphIndex);
		map.put("elmt", pos.ElementIndex);
		map.put("char", pos.CharIndex);
		map.put("timestamp", pos.Timestamp);
		return map;
	}

	private ZLTextFixedPosition.WithTimestamp map2Position(Map<String,Object> map) {
		return new ZLTextFixedPosition.WithTimestamp(
			(int)(long)(Long)map.get("para"),
			(int)(long)(Long)map.get("elmt"),
			(int)(long)(Long)map.get("char"),
			(Long)map.get("timestamp")
		);
	}

	private Map<String,Object> positionMap(IBookCollection collection, Book book) {
		if (book == null) {
			return null;
		}
		final ZLTextFixedPosition.WithTimestamp pos = collection.getStoredPosition(book.getId());
		return pos != null ? position2Map(pos) : null;
	}

	public Map<String,Object> data(IBookCollection collection) {
		final Map<String,Object> map = new HashMap<String,Object>();
		map.put("generation", myGeneration.getValue());
		map.put("timestamp", System.currentTimeMillis());

		final Book currentBook = collection.getRecentBook(0);
		if (currentBook != null) {
			final String oldHash = myCurrentBookHash.getValue();
			final String newHash = collection.getHash(currentBook, true);
			if (newHash != null && !newHash.equals(oldHash)) {
				myCurrentBookHash.setValue(newHash);
				if (oldHash.length() != 0) {
					myCurrentBookTimestamp.setValue(String.valueOf(System.currentTimeMillis()));
					myServerBookHashes.setValue(Collections.<String>emptyList());
				}
			}
			final String currentBookHash = newHash != null ? newHash : oldHash;

			final Map<String,Object> currentBookMap = new HashMap<String,Object>();
			currentBookMap.put("hash", currentBookHash);
			currentBookMap.put("title", currentBook.getTitle());
			try {
				currentBookMap.put("timestamp", Long.parseLong(myCurrentBookTimestamp.getValue()));
			} catch (Exception e) {
			}
			map.put("currentbook", currentBookMap);

			final List<Map<String,Object>> lst = new ArrayList<Map<String,Object>>();
			if (positionOption(currentBookHash).getValue().length() == 0) {
				final Map<String,Object> posMap = positionMap(collection, currentBook);
				if (posMap != null) {
					posMap.put("hash", currentBookHash);
					lst.add(posMap);
				}
			}
			if (!currentBookHash.equals(oldHash) &&
				positionOption(oldHash).getValue().length() == 0) {
				final Map<String,Object> posMap =
					positionMap(collection, collection.getBookByHash(oldHash));
				if (posMap != null) {
					posMap.put("hash", oldHash);
					lst.add(posMap);
				}
			}
			if (lst.size() > 0) {
				map.put("positions", lst);
			}
		}

		System.err.println("DATA = " + map);
		return map;
	}

	public boolean updateFromServer(Map<String,Object> data) {
		System.err.println("RESPONSE = " + data);
		myGeneration.setValue((int)(long)(Long)data.get("generation"));

		final List<Map> positions = (List<Map>)data.get("positions");
		if (positions != null) {
			for (Map<String,Object> map : positions) {
				final ZLTextFixedPosition.WithTimestamp pos = map2Position(map);
				for (String hash : (List<String>)map.get("all_hashes")) {
					savePosition(hash, pos);
				}
			}
		}

		final Map<String,Object> currentBook = (Map<String,Object>)data.get("currentbook");
		if (currentBook != null) {
			myServerBookHashes.setValue((List<String>)currentBook.get("all_hashes"));
			myServerBookTitle.setValue((String)currentBook.get("title"));
		} else {
			myServerBookHashes.setValue(Collections.<String>emptyList());
		}

		return data.size() > 1;
	}

	private ZLStringOption positionOption(String hash) {
		return new ZLStringOption("SyncData", "Pos:" + hash, "");
	}

	private void savePosition(String hash, ZLTextFixedPosition.WithTimestamp pos) {
		positionOption(hash).setValue(pos != null ? JSONValue.toJSONString(position2Map(pos)) : "");
	}

	public boolean hasPosition(String hash) {
		return positionOption(hash).getValue().length() > 0;
	}

	public ServerBookInfo getServerBookInfo() {
		final List<String> hashes = myServerBookHashes.getValue();
		if (hashes.size() == 0) {
			return null;
		}
		return new ServerBookInfo(
			// TODO: get downloadable info from server
			hashes, myServerBookTitle.getValue(), true
		);
	}

	public ZLTextFixedPosition.WithTimestamp getAndCleanPosition(String hash) {
		final ZLStringOption option = positionOption(hash);
		try {
			return map2Position((Map)JSONValue.parse(option.getValue()));
		} catch (Throwable t) {
			return null;
		} finally {
			option.setValue("");
		}
	}

	public void reset() {
		Config.Instance().removeGroup("SyncData");
	}
}
