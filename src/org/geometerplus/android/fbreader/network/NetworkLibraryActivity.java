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

import java.util.*;
import java.io.File;

import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.fbreader.ZLTreeAdapter;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.network.authentication.*;


public class NetworkLibraryActivity extends ListActivity implements MenuItem.OnMenuItemClickListener {
	static NetworkLibraryActivity Instance;

	private final ZLResource myResource = ZLResource.resource("networkView");

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		new LibraryAdapter(getListView(), NetworkLibrary.Instance().getTree());
	}

	@Override
	public void onResume() {
		super.onResume();
		Instance = this;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		Instance = null;
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private final class LibraryAdapter extends ZLTreeAdapter {

		LibraryAdapter(ListView view, NetworkTree tree) {
			super(view, tree);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final NetworkTree tree = (NetworkTree) getItem(position);

			/*if (tree instanceof NetworkCatalogTree || tree instanceof NetworkBookTree) {
				menu.add(0, DBG_PRINT_ENTRY_ITEM_ID, 0, "dbg - Dump Entry");
			}*/

			if (tree instanceof NetworkCatalogRootTree) {
				menu.setHeaderTitle(tree.getName());
				NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
				NetworkCatalogItem item = catalogTree.Item;
				NetworkAuthenticationManager mgr = item.Link.authenticationManager();
				if (item.URLByType.get(NetworkLibraryItem.URL_CATALOG) != null) {
					String key = (tree.hasChildren() && isOpen(tree)) ? "closeCatalog" : "openCatalog";
					menu.add(0, EXPAND_OR_COLLAPSE_TREE_ITEM_ID, 0, myResource.getResource(key).getValue());
				}
				if (tree.hasChildren() && isOpen(tree)) {
					menu.add(0, RELOAD_ITEM_ID, 0, myResource.getResource("reload").getValue());
				}
				/*if (!mgr.isNull()) {
					registerAction(new LoginAction(*mgr));
					registerAction(new LogoutAction(*mgr));
					if (!mgr->refillAccountLink().empty()) {
						registerAction(new RefillAccountAction(*mgr));
					}
					if (mgr->registrationSupported()) {
						registerAction(new RegisterUserAction(*mgr), true);
					}
					if (mgr->passwordRecoverySupported()) {
						registerAction(new PasswordRecoveryAction(*mgr), true);
					}
				}*/
				//menu.add(0, DONT_SHOW_ITEM_ID, 0, myResource.getResource("dontShow").getValue()); // TODO: is it needed??? and how to turn it on???
			} else if (tree instanceof NetworkCatalogTree) {
				menu.setHeaderTitle(tree.getName());
				NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
				NetworkCatalogItem item = catalogTree.Item;
				if (item.URLByType.get(NetworkLibraryItem.URL_CATALOG) != null) {
					String key = (tree.hasChildren() && isOpen(tree)) ? "collapseTree" : "expandTree";
					menu.add(0, EXPAND_OR_COLLAPSE_TREE_ITEM_ID, 0, myResource.getResource(key).getValue());
				}
				String htmlUrl = item.URLByType.get(NetworkLibraryItem.URL_HTML_PAGE);
				if (htmlUrl != null) {
					menu.add(0, OPEN_IN_BROWSER_ITEM_ID, 0, myResource.getResource("openInBrowser").getValue());
				}
				if (tree.hasChildren() && isOpen(tree)) {
					menu.add(0, RELOAD_ITEM_ID, 0, myResource.getResource("reload").getValue());
				}
			} else if (tree instanceof NetworkBookTree) {
				NetworkBookTree bookTree = (NetworkBookTree) tree;
				NetworkBookItem book = bookTree.Book;
				menu.setHeaderTitle(tree.getName());
				if (book.reference(BookReference.Type.DOWNLOAD_FULL) != null ||
						book.reference(BookReference.Type.DOWNLOAD_FULL_CONDITIONAL) != null) {
					if (book.localCopyFileName() != null) {
						menu.add(0, READ_BOOK_ITEM_ID, 0, myResource.getResource("read").getValue());
						menu.add(0, DELETE_BOOK_ITEM_ID, 0, myResource.getResource("delete").getValue());
					} else if (book.reference(BookReference.Type.DOWNLOAD_FULL) != null) {
						menu.add(0, DOWNLOAD_BOOK_ITEM_ID, 0, myResource.getResource("download").getValue());
					}
				}
				if (book.reference(BookReference.Type.DOWNLOAD_DEMO) != null &&
						book.localCopyFileName() == null &&
						book.reference(BookReference.Type.DOWNLOAD_FULL) == null) {
					BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
					if (reference.localCopyFileName() != null) {
						menu.add(0, READ_DEMO_ITEM_ID, 0, myResource.getResource("readDemo").getValue());
						menu.add(0, DELETE_DEMO_ITEM_ID, 0, myResource.getResource("deleteDemo").getValue());
					} else {
						menu.add(0, DOWNLOAD_DEMO_ITEM_ID, 0, myResource.getResource("downloadDemo").getValue());
					}
				}
				if (book.localCopyFileName() == null &&
						book.reference(BookReference.Type.DOWNLOAD_FULL) == null) {
					if (book.reference(BookReference.Type.BUY) != null) {
						BookReference reference = book.reference(BookReference.Type.BUY);
						String title = myResource.getResource("buy").getValue()
							.replace("%s", ((BuyBookReference) reference).Price);
						menu.add(0, BUY_DIRECTLY_ITEM_ID, 0, title);
					} else if (book.reference(BookReference.Type.BUY_IN_BROWSER) != null) {
						BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
						String title = myResource.getResource("buy").getValue()
							.replace("%s", ((BuyBookReference) reference).Price);
						menu.add(0, BUY_IN_BROWSER_ITEM_ID, 0, title);
					}
				}
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);

			final NetworkTree tree = (NetworkTree)getItem(position);

			final ImageView iconView = (ImageView)view.findViewById(R.id.network_tree_item_icon);

			setIcon(iconView, tree);

			((TextView)view.findViewById(R.id.network_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.network_tree_item_childrenlist)).setText(tree.getSecondString());
			return view;
		}

		public void expandCatalog(NetworkCatalogTree tree) {
			/*if (!NetworkOperationRunnable::tryConnect()) {
				return;
			}*/
			NetworkCatalogItem item = tree.Item;
			NetworkLink link = item.Link;
			if (link.authenticationManager() != null) {
				NetworkAuthenticationManager mgr = link.authenticationManager();
				/*IsAuthorisedRunnable checker(mgr);
				checker.executeWithUI();
				if (checker.hasErrors()) {
					checker.showErrorMessage();
					return;
				}
				if (checker.result() == B3_TRUE && mgr.needsInitialization()) {
					InitializeAuthenticationManagerRunnable initializer(mgr);
					initializer.executeWithUI();
					if (initializer.hasErrors()) {
						LogOutRunnable logout(mgr);
						logout.executeWithUI();
					}
				}*/
			}
			if (!tree.hasChildren()) {
				updateCatalogChildren(tree);
				resetTree();
			}
			super.runTreeItem(tree);
		}

		public void reloadCatalog(NetworkCatalogTree tree) {
			updateCatalogChildren(tree);
			resetTree();
			//super.runTreeItem(tree);
		}

		public void diableCatalog(NetworkCatalogRootTree tree) {
			/*NetworkLink link = tree.Link;
			link.OnOption.setValue(false);
			NetworkLibrary library = NetworkLibrary.Instance();
			library.invalidate();
			library.synchronize();
			resetTree(); // FIXME: may be bug: [open catalog] -> [disable] -> [enable] -> [load againg] => catalog won't opens (it will be closed after previos opening)
			*/
		}

		@Override
		protected boolean runTreeItem(ZLTree tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			if (tree instanceof NetworkCatalogTree) {
				expandCatalog((NetworkCatalogTree) tree);
				return true;
			} else if (tree instanceof NetworkBookTree) {
				final ZLResource resource = myResource.getResource("confirmQuestions");
				final NetworkBookTree bookTree = (NetworkBookTree) tree;
				final NetworkBookItem book = bookTree.Book;
				int actionCode = -1;
				String confirm = null;
				if (book.reference(BookReference.Type.DOWNLOAD_FULL) != null ||
						book.reference(BookReference.Type.DOWNLOAD_FULL_CONDITIONAL) != null) {
					if (book.localCopyFileName() != null) {
						actionCode = READ_BOOK_ITEM_ID;
						confirm = resource.getResource("read").getValue();
					} else if (book.reference(BookReference.Type.DOWNLOAD_FULL) != null) {
						actionCode = DOWNLOAD_BOOK_ITEM_ID;
						confirm = resource.getResource("download").getValue();
					}
				} else if (book.reference(BookReference.Type.DOWNLOAD_DEMO) != null &&
						book.localCopyFileName() == null &&
						book.reference(BookReference.Type.DOWNLOAD_FULL) == null) {
					BookReference reference = book.reference(BookReference.Type.DOWNLOAD_DEMO);
					if (reference.localCopyFileName() != null) {
						actionCode = READ_DEMO_ITEM_ID;
						confirm = resource.getResource("readDemo").getValue();
					} else {
						actionCode = DOWNLOAD_DEMO_ITEM_ID;
						confirm = resource.getResource("downloadDemo").getValue();
					}
				} else if (book.localCopyFileName() == null &&
						book.reference(BookReference.Type.DOWNLOAD_FULL) == null) {
					if (book.reference(BookReference.Type.BUY) != null) {
						actionCode = BUY_DIRECTLY_ITEM_ID;
						BookReference reference = book.reference(BookReference.Type.BUY);
						confirm = resource.getResource("buy").getValue()
							.replace("%s", ((BuyBookReference) reference).Price);
					} else if (book.reference(BookReference.Type.BUY_IN_BROWSER) != null) {
						actionCode = BUY_IN_BROWSER_ITEM_ID;
						BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
						confirm = resource.getResource("buy").getValue()
							.replace("%s", ((BuyBookReference) reference).Price);
					}
				}
				if (confirm != null) {
					final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
					final int actionCodeValue = actionCode;
					new AlertDialog.Builder(NetworkLibraryActivity.this)
						.setTitle(book.Title)
						.setMessage(confirm)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								runContextMenuItem(bookTree, actionCodeValue);
							}
						})
						.setNegativeButton(buttonResource.getResource("no").getValue(), null)
						.create().show();
				}
			}
			return false;
		}
	}


	private void updateCatalogChildren(NetworkCatalogTree tree) {
		tree.clear();

		ArrayList<NetworkLibraryItem> children = tree.ChildrenItems;

		children.clear();
		LoadSubCatalogRunnable loader = new LoadSubCatalogRunnable(tree.Item, children);
		loader.executeWithUI();
		if (loader.hasErrors()) {
			loader.showErrorMessage(this);
		} else if (children.isEmpty()) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			final ZLResource buttonResource = dialogResource.getResource("button");
			final ZLResource boxResource = dialogResource.getResource("emptyCatalogBox");
			new AlertDialog.Builder(this)
				.setTitle(boxResource.getResource("title").getValue())
				.setMessage(boxResource.getResource("message").getValue())
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
				.create().show();
		}

		boolean hasSubcatalogs = false;
		for (NetworkLibraryItem child: children) {
			if (child instanceof NetworkCatalogItem) {
				hasSubcatalogs = true;
				break;
			}
		}

		if (hasSubcatalogs) {
			for (NetworkLibraryItem child: children) {
				NetworkTreeFactory.createNetworkTree(tree, child);
			}
		} else {
			NetworkTreeFactory.fillAuthorNode(tree, children);
		}
		NetworkLibrary.Instance().invalidateAccountDependents();
		NetworkLibrary.Instance().synchronize();
	}


	private static final int EXPAND_OR_COLLAPSE_TREE_ITEM_ID = 0;
	private static final int DOWNLOAD_BOOK_ITEM_ID = 1;
	private static final int READ_BOOK_ITEM_ID = 2;
	private static final int DELETE_BOOK_ITEM_ID = 3;
	private static final int READ_DEMO_ITEM_ID = 4;
	private static final int DOWNLOAD_DEMO_ITEM_ID = 5;
	private static final int BUY_DIRECTLY_ITEM_ID = 6;
	private static final int BUY_IN_BROWSER_ITEM_ID = 7;
	private static final int OPEN_IN_BROWSER_ITEM_ID = 8;
	private static final int RELOAD_ITEM_ID = 9;
	private static final int DONT_SHOW_ITEM_ID = 10;
	private static final int DELETE_DEMO_ITEM_ID = 11;

	//private static final int DBG_PRINT_ENTRY_ITEM_ID = 32000;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final LibraryAdapter adapter = (LibraryAdapter) getListView().getAdapter();
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree) adapter.getItem(position);
		if (runContextMenuItem(tree, item.getItemId())) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private boolean runContextMenuItem(NetworkTree tree, int actionCode) {
		final LibraryAdapter adapter = (LibraryAdapter) getListView().getAdapter();
		switch (actionCode) {
			/*case DBG_PRINT_ENTRY_ITEM_ID: {
					String msg = null;
					if (tree instanceof NetworkCatalogTree) {
						msg = ((NetworkCatalogTree) tree).Item.dbgEntry.toString();
					} else if (tree instanceof NetworkBookTree) {
						msg = ((NetworkBookTree) tree).Book.dbgEntry.toString();
					}
					new AlertDialog.Builder(this).setTitle("dbg entry").setMessage(msg).setIcon(0).setPositiveButton("ok", null).create().show();
				}
				return true;*/
			case EXPAND_OR_COLLAPSE_TREE_ITEM_ID:
				adapter.expandCatalog((NetworkCatalogTree)tree);
				return true;
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
			case OPEN_IN_BROWSER_ITEM_ID:
				openInBrowser(((NetworkCatalogTree)tree).Item.URLByType.get(NetworkLibraryItem.URL_HTML_PAGE));
				return true;
			case RELOAD_ITEM_ID:
				adapter.reloadCatalog((NetworkCatalogTree)tree);
				return true;
			case DONT_SHOW_ITEM_ID:
				return true;
		}
		return false;
	}

	private void doBuyInBrowser(NetworkTree tree) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		BookReference reference = book.reference(BookReference.Type.BUY_IN_BROWSER);
		if (reference != null) {
			openInBrowser(reference.URL);
		}
	}

	private void openInBrowser(String url) {
		if (url != null) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
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


	private void doDownloadBook(NetworkTree tree, boolean demo) {
		NetworkBookTree bookTree = (NetworkBookTree) tree;
		NetworkBookItem book = bookTree.Book;
		BookReference ref = book.reference(
			demo ? BookReference.Type.DOWNLOAD_DEMO : BookReference.Type.DOWNLOAD_FULL
		);
		if (ref != null) {
			// TODO: add `demo` tag to the book???
			startService(
				new Intent(Intent.ACTION_VIEW, Uri.parse(ref.URL), this, BookDownloaderService.class)
					.putExtra(BookDownloaderService.BOOK_FORMAT_KEY, ref.BookFormat)
					.putExtra(BookDownloaderService.REFERENCE_TYPE_KEY, ref.ReferenceType)
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
				local = reference.localCopyFileName();
			}
		}
		if (local != null) {
			startActivity(
				new Intent(Intent.ACTION_VIEW, Uri.fromFile(new File(local)), this, org.geometerplus.android.fbreader.FBReader.class)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP/* | Intent.FLAG_ACTIVITY_NEW_TASK*/)
			);
		}
	}

	private void tryToDeleteBook(NetworkTree tree, final boolean demo) {
		final NetworkBookTree bookTree = (NetworkBookTree) tree;
		final NetworkBookItem book = bookTree.Book;
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(this)
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
							final String fileName = reference.localCopyFileName();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemClick(MenuItem item) {
		return true;
	}
}
