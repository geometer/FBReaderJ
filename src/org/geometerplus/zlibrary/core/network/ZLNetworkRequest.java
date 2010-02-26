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


public abstract class ZLNetworkRequest {

	public final String Url;
	//public final String SSLCertificate;
	public String ErrorMessage;

	private String myUserName;
	private String myPassword;
	private boolean myRedirectionSupported;

	protected ZLNetworkRequest(String url/*, String sslCertificate*/) {
		Url = url;
		//SSLCertificate = sslCertificate;
	}

	public final void setRedirectionSupported(boolean supported) {
		myRedirectionSupported = supported;
	}

	public final void setupAuthentication(String userName, String password) {
		myUserName = userName;
		myPassword = password;
	}

	protected abstract boolean handleStream(InputStream stream);
}
