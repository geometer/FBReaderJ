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

	private boolean useFullReferences(NetworkBookItem book) {
		return book.reference(BookReference.Type.DOWNLOAD_FULL) != null ||
			book.reference(BookReference.Type.DOWNLOAD_FULL_CONDITIONAL) != null;
	}

	private boolean useDemoReferences(NetworkBookItem book) {
		return book.reference(BookReference.Type.DOWNLOAD_DEMO) != null &&
			book.localCopyFileName() == null &&
			book.reference(BookReference.Type.DOWNLOAD_FULL) == null;
	}

	private boolean useBuyReferences(NetworkBookItem book) {
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
		if (useFullReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_FULL);
			if (reference != null && NetworkLibraryActivity.Instance.isBeingDownloaded(reference.URL)) {
				addMenuItem(menu, -1, "alreadyDownloading").setEnabled(false);
			} else if (book.localCopyFileName() != null) {
				addMenuItem(menu, READ_BOOK_ITEM_ID, "read");
				addMenuItem(menu, DELETE_BOOK_ITEM_ID, "delete");
			} else if (reference != null) {
				addMenuItem(menu, DOWNLOAD_BOOK_ITEM_ID, "download");
			}
		}
		if (useDemoReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
			if (NetworkLibraryActivity.Instance.isBeingDownloaded(reference.URL)) {
				addMenuItem(menu, -1, "alreadyDownloadingDemo").setEnabled(false);
			} else if (reference.localCopyFileName(BookReference.Type.DOWNLOAD_DEMO) != null) {
				addMenuItem(menu, READ_DEMO_ITEM_ID, "readDemo");
				addMenuItem(menu, DELETE_DEMO_ITEM_ID, "deleteDemo");
			} else {
				addMenuItem(menu, DOWNLOAD_DEMO_ITEM_ID, "downloadDemo");
			}
		}
		if (useBuyReferences(book)) {
			int id = -1;
			BookReference reference = null;
			if (book.reference(BookReference.Type.BUY) != null) {
				reference = book.reference(BookReference.Type.BUY);
				id = BUY_DIRECTLY_ITEM_ID;
			} else if (book.reference(BookReference.Type.BUY_IN_BROWSER) != null) {
				reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
				id = BUY_IN_BROWSER_ITEM_ID;
			}
			if (reference != null) {
				String price = ((BuyBookReference) reference).Price;
				addMenuItem(menu, id, "buy", price);
			}
		}
	}

	@Override
	public int getDefaultActionCode(NetworkTree tree) {
		return SHOW_BOOK_ACTIVITY_ITEM_ID;
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
		switch (actionCode) {
		case READ_BOOK_ITEM_ID:
			return getConfirmValue("read");
		case DOWNLOAD_BOOK_ITEM_ID:
			return getConfirmValue("download");
		case READ_DEMO_ITEM_ID:
			return getConfirmValue("readDemo");
		case DOWNLOAD_DEMO_ITEM_ID:
			return getConfirmValue("downloadDemo");
		case BUY_DIRECTLY_ITEM_ID:
			return getConfirmValue("buy", ((BuyBookReference) book.reference(BookReference.Type.BUY)).Price);
		case BUY_IN_BROWSER_ITEM_ID:
			return getConfirmValue("buy", ((BuyBookReference) book.reference(BookReference.Type.BUY_IN_BROWSER)).Price);
		}
		return null;
	}

	@Override
	public boolean runAction(NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case DOWNLOAD_BOOK_ITEM_ID:
				doDownloadBook(tree, false);
				return true;
			case DOWNLOAD_DEMO_ITEM_ID:
				doDownloadBook(tree, true);
				return true;
			case READ_BOOK_ITEM_ID:
				doReadBook(tree, false);
				return true;
			case READ_DEMO_ITEM_ID:
				doReadBook(tree, true);
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(tree, false);
				return true;
			case DELETE_DEMO_ITEM_ID:
				tryToDeleteBook(tree, true);
				return true;
			case BUY_DIRECTLY_ITEM_ID:
				doBuyDirectly(tree);
				return true;
			case BUY_IN_BROWSER_ITEM_ID:
				doBuyInBrowser(tree);
				return true;
			case SHOW_BOOK_ACTIVITY_ITEM_ID:
				NetworkLibraryActivity.Instance.showBookInfoActivity(((NetworkBookTree) tree).Book);
				return true;
		}
		return false;
	}

	private void doDownloadBook(NetworkTree tree, boolean demo) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		int resolvedType = demo ? BookReference.Type.DOWNLOAD_DEMO : BookReference.Type.DOWNLOAD_FULL;
		BookReference ref = book.reference(resolvedType);
		if (ref != null) {
			final String sslCertificate;
			if (book.Link.authenticationManager() != null) {
				sslCertificate = book.Link.authenticationManager().SSLCertificate;
			} else {
				sslCertificate = null;
			}
			NetworkLibraryActivity.Instance.startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.URL), NetworkLibraryActivity.Instance, BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanURL())
					.putExtra(BookDownloaderService.TITLE_KEY, book.Title)
					.putExtra(BookDownloaderService.SSL_CERTIFICATE_KEY, sslCertificate)
			);
		}
	}

	private void doReadBook(NetworkTree tree, boolean demo) {
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
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
			NetworkLibraryActivity.Instance.startActivity(
				new Intent(Intent.ACTION_VIEW,
					Uri.fromFile(new File(local)),
					NetworkLibraryActivity.Instance,
					org.geometerplus.android.fbreader.FBReader.class
				).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			);
		}
	}

	private void tryToDeleteBook(NetworkTree tree, final boolean demo) {
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(NetworkLibraryActivity.Instance)
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
				}
			})
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private void doBuyDirectly(NetworkTree tree) {
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
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
				final boolean downloadBook = which == DialogInterface.BUTTON_NEUTRAL;
				if (mgr.needPurchase(book)) {
					final Handler handler = new Handler() {
						public void handleMessage(Message message) {
							String err = (String) message.obj;
							if (err != null) {
								final ZLResource boxResource = dialogResource.getResource("networkError");
								new AlertDialog.Builder(NetworkLibraryActivity.Instance)
									.setTitle(boxResource.getResource("title").getValue())
									.setMessage(err)
									.setIcon(0)
									.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
									.create().show();
							} else if (downloadBook) {
								doDownloadBook(bookTree, false);
							}
							if (mgr.isAuthorised(true).Status == ZLBoolean3.B3_FALSE) {
								final NetworkLibrary library = NetworkLibrary.Instance();
								library.invalidateAccountDependents();
								library.synchronize();
							}
							if (NetworkLibraryActivity.Instance != null) {
								NetworkLibraryActivity.Instance.getAdapter().resetTree();
								NetworkLibraryActivity.Instance.getListView().invalidateViews();
							}
						}
					};
					final Runnable runnable = new Runnable() {
						public void run() {
							String err = mgr.purchaseBook(book);
							handler.sendMessage(handler.obtainMessage(0, err));
						}
					};
					((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("purchaseBook", runnable, NetworkLibraryActivity.Instance);
				}
			}
		};

		final Runnable buyRunnable = new Runnable() {
			public void run() {
				if (!mgr.needPurchase(book)) {
					return;
				}
				final ZLResource boxResource = dialogResource.getResource("purchaseConfirmBox");
				new AlertDialog.Builder(NetworkLibraryActivity.Instance)
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

	private void doBuyInBrowser(NetworkTree tree) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
		if (reference != null) {
			NetworkLibraryActivity.Instance.openInBrowser(reference.URL);
		}
	}

}
