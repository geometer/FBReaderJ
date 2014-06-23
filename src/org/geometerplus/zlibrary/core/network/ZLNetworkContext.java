/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.HashSet;
import java.util.List;

import org.apache.http.client.CookieStore;

public abstract class ZLNetworkContext implements ZLNetworkManager.BearerAuthenticator {
	private final ZLNetworkManager myManager = ZLNetworkManager.Instance();

	protected ZLNetworkContext() {
	}

	protected CookieStore cookieStore() {
		return myManager.CookieStore;
	}

	public boolean performQuietly(ZLNetworkRequest request) {
		try {
			perform(request);
			return true;
		} catch (ZLNetworkException e) {
			return false;
		}
	}

	public void perform(ZLNetworkRequest request) throws ZLNetworkException {
		myManager.perform(request, this, 30000, 15000);
	}

	public void perform(List<? extends ZLNetworkRequest> requests) throws ZLNetworkException {
		if (requests.size() == 0) {
			return;
		}
		if (requests.size() == 1) {
			perform(requests.get(0));
			return;
		}
		HashSet<String> errors = new HashSet<String>();
		// TODO: implement concurrent execution !!!
		for (ZLNetworkRequest r : requests) {
			try {
				perform(r);
			} catch (ZLNetworkException e) {
				e.printStackTrace();
				errors.add(e.getMessage());
			}
		}
		if (errors.size() > 0) {
			StringBuilder message = new StringBuilder();
			for (String e : errors) {
				if (message.length() != 0) {
					message.append(", ");
				}
				message.append(e);
			}
			throw new ZLNetworkException(true, message.toString());
		}
	}

	public final void downloadToFile(String url, final File outFile) throws ZLNetworkException {
		downloadToFile(url, outFile, 8192);
	}

	private final void downloadToFile(String url, final File outFile, final int bufferSize) throws ZLNetworkException {
		myManager.perform(new ZLNetworkRequest(url) {
			public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
				OutputStream outStream = new FileOutputStream(outFile);
				try {
					final byte[] buffer = new byte[bufferSize];
					while (true) {
						final int size = inputStream.read(buffer);
						if (size <= 0) {
							break;
						}
						outStream.write(buffer, 0, size);
					}
				} finally {
					outStream.close();
				}
			}
		}, this, 0, 0);
	}
}
