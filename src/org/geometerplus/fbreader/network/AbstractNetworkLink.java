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

package org.geometerplus.fbreader.network;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public abstract class AbstractNetworkLink implements INetworkLink {
	private int myId;

	protected String myTitle;
	protected String mySummary;
	protected final String myLanguage;
	protected final UrlInfoCollection<UrlInfoWithDate> myInfos;

	/**
	 * Creates new NetworkLink instance.
	 *
	 * @param title      title of the corresponding library item. Must be not <code>null</code>.
	 * @param summary    description of the corresponding library item. Can be <code>null</code>.
	 * @param language   language of the catalog. If <code>null</code> we assume this catalog is multilanguage.
	 * @param infos      collection of URL infos; must always contain one URL with <code>UrlInfo.Type.Catalog</code> identifier
	 */
	public AbstractNetworkLink(int id, String title, String summary, String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		myId = id;
		myTitle = title;
		mySummary = summary;
		myLanguage = language != null ? language : "multi";
		myInfos = new UrlInfoCollection<UrlInfoWithDate>(infos);
	}

	public int getId() {
		return myId;
	}

	public void setId(int id) {
		myId = id;
	}

	public String getShortName() {
		return getHostName();
	}

	public String getStringId() {
		final String hostName = getHostName();
		if (hostName != null) {
			return hostName;
		}
		return "CATALOG_" + myId;
	}

	public final String getHostName() {
		final String catalogUrl = getUrl(UrlInfo.Type.Catalog);
		if (catalogUrl == null) {
			return null;
		}
		final Matcher m = Pattern.compile("^[a-zA-Z]+://([^/]+).*").matcher(catalogUrl);
		return m.matches() ? m.group(1) : null;
	}

	public final String getTitle() {
		return myTitle;
	}

	public String getSummary() {
		return mySummary;
	}

	public final String getLanguage() {
		return myLanguage;
	}

	public final UrlInfoCollection<UrlInfoWithDate> urlInfoMap() {
		return new UrlInfoCollection<UrlInfoWithDate>(myInfos);
	}

	public final String getUrl(UrlInfo.Type type) {
		return getUrlInfo(type).Url;
	}

	public final UrlInfoWithDate getUrlInfo(UrlInfo.Type type) {
		final UrlInfoWithDate info = myInfos.getInfo(type);
		return info != null ? info : UrlInfoWithDate.NULL;
	}

	public final Set<UrlInfo.Type> getUrlKeys() {
		final HashSet<UrlInfo.Type> set = new HashSet<UrlInfo.Type>();
		for (UrlInfo info : myInfos.getAllInfos()) {
			set.add(info.InfoType);
		}
		return set;
	}

	public BasketItem getBasketItem() {
		return null;
	}

	public ZLNetworkRequest bookListRequest(List<String> bookIds, NetworkOperationData data) {
		return null;
	}

	public NetworkOperationData createOperationData(NetworkItemsLoader listener) {
		return new NetworkOperationData(this, listener);
	}

	@Override
	public String toString() {
		String icon = getUrl(UrlInfo.Type.Catalog);
		if (icon != null) {
			if (icon.length() > 64) {
				icon = icon.substring(0, 61) + "...";
			}
			icon = icon.replaceAll("\n", "");
		}
		return "AbstractNetworkLink: {"
			+ "id=" + getStringId()
			+ "; title=" + myTitle
			+ "; summary=" + mySummary
			+ "; icon=" + icon
			+ "; infos=" + myInfos
			+ "}";
	}

	private String getTitleForComparison() {
		String title = getTitle();
		for (int index = 0; index < title.length(); ++index) {
			final char ch = title.charAt(index);
			if (ch < 128 && Character.isLetter(ch)) {
				return title.substring(index);
			}
		}
		return title;
	}

	private static int getLanguageOrder(String language) {
		if (Language.MULTI_CODE.equals(language)) {
			return 1;
		}
		if (language.equals(Locale.getDefault().getLanguage())) {
			return 0;
		}
		return 2;
	}

	public int compareTo(INetworkLink link) {
		int diff = getLanguageOrder(getLanguage()) - getLanguageOrder(link.getLanguage());
		if (diff != 0) {
			return diff;
		}
		diff = getTitleForComparison().compareToIgnoreCase(((AbstractNetworkLink)link).getTitleForComparison());
		if (diff != 0) {
			return diff;
		}
		return getId() - link.getId();
	}
}
