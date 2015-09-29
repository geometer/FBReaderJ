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

package org.geometerplus.zlibrary.core.network;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public class ZLNetworkException extends Exception {
	private static final long serialVersionUID = 4272384299121648643L;

	// Messages with no parameters:
	public static final String ERROR_UNKNOWN_ERROR = "unknownErrorMessage";
	public static final String ERROR_TIMEOUT = "operationTimedOutMessage";
	public static final String ERROR_UNSUPPORTED_PROTOCOL = "unsupportedProtocol";
	public static final String ERROR_INVALID_URL = "invalidURL";

	// Messages with one parameter:
	public static final String ERROR_SOMETHING_WRONG = "somethingWrongMessage";
	public static final String ERROR_CREATE_DIRECTORY = "couldntCreateDirectoryMessage";
	public static final String ERROR_CREATE_FILE = "couldntCreateFileMessage";
	public static final String ERROR_CONNECT_TO_HOST = "couldntConnectMessage";
	public static final String ERROR_RESOLVE_HOST = "couldntResolveHostMessage";
	public static final String ERROR_HOST_CANNOT_BE_REACHED = "hostCantBeReached";
	public static final String ERROR_CONNECTION_REFUSED = "connectionRefused";

	private static ZLResource getResource() {
		return ZLResource.resource("dialog").getResource("networkError");
	}

	protected static String errorMessage(String code) {
		return code != null ? getResource().getResource(code).getValue() : "null";
	}

	public static ZLNetworkException forCode(String code, String arg, Throwable cause) {
		final String message;
		if (code == null) {
			message = "null";
		} else {
			if (arg == null) {
				arg = "null";
			}
			message = getResource().getResource(code).getValue().replace("%s", arg);
		}
		return new ZLNetworkException(message, cause);
	}

	public static ZLNetworkException forCode(String code, Throwable cause) {
		return new ZLNetworkException(errorMessage(code), cause);
	}

	public static ZLNetworkException forCode(String code, String arg) {
		return forCode(code, arg, null);
	}

	public static ZLNetworkException forCode(String code) {
		return forCode(code, (Throwable)null);
	}

	public ZLNetworkException(String message) {
		super(message);
	}

	public ZLNetworkException(String message, Throwable cause) {
		super(message, cause);
	}
}
