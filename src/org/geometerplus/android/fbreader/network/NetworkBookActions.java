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

import java.util.Set;
import java.util.LinkedHashSet;
import java.io.File;

import android.app.AlertDialog;
import android.app.Activity;
import android.os.Message;
import android.os.Handler;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.fbreader.FBReader;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.tree.NetworkAuthorTree;
import org.geometerplus.fbreader.network.tree.NetworkSeriesTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class NetworkBookActions extends NetworkTreeActions {
	private static final String PACKAGE = "org.geometerplus.android.fbreader.network";

	public static final int DOWNLOAD_BOOK_ITEM_ID = 0;
	public static final int DOWNLOAD_DEMO_ITEM_ID = 1;
	public static final int READ_BOOK_ITEM_ID = 2;
	public static final int READ_DEMO_ITEM_ID = 3;
	public static final int DELETE_BOOK_ITEM_ID = 4;
	public static final int DELETE_DEMO_ITEM_ID = 5;
	public static final int BUY_DIRECTLY_ITEM_ID = 6;
	public static final int BUY_IN_BROWSER_ITEM_ID = 7;
	public static final int SHOW_BOOK_ACTIVITY_ITEM_ID = 8;

	public static final int SHOW_BOOKS_ITEM_ID = 9;

	private static boolean useFullReferences(NetworkBookItem book) {
		return book.reference(BookReference.Type.DOWNLOAD_FULL) != null ||
			book.reference(BookReference.Type.DOWNLOAD_FULL_CONDITIONAL) != null;
	}

	private static boolean useDemoReferences(NetworkBookItem book) {
		return book.reference(BookReference.Type.DOWNLOAD_DEMO) != null &&
			book.localCopyFileName() == null &&
			book.reference(BookReference.Type.DOWNLOAD_FULL) == null;
	}

	private static boolean useBuyReferences(NetworkBookItem book) {
		return book.localCopyFileName() == null &&
			book.reference(BookReference.Type.DOWNLOAD_FULL) == null;
	}

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkBookTree
			|| tree instanceof NetworkAuthorTree
			|| tree instanceof NetworkSeriesTree;
	}

	@Override
	public void buildContextMenu(NetworkBaseActivity activity, ContextMenu menu, NetworkTree tree) {
		menu.setHeaderTitle(tree.getName());
		if (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			addMenuItem(menu, SHOW_BOOKS_ITEM_ID, "showBooks");
			return;
		}

		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;

		Set<Action> actions = getContextMenuActions(book, activity.Connection);
		for (Action a: actions) {
			if (a.Arg == null) {
				addMenuItem(menu, a.Id, a.Key);
			} else {
				addMenuItem(menu, a.Id, a.Key, a.Arg);
			}
		}
	}

	static class Action {
		public final int Id;
		public final String Key;
		public final String Arg;

		public Action(int id, String key) {
			Id = id;
			Key = key;
			Arg = null;
		}

		public Action(int id, String key, String arg) {
			Id = id;
			Key = key;
			Arg = arg;
		}
	}

	static int getBookStatus(NetworkBookItem book, BookDownloaderServiceConnection connection) {
		if (useFullReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_FULL);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.URL)) {
				return R.drawable.ic_list_download;
			} else if (book.localCopyFileName() != null) {
				return R.drawable.ic_list_flag;
			} else if (reference != null) {
				return R.drawable.ic_list_download;
			}
		}
		if (useBuyReferences(book)
				&& book.reference(BookReference.Type.BUY) != null
				|| book.reference(BookReference.Type.BUY_IN_BROWSER) != null) {
			return R.drawable.ic_list_buy;
		}
		return 0;
	}

	static Set<Action> getContextMenuActions(NetworkBookItem book, BookDownloaderServiceConnection connection) {
		LinkedHashSet<Action> actions = new LinkedHashSet<Action>();
		if (useFullReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_FULL);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.URL)) {
				actions.add(new Action(TREE_NO_ACTION, "alreadyDownloading"));
			} else if (book.localCopyFileName() != null) {
				actions.add(new Action(READ_BOOK_ITEM_ID, "read"));
				actions.add(new Action(DELETE_BOOK_ITEM_ID, "delete"));
			} else if (reference != null) {
				actions.add(new Action(DOWNLOAD_BOOK_ITEM_ID, "download"));
			}
		}
		if (useDemoReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
			if (connection != null && connection.isBeingDownloaded(reference.URL)) {
				actions.add(new Action(TREE_NO_ACTION, "alreadyDownloadingDemo"));
			} else if (reference.localCopyFileName(BookReference.Type.DOWNLOAD_DEMO) != null) {
				actions.add(new Action(READ_DEMO_ITEM_ID, "readDemo"));
				actions.add(new Action(DELETE_DEMO_ITEM_ID, "deleteDemo"));
			} else {
				actions.add(new Action(DOWNLOAD_DEMO_ITEM_ID, "downloadDemo"));
			}
		}
		if (useBuyReferences(book)) {
			int id = TREE_NO_ACTION;
			BookReference reference = null;
			if (book.reference(BookReference.Type.BUY) != null) {
				reference = book.reference(BookReference.Type.BUY);
				id = BUY_DIRECTLY_ITEM_ID;
			} else if (book.reference(BookReference.Type.BUY_IN_BROWSER) != null) {
				reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
				id = BUY_IN_BROWSER_ITEM_ID;
			}
			if (reference != null) {
				final String price = ((BuyBookReference) reference).Price;
				actions.add(new Action(id, "buy", price));
			}
		}
		return actions;
	}

	@Override
	public int getDefaultActionCode(NetworkBaseActivity activity, NetworkTree tree) {
		if (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			return SHOW_BOOKS_ITEM_ID;
		}
		return SHOW_BOOK_ACTIVITY_ITEM_ID;
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		return null;
	}

	@Override
	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean prepareOptionsMenu(NetworkBaseActivity activity, Menu menu, NetworkTree tree) {
		return false;
	}

	@Override
	public boolean runAction(NetworkBaseActivity activity, NetworkTree tree, int actionCode) {
		if (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			switch (actionCode) {
			case SHOW_BOOKS_ITEM_ID:
				showBooks(activity, tree);
				return true;
			}
			return false;
		}
		return runAction(activity, ((NetworkBookTree) tree).Book, actionCode);
	}


	private void showBooks(NetworkBaseActivity activity, NetworkTree tree) {
		String key = null;
		if (tree instanceof NetworkAuthorTree) {
			key = PACKAGE + ".Authors:" + ((NetworkAuthorTree) tree).Author.DisplayName;
		} else if (tree instanceof NetworkSeriesTree) {
			key = PACKAGE + ".Series:" + ((NetworkSeriesTree) tree).SeriesTitle;
		}
		if (key != null) {
			NetworkView.Instance().openTree(activity, tree, key);
		}
	}

	static boolean runAction(Activity activity, NetworkBookItem book, int actionCode) {
		switch (actionCode) {
			case DOWNLOAD_BOOK_ITEM_ID:
				doDownloadBook(activity, book, false);
				return true;
			case DOWNLOAD_DEMO_ITEM_ID:
				doDownloadBook(activity, book, true);
				return true;
			case READ_BOOK_ITEM_ID:
				doReadBook(activity, book, false);
				return true;
			case READ_DEMO_ITEM_ID:
				doReadBook(activity, book, true);
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(activity, book, false);
				return true;
			case DELETE_DEMO_ITEM_ID:
				tryToDeleteBook(activity, book, true);
				return true;
			case BUY_DIRECTLY_ITEM_ID:
				doBuyDirectly(activity, book);
				return true;
			case BUY_IN_BROWSER_ITEM_ID:
				doBuyInBrowser(activity, book);
				return true;
			case SHOW_BOOK_ACTIVITY_ITEM_ID:
				NetworkView.Instance().showBookInfoActivity(activity, book);
				return true;
		}
		return false;
	}

	private static void doDownloadBook(Activity activity, final NetworkBookItem book, boolean demo) {
		int resolvedType = demo ? BookReference.Type.DOWNLOAD_DEMO : BookReference.Type.DOWNLOAD_FULL;
		BookReference ref = book.reference(resolvedType);
		if (ref != null) {
			final String sslCertificate;
			if (book.Link.authenticationManager() != null) {
				sslCertificate = book.Link.authenticationManager().SSLCertificate;
			} else {
				sslCertificate = null;
			}
			activity.startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.URL), 
						activity.getApplicationContext(), BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanURL())
					.putExtra(BookDownloaderService.TITLE_KEY, book.Title)
					.putExtra(BookDownloaderService.SSL_CERTIFICATE_KEY, sslCertificate)
			);
		}
	}

	private static void doReadBook(Activity activity, final NetworkBookItem book, boolean demo) {
		String local = null;
		if (!demo) {
			local = book.localCopyFileName();
		} else {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
			if (reference != null) {
				local = reference.localCopyFileName(BookReference.Type.DOWNLOAD_DEMO);
			}
		}
		if (local != null) {
			activity.startActivity(
				new Intent(Intent.ACTION_VIEW,
					Uri.fromFile(new File(local)),
					activity.getApplicationContext(),
					FBReader.class
				).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			);
		}
	}

	private static void tryToDeleteBook(Activity activity, final NetworkBookItem book, final boolean demo) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(activity)
			.setTitle(book.Title)
			.setMessage(boxResource.getResource("message").getValue())
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO: remove information about book from Library???
					if (!demo) {
						book.removeLocalFiles();
					} else {
						final BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
						if (reference != null) {
							final String fileName = reference.localCopyFileName(BookReference.Type.DOWNLOAD_DEMO);
							if (fileName != null) {
								new File(fileName).delete();
							}
						}
					}
					if (NetworkView.Instance().isInitialized()) {
						NetworkView.Instance().fireModelChangedAsync();
					}
				}
			})
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private static void doBuyDirectly(final Activity activity, final NetworkBookItem book) {
		final NetworkAuthenticationManager mgr = book.Link.authenticationManager();
		if (mgr == null) {
			return;
		}
		/*if (!NetworkOperationRunnable::tryConnect()) {
			return;
		}*/


		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_NEGATIVE) {
					return;
				}
				if (!mgr.needPurchase(book)) {
					return;
				}
				final boolean downloadBook = which == DialogInterface.BUTTON_NEUTRAL;
				final Handler handler = new Handler() {
					public void handleMessage(Message message) {
						String err = (String) message.obj;
						if (err != null) {
							final ZLResource boxResource = dialogResource.getResource("networkError");
							new AlertDialog.Builder(activity)
								.setTitle(boxResource.getResource("title").getValue())
								.setMessage(err)
								.setIcon(0)
								.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
								.create().show();
						} else if (downloadBook) {
							doDownloadBook(activity, book, false);
						}
						if (!mgr.mayBeAuthorised(true)) {
							final NetworkLibrary library = NetworkLibrary.Instance();
							library.invalidateVisibility();
							library.synchronize();
						}
						if (NetworkView.Instance().isInitialized()) {
							NetworkView.Instance().fireModelChangedAsync();
						}
					}
				}; // end Handler
				final Runnable runnable = new Runnable() {
					public void run() {
						String error = null;
						try {
							mgr.purchaseBook(book);
						} catch (ZLNetworkException e) {
							error = e.getMessage();
						}
						handler.sendMessage(handler.obtainMessage(0, error));
					}
				}; // end Runnable
				UIUtil.wait("purchaseBook", runnable, activity);
			} // end onClick
		}; // end listener

		final Runnable buyRunnable = new Runnable() {
			public void run() {
				if (!mgr.needPurchase(book)) {
					final ZLResource boxResource = dialogResource.getResource("alreadyPurchasedBox");
					new AlertDialog.Builder(activity)
						.setTitle(boxResource.getResource("title").getValue())
						.setMessage(boxResource.getResource("message").getValue())
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
						.create().show();
					return;
				}
				final ZLResource boxResource = dialogResource.getResource("purchaseConfirmBox");
				new AlertDialog.Builder(activity)
					.setTitle(boxResource.getResource("title").getValue())
					.setMessage(boxResource.getResource("message").getValue().replace("%s", book.Title))
					.setIcon(0)
					.setPositiveButton(buttonResource.getResource("buy").getValue(), listener)
					.setNeutralButton(buttonResource.getResource("buyAndDownload").getValue(), listener)
					.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
					.create().show();
			}
		};

		try {
			if (mgr.isAuthorised(true)) {
				buyRunnable.run();
				return;
			}
		} catch (ZLNetworkException e) {
		}
		NetworkDialog.show(activity, NetworkDialog.DIALOG_AUTHENTICATION, book.Link, buyRunnable);
	}

	private static void doBuyInBrowser(Activity activity, final NetworkBookItem book) {
		BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
		if (reference != null) {
			NetworkView.Instance().openInBrowser(activity, reference.URL);
		}
	}

}
