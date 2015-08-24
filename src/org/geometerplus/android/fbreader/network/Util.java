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

package org.geometerplus.android.fbreader.network;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.util.SystemInfo;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public abstract class Util implements UserRegistrationConstants {
	static final String AUTHORISATION_ACTION = "android.fbreader.action.network.AUTHORISATION";
	public static final String SIGNIN_ACTION = "android.fbreader.action.network.SIGNIN";
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

	public static NetworkLibrary networkLibrary(Context context) {
		return NetworkLibrary.Instance(Paths.systemInfo(context));
	}

	static void initLibrary(final Activity activity, final ZLNetworkContext nc, final Runnable action) {
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				UIUtil.wait("loadingNetworkLibrary", new Runnable() {
					public void run() {
						final NetworkLibrary library = networkLibrary(activity);
						if (SQLiteNetworkDatabase.Instance() == null) {
							new SQLiteNetworkDatabase(activity.getApplication(), library);
						}

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

	public static Intent authorisationIntent(INetworkLink link, Activity activity, Class<? extends Activity> cls) {
		final Intent intent = new Intent(activity, cls);
		intent.putExtra(CATALOG_URL, link.getUrl(UrlInfo.Type.Catalog));
		intent.putExtra(SIGNIN_URL, link.getUrl(UrlInfo.Type.SignIn));
		intent.putExtra(SIGNUP_URL, link.getUrl(UrlInfo.Type.SignUp));
		intent.putExtra(RECOVER_PASSWORD_URL, link.getUrl(UrlInfo.Type.RecoverPassword));
		return intent;
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

	public static boolean isOurLink(String url) {
		try {
			return Uri.parse(url).getHost().endsWith(".fbreader.org");
		} catch (Throwable t) {
			return false;
		}
	}

	public static void openInBrowser(Activity activity, String url) {
		if (url != null) {
			url = networkLibrary(activity).rewriteUrl(url, true);
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
					.putExtra(BookDownloaderService.Key.BOOK_MIME, ref.Mime.toString())
					.putExtra(BookDownloaderService.Key.BOOK_KIND, resolvedType)
					.putExtra(BookDownloaderService.Key.CLEAN_URL, ref.cleanUrl())
					.putExtra(BookDownloaderService.Key.BOOK_TITLE, book.Title)
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
