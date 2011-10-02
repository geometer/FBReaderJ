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
import android.net.Uri;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class Util implements UserRegistrationConstants {
	static final String ACCOUNT_ACTION = "android.fbreader.action.network.ACCOUNT";

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
			ACCOUNT_ACTION,
			link.getUrl(UrlInfo.Type.Catalog) + "/register"
		);
	}

	public static void runRegistrationDialog(Activity activity, INetworkLink link) {
		try {
			final Intent intent = new Intent(
				ACCOUNT_ACTION,
				Uri.parse(link.getUrl(UrlInfo.Type.Catalog) + "/register")
			);
			intent.putExtra(UserRegistrationConstants.SIGNUP_URL, link.getUrl(UrlInfo.Type.SignUp));
			if (PackageUtil.canBeStarted(activity, intent, true)) {
				activity.startActivityForResult(intent, NetworkLibraryActivity.SIGNUP_CODE);
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

	public static void openInBrowser(Activity activity, String url) {
		if (url != null) {
			url = NetworkLibrary.Instance().rewriteUrl(url, true);
			activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
	}

	public static void doDownloadBook(Activity activity, final NetworkBookItem book, boolean demo) {
		final UrlInfo.Type resolvedType =
			demo ? UrlInfo.Type.BookDemo : UrlInfo.Type.Book;
		final BookUrlInfo ref = book.reference(resolvedType);
		if (ref != null) {
			activity.startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.Url), 
						activity.getApplicationContext(), BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanUrl())
					.putExtra(BookDownloaderService.TITLE_KEY, book.Title)
			);
		}
	}
}
