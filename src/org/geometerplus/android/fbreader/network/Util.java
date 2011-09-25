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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class Util implements UserRegistrationConstants {
	private static final String REGISTRATION_ACTION =
		"android.fbreader.action.NETWORK_LIBRARY_REGISTER";
	private static final String AUTO_SIGNIN_ACTION =
		"android.fbreader.action.NETWORK_LIBRARY_AUTOSIGNIN";

	static INetworkLink linkByIntent(Intent intent) {
		final Uri uri = intent.getData();
		return uri != null ? NetworkLibrary.Instance().getLinkByUrl(uri.toString()) : null;
	}

	public static Intent intentByLink(Intent intent, INetworkLink link) {
		if (link != null) {
			intent.setData(Uri.parse(link.getUrl(UrlInfo.Type.Catalog)));
		}
		return intent;
	}

	static void initLibrary(Activity activity) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		if (library.isInitialized()) {
			return;
		}

		UIUtil.wait("loadingNetworkLibrary", new Runnable() {
			public void run() {
				if (SQLiteNetworkDatabase.Instance() == null) {
					new SQLiteNetworkDatabase();
				}
                
				library.initialize();
			}
		}, activity);
	}

	private static boolean testService(Activity activity, String action, String url) {
		return url != null && PackageUtil.canBeStarted(activity, new Intent(action, Uri.parse(url)), true);
	}

	public static boolean isRegistrationSupported(Activity activity, INetworkLink link) {
		return testService(
			activity,
			REGISTRATION_ACTION,
			link.getUrl(UrlInfo.Type.SignUp)
		);
	}

	public static boolean isAutoSignInSupported(Activity activity, INetworkLink link) {
		return testService(
			activity,
			AUTO_SIGNIN_ACTION,
			link.getUrl(UrlInfo.Type.SignUp)
		);
	}

	public static void runRegistrationDialog(Activity activity, INetworkLink link) {
		try {
			final Intent intent = new Intent(
				REGISTRATION_ACTION,
				Uri.parse(link.getUrl(UrlInfo.Type.SignUp))
			);
			if (PackageUtil.canBeStarted(activity, intent, true)) {
				activity.startActivityForResult(intent, NetworkLibraryActivity.SIGNUP_CODE);
			}
		} catch (ActivityNotFoundException e) {
		}
	}

	public static void runAutoSignInDialog(Activity activity, INetworkLink link) {
		try {
			final Intent intent = new Intent(
				AUTO_SIGNIN_ACTION,
				Uri.parse(link.getUrl(UrlInfo.Type.SignIn))
			);
			if (PackageUtil.canBeStarted(activity, intent, true)) {
				activity.startActivityForResult(intent, NetworkLibraryActivity.AUTO_SIGNIN_CODE);
			}
		} catch (ActivityNotFoundException e) {
		}
	}

	public static void runAuthenticationDialog(Activity activity, INetworkLink link, Runnable onSuccess) {
		final NetworkAuthenticationManager mgr = link.authenticationManager();

		final Intent intent = intentByLink(new Intent(activity, AuthenticationActivity.class), link);
		AuthenticationActivity.registerRunnable(intent, onSuccess);
		intent.putExtra(AuthenticationActivity.USERNAME_KEY, mgr.UserNameOption.getValue());
		intent.putExtra(AuthenticationActivity.SCHEME_KEY, "https");
		intent.putExtra(AuthenticationActivity.CUSTOM_AUTH_KEY, true);
		activity.startActivity(intent);
	}

	static void processSignup(INetworkLink link, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			try {
				final NetworkAuthenticationManager mgr = link.authenticationManager();
				if (mgr instanceof LitResAuthenticationManager) {
					((LitResAuthenticationManager)mgr).initUser(
						data.getStringExtra(USER_REGISTRATION_USERNAME),
						data.getStringExtra(USER_REGISTRATION_LITRES_SID),
						"",
						false
					);
				}
				if (!mgr.isAuthorised(true)) {
					throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
				}
				try {
					mgr.initialize();
				} catch (ZLNetworkException e) {
					mgr.logOut();
					throw e;
				}
			} catch (ZLNetworkException e) {
				// TODO: show an error message
			}
		}
	}

	public static void processAutoSignIn(Activity activity, INetworkLink link, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			try {
				final NetworkAuthenticationManager mgr = link.authenticationManager();
				if (mgr instanceof LitResAuthenticationManager) {
					((LitResAuthenticationManager)mgr).initUser(
						data.getStringExtra(USER_REGISTRATION_USERNAME),
						data.getStringExtra(USER_REGISTRATION_LITRES_SID),
						"",
						false
					);
				}
				if (!mgr.isAuthorised(true)) {
					throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
				}
				try {
					mgr.initialize();
				} catch (ZLNetworkException e) {
					mgr.logOut();
					throw e;
				}
				// TODO: implement postRunnable (e.g. buying book on "quick buy")
			} catch (ZLNetworkException e) {
				// TODO: show an error message
			}
		}
	}


	public static void openInBrowser(Context context, String url) {
		if (url != null) {
			url = NetworkLibrary.Instance().rewriteUrl(url, true);
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
	}

	public static void openTree(Context context, NetworkTree tree) {
		final Class<?> clz = tree instanceof NetworkBookTree
			? NetworkBookInfoActivity.class : NetworkLibraryActivity.class;
		if (context instanceof NetworkLibraryActivity && clz == NetworkLibraryActivity.class) {
			((NetworkLibraryActivity)context).openTree(tree);
		} else {
			context.startActivity(
				new Intent(context.getApplicationContext(), clz)
					.putExtra(NetworkLibraryActivity.TREE_KEY_KEY, tree.getUniqueKey())
			);
		}
	}

	public static NetworkTree getTreeFromIntent(Intent intent) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		final NetworkTree.Key key = (NetworkTree.Key)intent.getSerializableExtra(NetworkLibraryActivity.TREE_KEY_KEY);
		return library.getTreeByKey(key);
	}
}
