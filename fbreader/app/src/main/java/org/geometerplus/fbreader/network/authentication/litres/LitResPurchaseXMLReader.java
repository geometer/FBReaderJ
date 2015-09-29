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

package org.geometerplus.fbreader.network.authentication.litres;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.network.ZLNetworkAuthenticationException;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.AlreadyPurchasedException;
import org.geometerplus.fbreader.network.NetworkException;

class LitResPurchaseXMLReader extends LitResAuthenticationXMLReader {
	private static final String TAG_AUTHORIZATION_FAILED = "catalit-authorization-failed";
	private static final String TAG_PURCHASE_OK = "catalit-purchase-ok";
	private static final String TAG_PURCHASE_FAILED = "catalit-purchase-failed";

	public String Account;
	public String BookId;

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if (TAG_AUTHORIZATION_FAILED == tag) {
			setException(new ZLNetworkAuthenticationException());
		} else {
			Account = attributes.getValue("account");
			BookId = attributes.getValue("art");
			if (TAG_PURCHASE_OK == tag) {
				// nop
			} else if (TAG_PURCHASE_FAILED == tag) {
				final String error = attributes.getValue("error");
				if ("1".equals(error)) {
					setException(ZLNetworkException.forCode(NetworkException.ERROR_PURCHASE_NOT_ENOUGH_MONEY));
				} else if ("2".equals(error)) {
					setException(ZLNetworkException.forCode(NetworkException.ERROR_PURCHASE_MISSING_BOOK));
				} else if ("3".equals(error)) {
					setException(new AlreadyPurchasedException());
				} else {
					setException(ZLNetworkException.forCode(NetworkException.ERROR_INTERNAL));
				}
			} else {
				setException(ZLNetworkException.forCode(NetworkException.ERROR_SOMETHING_WRONG, LitResUtil.HOST_NAME));
			}
		}
		return true;
	}
}
