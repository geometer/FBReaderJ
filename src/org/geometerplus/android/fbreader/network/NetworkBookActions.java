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
import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.tree.NetworkAuthorTree;
import org.geometerplus.fbreader.network.tree.NetworkSeriesTree;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


class NetworkBookActions extends NetworkTreeActions {
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
	public static final int ADD_BOOK_TO_BASKET = 10;
	public static final int REMOVE_BOOK_FROM_BASKET = 11;
	public static final int ADD_CATALOG_TO_FAVORITES = 12;
	public static final int REMOVE_CATALOG_FROM_FAVORITES = 13;

	private static boolean useFullReferences(NetworkBookItem book) {
		return book.reference(UrlInfo.Type.Book) != null ||
			book.reference(UrlInfo.Type.BookConditional) != null;
	}

	private static boolean useDemoReferences(NetworkBookItem book) {
		return book.reference(UrlInfo.Type.BookDemo) != null &&
			book.localCopyFileName() == null &&
			book.reference(UrlInfo.Type.Book) == null;
	}

	private static boolean useBuyReferences(NetworkBookItem book) {
		return book.localCopyFileName() == null &&
			book.reference(UrlInfo.Type.Book) == null;
	}

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkBookTree
			|| tree instanceof NetworkAuthorTree
			|| tree instanceof NetworkSeriesTree;
	}

	@Override
	public void buildContextMenu(Activity activity, ContextMenu menu, NetworkTree tree) {
		menu.setHeaderTitle(tree.getName());
		if (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			addMenuItem(menu, SHOW_BOOKS_ITEM_ID, "showBooks");
			return;
		}

		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;

		Set<Action> actions = getContextMenuActions(book, ((NetworkBaseActivity)activity).Connection);
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
			final BookUrlInfo reference = book.reference(UrlInfo.Type.Book);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.Url)) {
				return R.drawable.ic_list_download;
			} else if (book.localCopyFileName() != null) {
				return R.drawable.ic_list_flag;
			} else if (reference != null) {
				return R.drawable.ic_list_download;
			}
		}
		if (useBuyReferences(book)
				&& book.reference(UrlInfo.Type.BookBuy) != null
				|| book.reference(UrlInfo.Type.BookBuyInBrowser) != null) {
			return R.drawable.ic_list_buy;
		}
		return 0;
	}

	static Set<Action> getContextMenuActions(NetworkBookItem book, BookDownloaderServiceConnection connection) {
		LinkedHashSet<Action> actions = new LinkedHashSet<Action>();
		if (useFullReferences(book)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.Book);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.Url)) {
				actions.add(new Action(TREE_NO_ACTION, "alreadyDownloading"));
			} else if (book.localCopyFileName() != null) {
				actions.add(new Action(READ_BOOK_ITEM_ID, "read"));
				actions.add(new Action(DELETE_BOOK_ITEM_ID, "delete"));
			} else if (reference != null) {
				actions.add(new Action(DOWNLOAD_BOOK_ITEM_ID, "download"));
			}
		}
		if (useDemoReferences(book)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.BookDemo);
			if (connection != null && connection.isBeingDownloaded(reference.Url)) {
				actions.add(new Action(TREE_NO_ACTION, "alreadyDownloadingDemo"));
			} else if (reference.localCopyFileName(UrlInfo.Type.BookDemo) != null) {
				actions.add(new Action(READ_DEMO_ITEM_ID, "readDemo"));
				actions.add(new Action(DELETE_DEMO_ITEM_ID, "deleteDemo"));
			} else {
				actions.add(new Action(DOWNLOAD_DEMO_ITEM_ID, "downloadDemo"));
			}
		}
		if (useBuyReferences(book)) {
			int id = TREE_NO_ACTION;
			BookUrlInfo reference = null;
			if (book.reference(UrlInfo.Type.BookBuy) != null) {
				reference = book.reference(UrlInfo.Type.BookBuy);
				id = BUY_DIRECTLY_ITEM_ID;
			} else if (book.reference(UrlInfo.Type.BookBuyInBrowser) != null) {
				reference = book.reference(UrlInfo.Type.BookBuyInBrowser);
				id = BUY_IN_BROWSER_ITEM_ID;
			}
			if (reference != null) {
				final String price = ((BookBuyUrlInfo)reference).Price;
				actions.add(new Action(id, "buy", price));
			}
			final Basket basket = book.Link.basket();
			if (basket != null) {
				if (basket.contains(book)) {
					actions.add(new Action(REMOVE_BOOK_FROM_BASKET, "removeFromBasket"));
				} else {
					actions.add(new Action(ADD_BOOK_TO_BASKET, "addToBasket"));
				}
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
					Util.openTree(activity, tree);
					return true;
			}
			return false;
		} else if (tree instanceof NetworkBookTree) {
			return runActionStatic(activity, ((NetworkBookTree)tree), actionCode);
		} else {
			return false;
		}
	}

	static boolean runActionStatic(final Activity activity, final NetworkBookTree tree, int actionCode) {
		switch (actionCode) {
			case SHOW_BOOK_ACTIVITY_ITEM_ID:
				if (tree.Book.isFullyLoaded()) {
					Util.openTree(activity, tree);
				} else {
					UIUtil.wait("loadInfo", new Runnable() {
						public void run() {
							try {
								tree.Book.loadFullInformation();
							} catch (ZLNetworkException e) {
								e.printStackTrace();
							}
							activity.runOnUiThread(new Runnable() {
								public void run() {
									Util.openTree(activity, tree);
								}
							});
						}
					}, activity);
				}
				return true;
			default:
				return runActionStatic(activity, tree.Book, actionCode);
		}
	}

	static boolean runActionStatic(Activity activity, NetworkBookItem book, int actionCode) {
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
			case ADD_BOOK_TO_BASKET:
				book.Link.basket().add(book);
				return true;
			case REMOVE_BOOK_FROM_BASKET:
				book.Link.basket().remove(book);
				return true;
		}
		return false;
	}

	private static void doDownloadBook(Activity activity, final NetworkBookItem book, boolean demo) {
		final UrlInfo.Type resolvedType =
			demo ? UrlInfo.Type.BookDemo : UrlInfo.Type.Book;
		final BookUrlInfo ref = book.reference(resolvedType);
		if (ref != null) {
			final String sslCertificate;
			if (book.Link.authenticationManager() != null) {
				sslCertificate = book.Link.authenticationManager().SSLCertificate;
			} else {
				sslCertificate = null;
			}
			activity.startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.Url), 
						activity.getApplicationContext(), BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanUrl())
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
			final BookUrlInfo reference = book.reference(UrlInfo.Type.BookDemo);
			if (reference != null) {
				local = reference.localCopyFileName(UrlInfo.Type.BookDemo);
			}
		}
		if (local != null) {
			activity.startActivity(
				new Intent(Intent.ACTION_VIEW,
					Uri.fromFile(new File(local)),
					activity.getApplicationContext(),
					FBReader.class
				).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
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
						final BookUrlInfo reference = book.reference(UrlInfo.Type.BookDemo);
						if (reference != null) {
							final String fileName = reference.localCopyFileName(UrlInfo.Type.BookDemo);
							if (fileName != null) {
								new File(fileName).delete();
							}
						}
					}
					NetworkView.Instance().fireModelChangedAsync();
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
						final ZLNetworkException exception = (ZLNetworkException)message.obj;
						if (exception != null) {
							String buttonKey;
							DialogInterface.OnClickListener action = null;
							if (NetworkException.ERROR_PURCHASE_NOT_ENOUGH_MONEY.equals(
								exception.getCode())
							) {
								buttonKey = "topup";
								action = new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										new TopupActions().runStandalone(activity, book.Link);
									}
								};
							} else {
								buttonKey = "ok";
							}
							final ZLResource boxResource = dialogResource.getResource("networkError");
							new AlertDialog.Builder(activity)
								.setTitle(boxResource.getResource("title").getValue())
								.setMessage(exception.getMessage())
								.setIcon(0)
								.setPositiveButton(buttonResource.getResource(buttonKey).getValue(), action)
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
						ZLNetworkException exception = null;
						try {
							mgr.purchaseBook(book);
						} catch (ZLNetworkException e) {
							exception = e;
						}
						handler.sendMessage(handler.obtainMessage(0, exception));
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
		final Runnable buyOnUiRunnable = new Runnable() {
			public void run() {
				activity.runOnUiThread(buyRunnable);
			}
		};

		try {
			if (mgr.isAuthorised(true)) {
				buyRunnable.run();
				return;
			}
		} catch (ZLNetworkException e) {
		}
		Util.runAuthenticationDialog(activity, book.Link, null, buyOnUiRunnable);
	}

	private static void doBuyInBrowser(Activity activity, final NetworkBookItem book) {
		BookUrlInfo reference = book.reference(UrlInfo.Type.BookBuyInBrowser);
		if (reference != null) {
			Util.openInBrowser(activity, reference.Url);
		}
	}

}
