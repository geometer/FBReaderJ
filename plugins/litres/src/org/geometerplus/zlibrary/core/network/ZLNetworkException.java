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

package org.geometerplus.zlibrary.core.network;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public class ZLNetworkException extends Exception {
	private static final long serialVersionUID = 4272384299121648643L;

	// Messages with no parameters:
	public static final String ERROR_UNKNOWN_ERROR = "unknownErrorMessage";
	public static final String ERROR_TIMEOUT = "operationTimedOutMessage";
	public static final String ERROR_CONNECT_TO_NETWORK = "couldntConnectToNetworkMessage";
	public static final String ERROR_UNSUPPORTED_PROTOCOL = "unsupportedProtocol";
	public static final String ERROR_INVALID_URL = "invalidURL";
	public static final String ERROR_AUTHENTICATION_FAILED = "authenticationFailed";
	public static final String ERROR_SSL_SUBSYSTEM = "sslError";
	public static final String ERROR_SSL_PROTOCOL_ERROR = "sslProtocolError";

	// Messages with one parameter:
	public static final String ERROR_SOMETHING_WRONG = "somethingWrongMessage";
	public static final String ERROR_CREATE_DIRECTORY = "couldntCreateDirectoryMessage";
	public static final String ERROR_CREATE_FILE = "couldntCreateFileMessage";
	public static final String ERROR_CONNECT_TO_HOST = "couldntConnectMessage";
	public static final String ERROR_RESOLVE_HOST = "couldntResolveHostMessage";
	public static final String ERROR_HOST_CANNOT_BE_REACHED = "hostCantBeReached";
	public static final String ERROR_CONNECTION_REFUSED = "connectionRefused";
	public static final String ERROR_SSL_CONNECT = "sslConnectErrorMessage";
	public static final String ERROR_SSL_BAD_KEY = "sslBadKey";
	public static final String ERROR_SSL_PEER_UNVERIFIED = "sslPeerUnverified";
	public static final String ERROR_SSL_BAD_FILE = "sslBadCertificateFileMessage";
	public static final String ERROR_SSL_EXPIRED = "sslCertificateExpired";
	public static final String ERROR_SSL_NOT_YET_VALID = "sslCertificateNotYetValid";

	private static ZLResource getResource() {
		return ZLResource.resource("dialog").getResource("networkError");
	}

	private static String errorMessage(String key) {
		if (key == null) {
			return "null";
		}
		return getResource().getResource(key).getValue();
	}

	private static String errorMessage(String key, String arg) {
		if (key == null) {
			return "null";
		}
		if (arg == null) {
			arg = "null";
		}
		return getResource().getResource(key).getValue().replace("%s", arg);
	}

	final private String myCode;

	public ZLNetworkException(boolean useAsMessage, String str) {
		super(useAsMessage ? str : errorMessage(str));
		myCode = useAsMessage ? null : str;
	}

	public ZLNetworkException(String code) {
		this(false, code);
	}

	public ZLNetworkException(String code, String arg) {
		super(errorMessage(code, arg));
		myCode = code;
	}

	public String getCode() {
		return myCode;
	}
}
