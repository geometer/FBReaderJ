/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;

public abstract class AbstractNetworkLink implements INetworkLink {
	protected String mySiteName;
	protected String myTitle;
	protected String mySummary;
	protected final String myLanguage;
	protected final TreeMap<String,UrlInfo> myInfos;

	private boolean mySupportsBasket;
	private final ZLStringListOption myBooksInBasketOption;

	/**
	 * Creates new NetworkLink instance.
	 *
	 * @param siteName   name of the corresponding website. Must be not <code>null</code>.
	 * @param title      title of the corresponding library item. Must be not <code>null</code>.
	 * @param summary    description of the corresponding library item. Can be <code>null</code>.
	 * @param language   language of the catalog. If <code>null</code> we assume this catalog is multilanguage.
	 * @param infos      map contains URL infos with their identifiers; must always contain one URL with <code>URL_MAIN</code> identifier
	 */
	public AbstractNetworkLink(String siteName, String title, String summary, String language, Map<String,UrlInfo> infos) {
		mySiteName = siteName;
		myTitle = title;
		mySummary = summary;
		myLanguage = language != null ? language : "multi";
		myInfos = new TreeMap<String,UrlInfo>(infos);
		myBooksInBasketOption = new ZLStringListOption(siteName, "Basket", null);
	}

	public final String getSiteName() {
		return mySiteName;
	}

	public final String getTitle() {
		return myTitle;
	}

	public final String getSummary() {
		return mySummary;
	}

	public final String getLanguage() {
		return myLanguage;
	}

	public final HashMap<String,UrlInfo> urlInfoMap() {
		return new HashMap(myInfos);
	}

	public final UrlInfo getUrlInfo(String urlKey) {
		final UrlInfo info = myInfos.get(urlKey);
		return info != null ? info : UrlInfo.NULL;
	}

	public final Set<String> getUrlKeys() {
		return myInfos.keySet();
	}

	public final void setSupportsBasket() {
		mySupportsBasket = true;
	}

	public final boolean supportsBasket() {
		return mySupportsBasket;
	}

	public final void addToBasket(NetworkBookItem book) {
		if (supportsBasket() && book.Id != null && !"".equals(book.Id)) {
			List<String> ids = myBooksInBasketOption.getValue();
			if (!ids.contains(book.Id)) {
				ids = new ArrayList(ids);
				ids.add(book.Id);
				myBooksInBasketOption.setValue(ids);
			}
		}
	}

	public final void removeFromBasket(NetworkBookItem book) {
		if (supportsBasket() && book.Id != null && !"".equals(book.Id)) {
			List<String> ids = myBooksInBasketOption.getValue();
			if (ids.contains(book.Id)) {
				ids = new ArrayList(ids);
				ids.remove(book.Id);
				myBooksInBasketOption.setValue(ids);
			}
		}
	}

	public final boolean isBookInBasket(NetworkBookItem book) {
		return myBooksInBasketOption.getValue().contains(book.Id);
	}

	public final List<String> booksInBasket() {
		return myBooksInBasketOption.getValue();
	}

	public NetworkOperationData createOperationData(NetworkOperationData.OnNewItemListener listener) {
		return new NetworkOperationData(this, listener);
	}

	@Override
	public String toString() {
		String icon = getUrlInfo(URL_ICON).URL;
		if (icon != null) {
			if (icon.length() > 64) {
				icon = icon.substring(0, 61) + "...";
			}
			icon = icon.replaceAll("\n", "");
		}
		return "AbstractNetworkLink: {"
			+ "siteName=" + mySiteName
			+ "; title=" + myTitle
			+ "; summary=" + mySummary
			+ "; icon=" + icon
			+ "; infos=" + myInfos
			+ "}";
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AbstractNetworkLink)) {
			return false;
		}
		final AbstractNetworkLink lnk = (AbstractNetworkLink) o;
		if (!mySiteName.equals(lnk.mySiteName)
				|| !myTitle.equals(lnk.myTitle)
				|| !ZLMiscUtil.equals(mySummary, lnk.mySummary)
				|| !ZLMiscUtil.mapsEquals(myInfos, lnk.myInfos)) {
			return false;
		}
		return true;
	}
}
