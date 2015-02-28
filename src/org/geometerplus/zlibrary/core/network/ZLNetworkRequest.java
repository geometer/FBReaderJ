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

import java.io.*;
import java.util.Map;
import java.util.HashMap;

public abstract class ZLNetworkRequest {
	public static abstract class Get extends ZLNetworkRequest {
		protected Get(String url) {
			this(url, false);
		}

		protected Get(String url, boolean quiet) {
			super(url, quiet);
		}
	}

	public static abstract class PostWithMap extends ZLNetworkRequest {
		public final Map<String,String> PostParameters = new HashMap<String,String>();

		protected PostWithMap(String url) {
			this(url, false);
		}

		protected PostWithMap(String url, boolean quiet) {
			super(url, quiet);
		}

		public void addPostParameter(String name, String value) {
			PostParameters.put(name, value);
		}
	}

	public static abstract class PostWithBody extends ZLNetworkRequest {
		public final String Body;

		protected PostWithBody(String url, String body, boolean quiet) {
			super(url, quiet);
			Body = body;
		}
	}

	public static abstract class FileUpload extends ZLNetworkRequest {
		final File File;

		protected FileUpload(String url, File file, boolean quiet) {
			super(url, quiet);
			File = file;
		}
	}

	String URL;
	public final Map<String,String> Headers = new HashMap<String,String>();

	private final boolean myIsQuiet;

	private ZLNetworkRequest(String url) {
		this(url, false);
	}

	private ZLNetworkRequest(String url, boolean quiet) {
		URL = url;
		myIsQuiet = quiet;
	}

	public void addHeader(String name, String value) {
		Headers.put(name, value);
	}

	public String getURL() {
		return URL;
	}

	public boolean isQuiet() {
		return myIsQuiet;
	}

	public void doBefore() throws ZLNetworkException {
	}

	public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
}

	public void doAfter(boolean success) throws ZLNetworkException {
	}
}
