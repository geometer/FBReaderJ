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

package org.geometerplus.android.fbreader.network;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.options.Config;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class Util implements UserRegistrationConstants {
	static final String AUTHORIZATION_ACTION = "android.fbreader.action.network.AUTHORIZATION";
	static final String SIGNIN_ACTION = "android.fbreader.action.network.SIGNIN";
	static final String TOPUP_ACTION = "android.fbreader.action.network.TOPUP";
	static final String EXTRA_CATALOG_ACTION = "android.fbreader.action.network.EXTRA_CATALOG";

	public static final String ADD_CATALOG_ACTION = "android.fbreader.action.ADD_OPDS_CATALOG";
	public static final String ADD_CATALOG_URL_ACTION = "android.fbreader.action.ADD_OPDS_CATALOG_URL";
	public static final String EDIT_CATALOG_ACTION = "android.fbreader.action.EDIT_OPDS_CATALOG";

	public static Intent intentByLink(Intent intent, INetworkLink link) {
		if (link != null) {
			intent.setData(Uri.parse(link.getUrl(UrlInfo.Type.Catalog)));
		}
		return intent;
	}

	static void initLibrary(final Activity activity, final ZLNetworkContext nc, final Runnable action) {
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				UIUtil.wait("loadingNetworkLibrary", new Runnable() {
					public void run() {
						if (SQLiteNetworkDatabase.Instance() == null) {
							new SQLiteNetworkDatabase(activity.getApplication());
						}

						final NetworkLibrary library = NetworkLibrary.Instance();
						if (!library.isInitialized()) {
							try {
								library.initialize(nc);
							} catch (ZLNetworkException e) {
							}
						}
						if (action != null) {
							action.run();
						}
					}
				}, activity);
			}
		});
	}

	static Intent authorisationIntent(INetworkLink link, Uri id) {
		final Intent intent = new Intent(AUTHORIZATION_ACTION, id);
		intent.putExtra(CATALOG_URL, link.getUrl(UrlInfo.Type.Catalog));
		intent.putExtra(SIGNIN_URL, link.getUrl(UrlInfo.Type.SignIn));
		intent.putExtra(SIGNUP_URL, link.getUrl(UrlInfo.Type.SignUp));
		intent.putExtra(RECOVER_PASSWORD_URL, link.getUrl(UrlInfo.Type.RecoverPassword));
		return intent;
	}

	private static Intent registrationIntent(INetworkLink link) {
		return authorisationIntent(link, Uri.parse(link.getUrl(UrlInfo.Type.Catalog) + "/register"));
	}

	public static boolean isRegistrationSupported(Activity activity, INetworkLink link) {
		return PackageUtil.canBeStarted(activity, registrationIntent(link), true);
	}

	public static void runRegistrationDialog(Activity activity, INetworkLink link) {
		try {
			final Intent intent = registrationIntent(link);
			if (PackageUtil.canBeStarted(activity, intent, true)) {
				activity.startActivity(intent);
			}
		} catch (ActivityNotFoundException e) {
		}
	}

	public static void runAuthenticationDialog(Activity activity, INetworkLink link, Runnable onSuccess) {
		final NetworkAuthenticationManager mgr = link.authenticationManager();

		final Intent intent = intentByLink(new Intent(activity, AuthenticationActivity.class), link);
		AuthenticationActivity.registerRunnable(intent, onSuccess);
		intent.putExtra(AuthenticationActivity.USERNAME_KEY, mgr.getUserName());
		intent.putExtra(AuthenticationActivity.SCHEME_KEY, "https");
		intent.putExtra(AuthenticationActivity.CUSTOM_AUTH_KEY, true);
		activity.startActivity(intent);
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
					.putExtra(BookDownloaderService.BOOK_MIME, ref.Mime.toString())
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanUrl())
					.putExtra(BookDownloaderService.TITLE_KEY, book.Title)
			);
		}
	}

	public static Uri rewriteUri(Uri uri) {
		if (uri == null) {
			return null;
		}

		if ("http".equals(uri.getScheme()) &&
			"www.litres.ru".equals(uri.getHost()) &&
			"/pages/biblio_book/".equals(uri.getPath())) {
			final String bookId = uri.getQueryParameter("art");
			if (bookId != null && !"".equals(bookId)) {
				return Uri.parse("litres-book://data.fbreader.org/catalogs/litres2/full.php5?id=" + bookId);
			}
		}
		return uri;
	}
}
