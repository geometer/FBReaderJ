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

import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;


abstract class NetworkDialog {

	// dialog identifiers
	public static final int DIALOG_AUTHENTICATION = 0;
	public static final int DIALOG_REGISTER_USER = 1;
	public static final int DIALOG_CUSTOM_CATALOG = 2;

	private static final TreeMap<Integer, NetworkDialog> ourInstances = new TreeMap<Integer, NetworkDialog>();

	public static NetworkDialog getDialog(int id) {
		NetworkDialog dlg = ourInstances.get(Integer.valueOf(id));
		if (dlg == null) {
			switch (id) {
			case DIALOG_AUTHENTICATION:
				dlg = new AuthenticationDialog();
				break;
			case DIALOG_REGISTER_USER:
				dlg = new RegisterUserDialog();
				break;
			case DIALOG_CUSTOM_CATALOG:
				dlg = new CustomCatalogDialog();
				break;
			}
			if (dlg != null) {
				dlg.myId = id;
				ourInstances.put(Integer.valueOf(id), dlg);
			}
		}
		return dlg;
	}


	private class DialogHandler extends Handler {

		public Message obtainMessage(int code, boolean invalidateLibrary, String message) {
			return obtainMessage(code, invalidateLibrary ? 1 : 0, 0, message);
		}

		@Override
		public void handleMessage(Message message) {
			if (!NetworkView.Instance().isInitialized()) {
				return;
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			if (message.arg1 != 0) {
				library.invalidateChildren();
			}
			library.invalidateVisibility();
			library.synchronize();
			NetworkView.Instance().fireModelChanged();
			if (message.what < 0) {
				if (message.what == -2) {
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource boxResource = dialogResource.getResource("networkError");
					final ZLResource buttonResource = dialogResource.getResource("button");
					new AlertDialog.Builder(myActivity)
						.setTitle(boxResource.getResource("title").getValue())
						.setMessage((String) message.obj)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
						.create().show();
				} else {
					myErrorMessage = (String) message.obj;
					myActivity.showDialog(myId);
					return;
				}
			} else if (message.what > 0) {
				if (myOnSuccessRunnable != null) {
					myOnSuccessRunnable.run();
				}
			}
			clearData();
		}
	};


	protected final ZLResource myResource;

	protected int myId; 

	protected INetworkLink myLink;
	protected String myErrorMessage;
	protected Runnable myOnSuccessRunnable;
	protected Activity myActivity;

	protected final DialogHandler myHandler = new DialogHandler();

	public NetworkDialog(String key) {
		myResource = ZLResource.resource("dialog").getResource(key);
	}

	public static void show(Activity activity, int id, INetworkLink link, Runnable onSuccessRunnable) {
		getDialog(id).showInternal(activity, link, onSuccessRunnable);
	}

	private void showInternal(Activity activity, INetworkLink link, Runnable onSuccessRunnable) {
		myLink = link;
		myErrorMessage = null;
		myOnSuccessRunnable = onSuccessRunnable;
		activity.showDialog(myId);
	}


	protected void sendSuccess(boolean invalidateLibrary) {
		myHandler.sendMessage(myHandler.obtainMessage(1, invalidateLibrary, null));
	}

	protected void sendCancel(boolean invalidateLibrary) {
		myHandler.sendMessage(myHandler.obtainMessage(0, invalidateLibrary, null));
	}

	protected void sendError(boolean restart, boolean invalidateLibrary, String message) {
		myHandler.sendMessage(myHandler.obtainMessage(restart ? -1 : -2, invalidateLibrary, message));
	}

	protected abstract View createLayout();
	protected abstract void clearData();

	protected abstract void onPositive(DialogInterface dialog);
	protected abstract void onNegative(DialogInterface dialog);

	public final Dialog createDialog(final Activity activity) {
		myActivity = activity;

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					onPositive(dialog);
				} else {
					onNegative(dialog);
				}
			}
		};

		final View layout = createLayout();
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		return new AlertDialog.Builder(activity)
			.setView(layout)
			.setTitle(myResource.getResource("title").getValue())
			.setPositiveButton(buttonResource.getResource("ok").getValue(), listener)
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					onNegative(dialog);
				}
			})
			.create();
	}

	public abstract void prepareDialog(Dialog dialog);
}
