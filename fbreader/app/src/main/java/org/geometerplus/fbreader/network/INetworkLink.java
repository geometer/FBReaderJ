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

import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoWithDate;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public interface INetworkLink extends Comparable<INetworkLink> {
	public enum Type {
		Predefined(0),
		Custom(1),
		Local(2),
		Sync(3);

		public final int Index;

		Type(int index) {
			Index = index;
		}

		public static Type byIndex(int index) {
			for (Type t : Type.values()) {
				if (t.Index == index) {
					return t;
				}
			}
			return Custom;
		}
	};

	public enum AccountStatus {
		NotSupported,
		NoUserName,
		SignedIn,
		SignedOut,
		NotChecked
	};

	public static final int INVALID_ID = -1;

	int getId();
	void setId(int id);

	String getStringId();
	String getShortName();
	String getHostName();
	String getTitle();
	String getSummary();

	String getUrl(UrlInfo.Type type);
	UrlInfoWithDate getUrlInfo(UrlInfo.Type type);
	Set<UrlInfo.Type> getUrlKeys();

	/**
	 * @param force if local status is not checked then
     *    if force is set to false, NotChecked will be returned
     *    if force is set to true, network check will be performed;
	 *       that will take some time and can return NotChecked (if network is not available)
     */
	//AccountStatus getAccountStatus(boolean force);

	Type getType();

	/**
	 * @return 2-letters language code or special token "multi"
	 */
	String getLanguage();

	/**
	 * @param listener Network operation listener
	 * @return instance, which represents the state of the network operation.
	 */
	NetworkOperationData createOperationData(NetworkItemsLoader loader);

	BasketItem getBasketItem();

	ZLNetworkRequest simpleSearchRequest(String pattern, NetworkOperationData data);
	ZLNetworkRequest resume(NetworkOperationData data);

	NetworkCatalogItem libraryItem();
	NetworkAuthenticationManager authenticationManager();

	String rewriteUrl(String url, boolean isUrlExternal);
}
