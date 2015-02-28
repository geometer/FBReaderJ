/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.network.urlInfo;

import java.util.*;
import java.io.Serializable;

public class UrlInfoCollection<T extends UrlInfo> implements Serializable {
	private static final long serialVersionUID = -834589080548958222L;

	private final LinkedList<T> myInfos = new LinkedList<T>();

	public UrlInfoCollection(T ... elements) {
		for (T info : elements) {
			addInfo(info);
		}
	}

	public UrlInfoCollection(UrlInfoCollection<? extends T> other) {
		myInfos.addAll(other.myInfos);
	}

	public void upgrade(UrlInfoCollection<? extends T> other) {
		myInfos.removeAll(other.myInfos);
		myInfos.addAll(other.myInfos);
	}

	public void addInfo(T info) {
		if (info != null && info.InfoType != null) {
			myInfos.add(info);
		}
	}

	public T getInfo(UrlInfo.Type type) {
		for (T info : myInfos) {
			if (info.InfoType == type) {
				return info;
			}
		}
		return null;
	}

	public List<T> getAllInfos() {
		return Collections.unmodifiableList(myInfos);
	}

	public List<T> getAllInfos(UrlInfo.Type type) {
		List<T> list = null;
		for (T info : myInfos) {
			if (info.InfoType == type) {
				if (list == null) {
					list = new LinkedList<T>();
				}
				list.add(info);
			}
		}
		return list != null ? list : Collections.<T>emptyList();
	}

	public String getUrl(UrlInfo.Type type) {
		final T info = getInfo(type);
		return info != null ? info.Url : null;
	}

	public void clear() {
		myInfos.clear();
	}

	public void removeAllInfos(UrlInfo.Type type) {
		List<T> list = null;
		for (T info : myInfos) {
			if (info.InfoType == type) {
				if (list == null) {
					list = new LinkedList<T>();
				}
				list.add(info);
			}
		}

		if (list != null) {
			myInfos.removeAll(list);
		}
	}

	public boolean isEmpty() {
		return myInfos.isEmpty();
	}
}
