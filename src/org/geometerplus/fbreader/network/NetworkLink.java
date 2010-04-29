/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


abstract public class NetworkLink {

	public static final String URL_MAIN = "main";
	public static final String URL_SEARCH = "search";
	public static final String URL_SIGN_IN = "signIn";
	public static final String URL_SIGN_OUT = "signOut";
	public static final String URL_SIGN_UP = "signUp";
	public static final String URL_REFILL_ACCOUNT = "refillAccount";
	public static final String URL_RECOVER_PASSWORD = "recoverPassword";

	public final String SiteName;
	public final String Title;
	public final String Summary;
	public final String Icon;
	public final ZLBooleanOption OnOption;
	public final TreeMap<String, String> Links;


	/**
	 * Creates new NetworkLink instance.
	 *
	 * @param siteName   name of the corresponding website. Must be not <code>null</code>.
	 * @param title      title of the corresponding library item. Must be not <code>null</code>.
	 * @param summary    description of the corresponding library item. Can be <code>null</code>.
	 * @param icon       string contains link's icon data/url. Can be <code>null</code>.
	 * @param links      map contains URLs with their identifiers; must always contain one URL with <code>URL_MAIN</code> identifier
	 */
	public NetworkLink(String siteName, String title, String summary, String icon, Map<String, String> links) {
		SiteName = siteName;
		Title = title;
		Summary = summary;
		Icon = icon;
		OnOption = new ZLBooleanOption(SiteName, "on", true);
		Links = new TreeMap(links);
	}

	public abstract ZLNetworkRequest simpleSearchRequest(String pattern, NetworkOperationData data);
	public abstract ZLNetworkRequest resume(NetworkOperationData data);

	public abstract NetworkLibraryItem libraryItem();
	public abstract NetworkAuthenticationManager authenticationManager();

	public abstract String rewriteUrl(String url, boolean isUrlExternal);
}
