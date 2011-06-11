/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

public abstract class ZLNetworkRequest {
	String URL;
	public final String SSLCertificate;
	public final String PostData;
	public final Map<String,String> PostParameters = new HashMap<String,String>();

	protected ZLNetworkRequest(String url) {
		this(url, null, null);
	}

	protected ZLNetworkRequest(String url, String sslCertificate, String postData) {
		URL = url;
		SSLCertificate = sslCertificate;
		PostData = postData;
	}

	public void addPostParameter(String name, String value) {
		PostParameters.put(name, value);
	}

	public String getURL() {
		return URL;
	}

	public void doBefore() throws ZLNetworkException {
	}
	
	public abstract void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException;

	public void doAfter(boolean success) throws ZLNetworkException {
	}
}
