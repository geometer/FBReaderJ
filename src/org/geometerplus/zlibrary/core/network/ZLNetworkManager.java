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

import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;


public class ZLNetworkManager {

	private static ZLNetworkManager ourManager;

	public static ZLNetworkManager Instance() {
		if (ourManager == null) {
			ourManager = new ZLNetworkManager();
		}
		return ourManager;
	}


	private String doBeforeRequest(ZLNetworkRequest request) {
		final String err = request.doBefore();
		if (err != null) {
			return err;
		}

		/*if (request.isInstanceOf(ZLNetworkPostRequest::TYPE_ID)) {
			return doBeforePostRequest((ZLNetworkPostRequest &) request);
		}*/
		return null;
	}

	public String perform(List<ZLNetworkRequest> requests) {
		if (requests.size() == 0) {
			return "";
		}
		if (requests.size() == 1) {
			return perform(requests.get(0));
		}
		HashSet<String> errors = new HashSet<String>();
		// TODO: implement concurrent execution !!!
		for (ZLNetworkRequest r: requests) {
			final String e = perform(r);
			if (e != null && !errors.contains(e)) {
				errors.add(e);
			}
		}
		if (errors.size() == 0) {
			return null;
		}
		StringBuilder message = new StringBuilder();
		for (String e: errors) {
			if (message.length() != 0) {
				message.append(", ");
			}
			message.append(e);
		}
		return message.toString();
	}

	public String perform(ZLNetworkRequest request) {
		boolean sucess = false;
		try {
			final URL url = new URL(request.URL);
			final URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpURLConnection)) {
				return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_UNSUPPORTED_PROTOCOL);
			}
			String error = doBeforeRequest(request);
			if (error != null) {
				return error;
			}
			final HttpURLConnection httpConnection = (HttpURLConnection) connection;
			// TODO: handle SSLCertificate
			// TODO: handle Authentication
			httpConnection.setInstanceFollowRedirects(request.FollowRedirects);
			httpConnection.setConnectTimeout(15000); // FIXME: hardcoded timeout value!!!
			httpConnection.setReadTimeout(30000); // FIXME: hardcoded timeout value!!!
			httpConnection.setRequestProperty("Connection", "Close");
			httpConnection.setRequestProperty("User-Agent", ZLNetworkUtil.getUserAgent());
			final int response = httpConnection.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				InputStream stream = httpConnection.getInputStream();
				try {
					final String err = request.doHandleStream(httpConnection, stream);
					if (err != null) {
						return err;
					}
				} finally {
					stream.close();
				}
				sucess = true;
			} else if (response == HttpURLConnection.HTTP_UNAUTHORIZED) {
				return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_AUTHENTICATION_FAILED);
			} else {
				return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
			}
		} catch (SSLHandshakeException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SSL_CONNECT, ex.getMessage());
		} catch (SSLKeyException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SSL_BAD_KEY, ex.getMessage());
		} catch (SSLPeerUnverifiedException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SSL_PEER_UNVERIFIED, ex.getMessage());
		} catch (SSLProtocolException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SSL_PROTOCOL_ERROR, ex.getMessage());
		} catch (SSLException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SSL_SUBSYSTEM);
		} catch (ConnectException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_CONNECTION_REFUSED, ZLNetworkUtil.hostFromUrl(request.URL));
		} catch (NoRouteToHostException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_HOST_CANNOT_BE_REACHED, ZLNetworkUtil.hostFromUrl(request.URL));
		} catch (UnknownHostException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_RESOLVE_HOST, ZLNetworkUtil.hostFromUrl(request.URL));
		} catch (MalformedURLException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_INVALID_URL);
		} catch (SocketTimeoutException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_TIMEOUT);
		} catch (IOException ex) {
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
		} finally {
			final String err = request.doAfter(sucess);
			if (sucess && err != null) {
				return err;
			}
		}
		return null;
	}
}
