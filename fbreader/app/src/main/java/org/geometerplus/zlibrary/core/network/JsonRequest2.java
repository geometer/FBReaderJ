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

import org.json.simple.JSONValue;

public abstract class JsonRequest2 extends ZLNetworkRequest.PostWithBody {
	public JsonRequest2(String url, Object data) {
		super(url, JSONValue.toJSONString(data), false);
	}

	@Override
	public void handleStream(InputStream stream, int length) throws IOException, ZLNetworkException {
		processResponse(JSONValue.parse(new InputStreamReader(stream)));
	}

	protected abstract void processResponse(Object response);
}
