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

package org.geometerplus.fbreader.network.litres;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

public class LitresUtil {
	static String LITRES_API_URL = "://robot.litres.ru/";
	
	public static String url(String path) {
		String url = LITRES_API_URL + path;
		if (ZLNetworkUtil.hasParameter(url, "sid") ||
				ZLNetworkUtil.hasParameter(url, "pwd")) {
			url = "https" + url;
		} else {
			url = "http" + url;
		}
		return url;
	}

	public static String url(final INetworkLink link, final String path) {
		String urlString = url(path);
		link.rewriteUrl(urlString, true);
		return urlString;
	}

	public static String url(boolean secure, final String path) {
		String url = LITRES_API_URL + path;
		if (secure) {
			url = "https" + url;
		} else {
			url = "http" + url;
		}
		return url;
	}

	public static String url(final INetworkLink link, boolean secure, final String path) {
		String urlString = url(secure, path);
		link.rewriteUrl(urlString, true);
		return urlString;
	}

	public static String generateTrialUrl(String bookId) {
		do{
			bookId = "0"+bookId;
		}while(bookId.length() < 8);

		char[] bookIdArr = bookId.toCharArray();
		String query = "static/trials/"+bookIdArr[0]+bookIdArr[1]+"/"+
										bookIdArr[2]+bookIdArr[3]+"/"+
										bookIdArr[4]+bookIdArr[5]+"/"+
										bookId+".fb2.zip";
		return url(false, query);
	}

	public static String generatePurchaseUrl(final String bookId) {
		String query = url(true, "pages/purchase_book/?");
		query = ZLNetworkUtil.appendParameter(query, "art", bookId);
		return query;
	}

	public static String generateDownloadUrl(final String bookId) {
		String query = url(true, "pages/catalit_download_book/?");
		query = ZLNetworkUtil.appendParameter(query, "art", bookId);
		return query;
	}

	public static String generateAlsoReadUrl(final String bookId) {
		String query = url(false, "pages/catalit_browser/?");
		query = ZLNetworkUtil.appendParameter(query, "rating", "with");
		query = ZLNetworkUtil.appendParameter(query, "art", bookId);
		return query;
	}

	public static String generateBooksByGenreUrl(final String genreId) {
		String query = url(false, "pages/catalit_browser/?");
		query = ZLNetworkUtil.appendParameter(query, "checkpoint", "2000-01-01");
		query = ZLNetworkUtil.appendParameter(query, "genre", genreId);
		return query;
	}

	public static String generateBooksByAuthorUrl(final String authorId) {
		String query = url(false, "pages/catalit_browser/?");
		query = ZLNetworkUtil.appendParameter(query, "checkpoint", "2000-01-01");
		query = ZLNetworkUtil.appendParameter(query, "person", authorId);
		return query;
	}
	
	public static String generateBooksByAuthorSearchUrl(final String lastName) {
		String query = url(false, "pages/catalit_persons/?");
		query = ZLNetworkUtil.appendParameter(query, "search_last_name", lastName+"%");
		return query;
	}
}
