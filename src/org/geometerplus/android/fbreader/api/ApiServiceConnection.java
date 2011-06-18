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

package org.geometerplus.android.fbreader.api;

import android.content.*;
import android.os.IBinder;

public class ApiServiceConnection implements ServiceConnection, Api, ApiMethods {
	private static String ACTION_API = "android.fbreader.action.API";

	private final Context myContext;
	private volatile ApiInterface myInterface;

	public ApiServiceConnection(Context context) {
		myContext = context;
		connect();
	}

	public synchronized void connect() {
		if (myInterface == null) {
			myContext.bindService(new Intent(ACTION_API), this, Context.BIND_AUTO_CREATE);
		}
	}

	public synchronized void disconnect() {
		if (myInterface != null) {
			try {
				myContext.unbindService(this);
			} catch (IllegalArgumentException e) {
			}
			myInterface = null;
		}
	}

	public synchronized void onServiceConnected(ComponentName className, IBinder service) {
		System.err.println("onServiceConnected call");
		myInterface = ApiInterface.Stub.asInterface(service);
	}

	public synchronized void onServiceDisconnected(ComponentName name) {
		System.err.println("onServiceDisconnected call");
		myInterface = null;
	}

	private synchronized ApiObject request(int method, ApiObject[] params) throws ApiException {
		if (myInterface == null) {
			throw new ApiException("Not connected to FBReader");
		}
		final ApiObject object;
		try {
			object = myInterface.request(method, params);
		} catch (android.os.RemoteException e) {
			throw new ApiException(e);
		}
		if (object instanceof ApiObject.Error) {
			throw new ApiException(((ApiObject.Error)object).Message);
		}
		return object;
	}

	private String requestString(int method, ApiObject[] params) throws ApiException {
		final ApiObject object = request(method, params);
		if (!(object instanceof ApiObject.String)) {
			throw new ApiException("Cannot cast return type of method " + method + " to String");
		}
		return ((ApiObject.String)object).Value;
	}

	private int requestInt(int method, ApiObject[] params) throws ApiException {
		final ApiObject object = request(method, params);
		if (!(object instanceof ApiObject.Integer)) {
			throw new ApiException("Cannot cast return type of method " + method + " to int");
		}
		return ((ApiObject.Integer)object).Value;
	}

	private TextPosition requestTextPosition(int method, ApiObject[] params) throws ApiException {
		final ApiObject object = request(method, params);
		if (!(object instanceof TextPosition)) {
			throw new ApiException("Cannot cast return type of method " + method + " to TextPosition");
		}
		return (TextPosition)object;
	}

	private static final ApiObject[] EMPTY_PARAMETERS = new ApiObject[0];

	private static ApiObject[] envelope(int value) {
		return new ApiObject[] { ApiObject.envelope(value) };
	}

	public String getFBReaderVersion() throws ApiException {
		return requestString(GET_FBREADER_VERSION, EMPTY_PARAMETERS);
	}

	public String getBookLanguage() throws ApiException {
		return requestString(GET_BOOK_LANGUAGE, EMPTY_PARAMETERS);
	}

	public TextPosition getPageStart() throws ApiException {
		return requestTextPosition(GET_PAGE_START, EMPTY_PARAMETERS);
	}

	public TextPosition getPageEnd() throws ApiException {
		return requestTextPosition(GET_PAGE_END, EMPTY_PARAMETERS);
	}

	public void setPageStart(TextPosition position) throws ApiException {
		request(SET_PAGE_START, new ApiObject[] { position });
	}

	public int getParagraphsNumber() throws ApiException {
		return requestInt(GET_PARAGRAPHS_NUMBER, EMPTY_PARAMETERS);
	}

	public String getParagraphText(int paragraphIndex) throws ApiException {
		return requestString(GET_PARAGRAPH_TEXT, envelope(paragraphIndex));
	}

	public int getElementsNumber(int paragraphIndex) throws ApiException {
		return requestInt(GET_ELEMENTS_NUMBER, envelope(paragraphIndex));
	}
}
