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

package org.geometerplus.android.fbreader.network.action;

import java.util.*;
import java.io.File;

import android.app.AlertDialog;
import android.app.Activity;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.FBReader;

import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.tree.BasketCatalogTree;
import org.geometerplus.fbreader.network.urlInfo.*;

import org.geometerplus.android.fbreader.network.*;
import org.geometerplus.android.fbreader.network.auth.ActivityNetworkContext;

public abstract class NetworkBookActions {
	private static boolean useFullReferences(NetworkBookItem book) {
		return book.reference(UrlInfo.Type.Book) != null ||
			book.reference(UrlInfo.Type.BookConditional) != null;
	}

	private static boolean useDemoReferences(NetworkBookItem book, IBookCollection collection) {
		return book.reference(UrlInfo.Type.BookDemo) != null &&
			book.localCopyFileName(collection) == null &&
			book.reference(UrlInfo.Type.Book) == null;
	}

	public static class NBAction extends BookAction {
		private final IBookCollection myCollection;
		private final int myId;
		private final String myArg;

		public NBAction(Activity activity, IBookCollection collection, int id, String key) {
			this(activity, collection, id, key, null);
		}

		public NBAction(Activity activity, IBookCollection collection, int id, String key, String arg) {
			super(activity, id, key);
			myCollection = collection;
			myId = id;
			myArg = arg;
		}

		@Override
		public boolean isEnabled(NetworkTree tree) {
			return myId >= 0;
		}

		@Override
		public String getContextLabel(NetworkTree tree) {
			final String base = super.getContextLabel(tree);
			return myArg == null ? base : base.replace("%s", myArg);
		}

		@Override
		public void run(NetworkTree tree) {
			runActionStatic(myActivity, (NetworkBookTree)tree, myId, myCollection);
		}
	}

	public static int getBookStatus(NetworkBookItem book, IBookCollection collection, BookDownloaderServiceConnection connection) {
		if (useFullReferences(book)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.Book);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.Url)) {
				return R.drawable.ic_list_downloading;
			} else if (book.localCopyFileName(collection) != null) {
				return R.drawable.ic_list_flag;
			} else if (reference != null) {
				return R.drawable.ic_list_download;
			}
		}
		if (book.getStatus(collection) == NetworkBookItem.Status.CanBePurchased) {
			return R.drawable.ic_list_buy;
		}
		return 0;
	}

	public static List<NBAction> getContextMenuActions(Activity activity, NetworkBookTree tree, IBookCollection collection, BookDownloaderServiceConnection connection) {
		if (tree == null) {
			throw new IllegalArgumentException("tree == null");
		}
		final NetworkBookItem book = tree.Book;
		List<NBAction> actions = new LinkedList<NBAction>();
		if (useFullReferences(book)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.Book);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.Url)) {
				actions.add(new NBAction(activity, collection, ActionCode.TREE_NO_ACTION, "alreadyDownloading"));
			} else if (book.localCopyFileName(collection) != null) {
				actions.add(new NBAction(activity, collection, ActionCode.READ_BOOK, "read"));
				actions.add(new NBAction(activity, collection, ActionCode.DELETE_BOOK, "delete"));
			} else if (reference != null) {
				actions.add(new NBAction(activity, collection, ActionCode.DOWNLOAD_BOOK, "download"));
			}
		}
		if (useDemoReferences(book, collection)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.BookDemo);
			if (connection != null && connection.isBeingDownloaded(reference.Url)) {
				actions.add(new NBAction(activity, collection, ActionCode.TREE_NO_ACTION, "alreadyDownloadingDemo"));
			} else if (reference.localCopyFileName(UrlInfo.Type.BookDemo) != null) {
				actions.add(new NBAction(activity, collection, ActionCode.READ_DEMO, "readDemo"));
				actions.add(new NBAction(activity, collection, ActionCode.DELETE_DEMO, "deleteDemo"));
			} else {
				actions.add(new NBAction(activity, collection, ActionCode.DOWNLOAD_DEMO, "downloadDemo"));
			}
		}
		if (book.getStatus(collection) == NetworkBookItem.Status.CanBePurchased) {
			final BookBuyUrlInfo reference = book.buyInfo();
			final int id = reference.InfoType == UrlInfo.Type.BookBuy
				? ActionCode.BUY_DIRECTLY : ActionCode.BUY_IN_BROWSER;
			final String priceString = reference.Price != null ? String.valueOf(reference.Price) : "";
			actions.add(new NBAction(activity, collection, id, "buy", priceString));
			final BasketItem basketItem = book.Link.getBasketItem();
			if (basketItem != null) {
				if (basketItem.contains(book)) {
					if (tree.Parent instanceof BasketCatalogTree ||
						activity instanceof NetworkLibraryActivity) {
						actions.add(new NBAction(activity, collection, ActionCode.REMOVE_BOOK_FROM_BASKET, "removeFromBasket"));
					} else {
						actions.add(new NBAction(activity, collection, ActionCode.OPEN_BASKET, "openBasket"));
					}
				} else {
					actions.add(new NBAction(activity, collection, ActionCode.ADD_BOOK_TO_BASKET, "addToBasket"));
				}
			}
		}
		return actions;
	}

	private static boolean runActionStatic(Activity activity, NetworkBookTree tree, int actionCode, IBookCollection collection) {
		final NetworkBookItem book = tree.Book;
		switch (actionCode) {
			case ActionCode.DOWNLOAD_BOOK:
				Util.doDownloadBook(activity, book, false);
				return true;
			case ActionCode.DOWNLOAD_DEMO:
				Util.doDownloadBook(activity, book, true);
				return true;
			case ActionCode.READ_BOOK:
				doReadBook(activity, book, collection, false);
				return true;
			case ActionCode.READ_DEMO:
				doReadBook(activity, book, collection, true);
				return true;
			case ActionCode.DELETE_BOOK:
				tryToDeleteBook(activity, book, false);
				return true;
			case ActionCode.DELETE_DEMO:
				tryToDeleteBook(activity, book, true);
				return true;
			case ActionCode.BUY_DIRECTLY:
				doBuyDirectly(activity, tree);
				return true;
			case ActionCode.BUY_IN_BROWSER:
				doBuyInBrowser(activity, book);
				return true;
			case ActionCode.ADD_BOOK_TO_BASKET:
				book.Link.getBasketItem().add(book);
				return true;
			case ActionCode.REMOVE_BOOK_FROM_BASKET:
				book.Link.getBasketItem().remove(book);
				return true;
			case ActionCode.OPEN_BASKET:
				new OpenCatalogAction(activity, new ActivityNetworkContext(activity))
					.run(Util.networkLibrary(activity).getFakeBasketTree(book.Link.getBasketItem()));
				return true;
		}
		return false;
	}

	private static void doReadBook(Activity activity, final NetworkBookItem book, IBookCollection collection, boolean demo) {
		String local = null;
		if (!demo) {
			local = book.localCopyFileName(collection);
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

	private static void tryToDeleteBook(final Activity activity, final NetworkBookItem book, final boolean demo) {
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
					Util.networkLibrary(activity).fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
				}
			})
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private static void doBuyDirectly(Activity activity, NetworkBookTree tree) {
		BuyBooksActivity.run(activity, tree);
	}

	private static void doBuyInBrowser(Activity activity, final NetworkBookItem book) {
		BookUrlInfo reference = book.reference(UrlInfo.Type.BookBuyInBrowser);
		if (reference != null) {
			Util.openInBrowser(activity, reference.Url);
		}
	}
}
