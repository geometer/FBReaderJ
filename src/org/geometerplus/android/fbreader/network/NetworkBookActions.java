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
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;


class NetworkBookActions extends NetworkTreeActions {

	public static final int DOWNLOAD_BOOK_ITEM_ID = 0;
	public static final int DOWNLOAD_DEMO_ITEM_ID = 1;
	public static final int READ_BOOK_ITEM_ID = 2;
	public static final int READ_DEMO_ITEM_ID = 3;
	public static final int DELETE_BOOK_ITEM_ID = 4;
	public static final int DELETE_DEMO_ITEM_ID = 5;
	public static final int BUY_DIRECTLY_ITEM_ID = 6;
	public static final int BUY_IN_BROWSER_ITEM_ID = 7;

	protected NetworkBookActions(NetworkLibraryActivity activity) {
		super(activity);
	}

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
			if (reference != null && myActivity.isBeingDownloaded(reference.URL)) {
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
			if (myActivity.isBeingDownloaded(reference.URL)) {
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
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
		if (useFullReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_FULL);
			if (reference == null || !myActivity.isBeingDownloaded(reference.URL)) {
				if (book.localCopyFileName() != null) {
					return READ_BOOK_ITEM_ID;
				} else if (reference != null) {
					return DOWNLOAD_BOOK_ITEM_ID;
				}
			}
		}
		if (useDemoReferences(book)) {
			BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
			if (!myActivity.isBeingDownloaded(reference.URL)) {
				if (reference.localCopyFileName(BookReference.Type.DOWNLOAD_DEMO) != null) {
					return READ_DEMO_ITEM_ID;
				} else {
					return DOWNLOAD_DEMO_ITEM_ID;
				}
			}
		}
		if (useBuyReferences(book)) {
			if (book.reference(BookReference.Type.BUY) != null) {
				return BUY_DIRECTLY_ITEM_ID;
			} else if (book.reference(BookReference.Type.BUY_IN_BROWSER) != null) {
				return BUY_IN_BROWSER_ITEM_ID;
			}
		}
		return -1;
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
		}
		return false;
	}

	private void doDownloadBook(NetworkTree tree, boolean demo) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		int resolvedType = demo ? BookReference.Type.DOWNLOAD_DEMO : BookReference.Type.DOWNLOAD_FULL;
		BookReference ref = book.reference(resolvedType);
		if (ref != null) {
			// TODO: add `demo` tag to the book???
			myActivity.startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.URL), myActivity, BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, resolvedType)
					.putExtra(BookDownloaderService.CLEAN_URL_KEY, ref.cleanURL())
					.putExtra(BookDownloaderService.TITLE_KEY, book.Title)
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
			myActivity.startActivity(
				new Intent(Intent.ACTION_VIEW,
					Uri.fromFile(new File(local)),
					myActivity,
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
		new AlertDialog.Builder(myActivity)
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
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		//FBReader &fbreader = FBReader::Instance();
		/*NetworkAuthenticationManager mgr = book.Link.authenticationManager();
		if (mgr == null) {
			return;
		}
		if (!NetworkOperationRunnable::tryConnect()) {
			return;
		}
		if (mgr.isAuthorised().Status != ZLBoolean3.B3_TRUE) {
			return;
			if (!AuthenticationDialog::run(mgr)) {
				return;
			}
			fbreader.invalidateAccountDependents();
			fbreader.refreshWindow();
			if (!mgr.needPurchase(myBook)) {
				return;
			}
		}
		ZLResourceKey boxKey("purchaseConfirmBox");
		const std::string message = ZLStringUtil::printf(ZLDialogManager::dialogMessage(boxKey), myBook.Title);
		const int code = ZLDialogManager::Instance().questionBox(boxKey, message, ZLResourceKey("buy"), ZLResourceKey("buyAndDownload"), ZLDialogManager::CANCEL_BUTTON);
		if (code == 2) {
			return;
		}
		bool downloadBook = code == 1;
		if (mgr.needPurchase(myBook)) {
			PurchaseBookRunnable purchaser(mgr, myBook);
			purchaser.executeWithUI();
			if (purchaser.hasErrors()) {
				purchaser.showErrorMessage();
				downloadBook = false;
			}
		}
		if (downloadBook) {
			NetworkBookDownloadAction(myBook, false).run();
		}
		if (mgr.isAuthorised().Status == B3_FALSE) {
			fbreader.invalidateAccountDependents();
		}
		fbreader.refreshWindow();*/
	}

	private void doBuyInBrowser(NetworkTree tree) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
		if (reference != null) {
			myActivity.openInBrowser(reference.URL);
		}
	}

}
