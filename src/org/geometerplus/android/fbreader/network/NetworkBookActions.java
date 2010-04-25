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
import android.os.Message;
import android.os.Handler;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.authentication.*;


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
		return tree instanceof NetworkBookTree;
	}

	@Override
	public void buildContextMenu(ContextMenu menu, NetworkTree tree) {
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
		menu.setHeaderTitle(tree.getName());

		Set<Action> actions = getContextMenuActions(book);
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

	static Set<Action> getContextMenuActions(NetworkBookItem book) {
		LinkedHashSet<Action> actions = new LinkedHashSet<Action>();
		if (useFullReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_FULL);
			if (reference != null && NetworkLibraryActivity.Instance.isBeingDownloaded(reference.URL)) {
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
			if (NetworkLibraryActivity.Instance.isBeingDownloaded(reference.URL)) {
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
	public int getDefaultActionCode(NetworkTree tree) {
		return SHOW_BOOK_ACTIVITY_ITEM_ID;
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		return null;
	}

	@Override
	public boolean runAction(NetworkTree tree, int actionCode) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		return runAction(book, actionCode);
	}

	static boolean runAction(NetworkBookItem book, int actionCode) {
		switch (actionCode) {
			case DOWNLOAD_BOOK_ITEM_ID:
				doDownloadBook(book, false);
				return true;
			case DOWNLOAD_DEMO_ITEM_ID:
				doDownloadBook(book, true);
				return true;
			case READ_BOOK_ITEM_ID:
				doReadBook(book, false);
				return true;
			case READ_DEMO_ITEM_ID:
				doReadBook(book, true);
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book, false);
				return true;
			case DELETE_DEMO_ITEM_ID:
				tryToDeleteBook(book, true);
				return true;
			case BUY_DIRECTLY_ITEM_ID:
				doBuyDirectly(book);
				return true;
			case BUY_IN_BROWSER_ITEM_ID:
				doBuyInBrowser(book);
				return true;
			case SHOW_BOOK_ACTIVITY_ITEM_ID:
				if (NetworkLibraryActivity.Instance != null) {
					NetworkLibraryActivity.Instance.showBookInfoActivity(book);
				}
				return true;
		}
		return false;
	}

	private static void doDownloadBook(final NetworkBookItem book, boolean demo) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		int resolvedType = demo ? BookReference.Type.DOWNLOAD_DEMO : BookReference.Type.DOWNLOAD_FULL;
		BookReference ref = book.reference(resolvedType);
		if (ref != null) {
			final String sslCertificate;
			if (book.Link.authenticationManager() != null) {
				sslCertificate = book.Link.authenticationManager().SSLCertificate;
			} else {
				sslCertificate = null;
			}
			NetworkLibraryActivity.Instance.getTopLevelActivity().startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.URL), 
						NetworkLibraryActivity.Instance.getApplicationContext(), BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanURL())
					.putExtra(BookDownloaderService.TITLE_KEY, book.Title)
					.putExtra(BookDownloaderService.SSL_CERTIFICATE_KEY, sslCertificate)
			);
		}
	}

	private static void doReadBook(final NetworkBookItem book, boolean demo) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
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
			NetworkLibraryActivity.Instance.getTopLevelActivity().startActivity(
				new Intent(Intent.ACTION_VIEW,
					Uri.fromFile(new File(local)),
					NetworkLibraryActivity.Instance.getApplicationContext(),
					org.geometerplus.android.fbreader.FBReader.class
				).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			);
		}
	}

	private static void tryToDeleteBook(final NetworkBookItem book, final boolean demo) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(NetworkLibraryActivity.Instance.getTopLevelActivity())
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
					if (NetworkLibraryActivity.Instance != null) {
						NetworkLibraryActivity.Instance.fireOnModelChanged();
					}
				}
			})
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private static void doBuyDirectly(final NetworkBookItem book) {
		final NetworkAuthenticationManager mgr = book.Link.authenticationManager();
		if (mgr == null) {
			return;
		}
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		/*if (!NetworkOperationRunnable::tryConnect()) {
			return;
		}*/


		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (NetworkLibraryActivity.Instance == null) {
					return;
				}
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
							new AlertDialog.Builder(NetworkLibraryActivity.Instance.getTopLevelActivity())
								.setTitle(boxResource.getResource("title").getValue())
								.setMessage(err)
								.setIcon(0)
								.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
								.create().show();
						} else if (downloadBook) {
							doDownloadBook(book, false);
						}
						if (mgr.isAuthorised(true).Status == ZLBoolean3.B3_FALSE) {
							final NetworkLibrary library = NetworkLibrary.Instance();
							library.invalidateAccountDependents();
							library.synchronize();
						}
						if (NetworkLibraryActivity.Instance != null) {
							NetworkLibraryActivity.Instance.getAdapter().resetTree();
							NetworkLibraryActivity.Instance.fireOnModelChanged();
						}
					}
				}; // end Handler
				final Runnable runnable = new Runnable() {
					public void run() {
						String err = mgr.purchaseBook(book);
						handler.sendMessage(handler.obtainMessage(0, err));
					}
				}; // end Runnable
				((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("purchaseBook", runnable, NetworkLibraryActivity.Instance.getTopLevelActivity());
			} // end onClick
		}; // end listener

		final Runnable buyRunnable = new Runnable() {
			public void run() {
				if (NetworkLibraryActivity.Instance == null) {
					return;
				}
				if (!mgr.needPurchase(book)) {
					// TODO: make dialog
					return;
				}
				final ZLResource boxResource = dialogResource.getResource("purchaseConfirmBox");
				new AlertDialog.Builder(NetworkLibraryActivity.Instance.getTopLevelActivity())
					.setTitle(boxResource.getResource("title").getValue())
					.setMessage(boxResource.getResource("message").getValue().replace("%s", book.Title))
					.setIcon(0)
					.setPositiveButton(buttonResource.getResource("buy").getValue(), listener)
					.setNeutralButton(buttonResource.getResource("buyAndDownload").getValue(), listener)
					.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
					.create().show();
			}
		};

		if (mgr.isAuthorised(true).Status != ZLBoolean3.B3_TRUE) {
			AuthenticationDialog.Instance().show(book.Link, buyRunnable);
			return;
		} else {
			buyRunnable.run();
		}
	}

	private static void doBuyInBrowser(final NetworkBookItem book) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
		if (reference != null) {
			NetworkLibraryActivity.Instance.openInBrowser(reference.URL);
		}
	}

}
