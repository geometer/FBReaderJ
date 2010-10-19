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

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

public abstract class NetworkException extends ZLNetworkException {
	private static final long serialVersionUID = 8931535868304063605L;

	public static final String ERROR_INTERNAL = "internalError";
	public static final String ERROR_PURCHASE_NOT_ENOUGH_MONEY = "purchaseNotEnoughMoney";
	public static final String ERROR_PURCHASE_MISSING_BOOK = "purchaseMissingBook";
	public static final String ERROR_PURCHASE_ALREADY_PURCHASED = "purchaseAlreadyPurchased";
	public static final String ERROR_BOOK_NOT_PURCHASED = "bookNotPurchased";
	public static final String ERROR_DOWNLOAD_LIMIT_EXCEEDED = "downloadLimitExceeded";

	public static final String ERROR_LOGIN_ALREADY_TAKEN = "loginAlreadyTaken";
	public static final String ERROR_LOGIN_WAS_NOT_SPECIFIED = "loginNotSpecified";
	public static final String ERROR_PASSWORD_WAS_NOT_SPECIFIED = "passwordNotSpecified";
	public static final String ERROR_EMAIL_WAS_NOT_SPECIFIED = "emailNotSpecified";
	public static final String ERROR_INVALID_EMAIL = "invalidEMail";
	public static final String ERROR_TOO_MANY_REGISTRATIONS = "tooManyRegistrations";

	public static final String ERROR_NO_USER_EMAIL = "noUserEmail";

	public static final String ERROR_UNSUPPORTED_OPERATION = "unsupportedOperation";

	public static final String ERROR_NOT_AN_OPDS = "notAnOPDS";
	public static final String ERROR_NO_REQUIRED_INFORMATION = "noRequiredInformation";
	public static final String ERROR_CACHE_DIRECTORY_ERROR = "cacheDirectoryError";

	private NetworkException() {
		super(null);
	}
}
