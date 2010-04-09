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

import java.io.*;
import java.net.*;

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
		if (!request.doBefore()) {
			final String err = request.getErrorMessage();
			if (err != null) {
				return err;
			}
			return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
		}

		/*if (request.isInstanceOf(ZLNetworkPostRequest::TYPE_ID)) {
			return doBeforePostRequest((ZLNetworkPostRequest &) request);
		}*/
		return null;
	}

	public String perform(ZLNetworkRequest request) {
		boolean sucess = false;
		try {
			final URL url = new URL(request.URL);
			final URLConnection connection = url.openConnection();
			if (!(connection instanceof HttpURLConnection)) {
				return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_UNSUPPORTED_URL);
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
					if (!request.handleStream(stream)) {
						final String err = request.getErrorMessage();
						if (err != null) {
							return err;
						}
						return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
					}
				} finally {
					stream.close();
				}
				sucess = true;
			} else {
				return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
			}
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
			boolean res = request.doAfter(sucess);
			if (sucess && !res) {
				final String err = request.getErrorMessage();
				if (err != null) {
					return err;
				}
				return ZLNetworkErrors.errorMessage(ZLNetworkErrors.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
			}
		}
		return null;
	}
}
