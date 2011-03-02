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

package org.geometerplus.android.fbreader.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.util.UIUtil;

class AuthenticationDialog {
	private static AuthenticationDialog ourDialog;

	public static AuthenticationDialog getDialog() {
		if (ourDialog == null) {
			ourDialog = new AuthenticationDialog();
		}
		return ourDialog;
	}

	private class DialogHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			if (!NetworkView.Instance().isInitialized()) {
				return;
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			library.invalidateVisibility();
			library.synchronize();
			NetworkView.Instance().fireModelChanged();
			if (message.what == -1) {
				myErrorMessage = (String)message.obj;
				myActivity.showDialog(0);
			} else if (message.what == 1) {
				if (myOnSuccessRunnable != null) {
					myOnSuccessRunnable.run();
				}
			}
		}
	};


	private final ZLResource myResource =
		ZLResource.resource("dialog").getResource("AuthenticationDialog");

	private INetworkLink myLink;
	private String myErrorMessage;
	private Runnable myOnSuccessRunnable;
	private Activity myActivity;

	private final DialogHandler myHandler = new DialogHandler();

	public static void show(Activity activity, INetworkLink link, Runnable onSuccessRunnable) {
		getDialog().showInternal(activity, link, onSuccessRunnable);
	}

	private void showInternal(Activity activity, INetworkLink link, Runnable onSuccessRunnable) {
		myLink = link;
		myErrorMessage = null;
		myOnSuccessRunnable = onSuccessRunnable;
		activity.showDialog(0);
	}

	private void sendSuccess() {
		myHandler.sendMessage(myHandler.obtainMessage(1, null));
	}

	private void sendCancel() {
		myHandler.sendMessage(myHandler.obtainMessage(0, null));
	}

	private void sendError(String message) {
		myHandler.sendMessage(myHandler.obtainMessage(-1, message));
	}

	private View createLayout() {
		final View layout = myActivity.getLayoutInflater().inflate(R.layout.network_authentication_dialog, null);

		((TextView)layout.findViewById(R.id.network_authentication_login_text)).setText(myResource.getResource("login").getValue());
		((TextView)layout.findViewById(R.id.network_authentication_password_text)).setText(myResource.getResource("password").getValue());

		final TextView registerText = (TextView)layout.findViewById(R.id.network_authentication_register);
		registerText.setText(myResource.getResource("register").getValue());
		registerText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (Util.isRegistrationSupported(myActivity, myLink)) {
					myActivity.dismissDialog(0);
					Util.runRegistrationDialog(myActivity, myLink);
				}
			}
		});
		return layout;
	}

	private void onPositive(DialogInterface dialog) {
		AlertDialog alert = (AlertDialog)dialog;
		final String login = ((TextView)alert.findViewById(R.id.network_authentication_login)).getText().toString().trim();
		final String password = ((TextView)alert.findViewById(R.id.network_authentication_password)).getText().toString();

		if (login.length() == 0) {
			final String err = myResource.getResource("loginIsEmpty").getValue();
			sendError(err);
			return;
		}

		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		mgr.UserNameOption.setValue(login);
		final Runnable runnable = new Runnable() {
			public void run() {
				try {
					mgr.authorise(password);
					if (mgr.needsInitialization()) {
						mgr.initialize();
					}
				} catch (ZLNetworkException e) {
					mgr.logOut();
					sendError(e.getMessage());
					return;
				}
				sendSuccess();
			}
		};
		UIUtil.wait("authentication", runnable, myActivity);
	}

	private void onNegative(DialogInterface dialog) {
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				if (mgr.mayBeAuthorised(false)) {
					mgr.logOut();
					sendCancel();
				}
			}
		};
		UIUtil.wait("signOut", runnable, myActivity);
	}

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

	public final void prepareDialog(final Activity activity, Dialog dialog) {
		myActivity = activity;

		final NetworkAuthenticationManager mgr = myLink.authenticationManager();

		((TextView)dialog.findViewById(R.id.network_authentication_login)).setText(mgr.UserNameOption.getValue());
		((TextView)dialog.findViewById(R.id.network_authentication_password)).setText("");

		final TextView error = (TextView)dialog.findViewById(R.id.network_authentication_error);
		if (myErrorMessage == null) {
			error.setVisibility(View.GONE);
			error.setText("");
		} else {
			error.setVisibility(View.VISIBLE);
			error.setText(myErrorMessage);
		}

		dialog.findViewById(R.id.network_authentication_register).setVisibility(Util.isRegistrationSupported(myActivity, myLink) ? View.VISIBLE : View.GONE);

		View dlgView = dialog.findViewById(R.id.network_authentication_dialog);
		dlgView.invalidate();
		dlgView.requestLayout();
	}
}
