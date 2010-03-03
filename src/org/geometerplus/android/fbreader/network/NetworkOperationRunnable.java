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

package org.geometerplus.android.fbreader.network;

import android.content.Context;
import android.app.AlertDialog;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;


abstract class NetworkOperationRunnable implements Runnable {

	private String myKey;
	protected String myErrorMessage;

	protected NetworkOperationRunnable(String key) {
		myKey = key;
	}

	public static void showErrorMessage(Context context, String message) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("networkError");
		new AlertDialog.Builder(context)
			.setTitle(boxResource.getResource("title").getValue())
			.setMessage(message)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
			.create().show();
	}

	public void executeWithUI() {
		//ZLDialogManager.Instance().wait(myKey, this);
		run();
	}

	public boolean hasErrors() {
		return myErrorMessage != null;
	}

	public void showErrorMessage(Context context) {
		if (myErrorMessage != null) {
			showErrorMessage(context, myErrorMessage);
		}
	}

	public String errorMessage() {
		return myErrorMessage;
	}
}
