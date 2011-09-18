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

package org.geometerplus.android.fbreader.network.action;

import java.util.*;
import java.io.File;

import android.app.AlertDialog;
import android.app.Activity;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.fbreader.FBReader;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.network.*;

public abstract class NetworkBookActions {
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

	public static class NBAction extends BookAction {
		private final int myId;
		private final String myArg;

		public NBAction(Activity activity, int id, String key) {
			this(activity, id, key, null);
		}

		public NBAction(Activity activity, int id, String key, String arg) {
			super(activity, id, key);
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
		protected void run(NetworkTree tree) {
			run(getBook(tree));
		}

		public void run(NetworkBookItem book) {
			runActionStatic(myActivity, book, myId);
		}
	}

	public static int getBookStatus(NetworkBookItem book, BookDownloaderServiceConnection connection) {
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

	public static List<NBAction> getContextMenuActions(Activity activity, NetworkBookItem book, BookDownloaderServiceConnection connection) {
		List<NBAction> actions = new LinkedList<NBAction>();
		if (useFullReferences(book)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.Book);
			if (reference != null
					&& connection != null && connection.isBeingDownloaded(reference.Url)) {
				actions.add(new NBAction(activity, ActionCode.TREE_NO_ACTION, "alreadyDownloading"));
			} else if (book.localCopyFileName() != null) {
				actions.add(new NBAction(activity, ActionCode.READ_BOOK, "read"));
				actions.add(new NBAction(activity, ActionCode.DELETE_BOOK, "delete"));
			} else if (reference != null) {
				actions.add(new NBAction(activity, ActionCode.DOWNLOAD_BOOK, "download"));
			}
		}
		if (useDemoReferences(book)) {
			final BookUrlInfo reference = book.reference(UrlInfo.Type.BookDemo);
			if (connection != null && connection.isBeingDownloaded(reference.Url)) {
				actions.add(new NBAction(activity, ActionCode.TREE_NO_ACTION, "alreadyDownloadingDemo"));
			} else if (reference.localCopyFileName(UrlInfo.Type.BookDemo) != null) {
				actions.add(new NBAction(activity, ActionCode.READ_DEMO, "readDemo"));
				actions.add(new NBAction(activity, ActionCode.DELETE_DEMO, "deleteDemo"));
			} else {
				actions.add(new NBAction(activity, ActionCode.DOWNLOAD_DEMO, "downloadDemo"));
			}
		}
		if (useBuyReferences(book)) {
			int id = ActionCode.TREE_NO_ACTION;
			final BookBuyUrlInfo reference = book.buyInfo();
			if (reference != null) {
				id = reference.InfoType == UrlInfo.Type.BookBuy
					? ActionCode.BUY_DIRECTLY : ActionCode.BUY_IN_BROWSER;
				actions.add(new NBAction(activity, id, "buy", reference.Price.toString()));
			}
			final Basket basket = book.Link.basket();
			if (basket != null) {
				if (basket.contains(book)) {
					actions.add(new NBAction(activity, ActionCode.REMOVE_BOOK_FROM_BASKET, "removeFromBasket"));
				} else {
					actions.add(new NBAction(activity, ActionCode.ADD_BOOK_TO_BASKET, "addToBasket"));
				}
			}
		}
		return actions;
	}

	private static boolean runActionStatic(Activity activity, NetworkBookItem book, int actionCode) {
		switch (actionCode) {
			case ActionCode.DOWNLOAD_BOOK:
				doDownloadBook(activity, book, false);
				return true;
			case ActionCode.DOWNLOAD_DEMO:
				doDownloadBook(activity, book, true);
				return true;
			case ActionCode.READ_BOOK:
				doReadBook(activity, book, false);
				return true;
			case ActionCode.READ_DEMO:
				doReadBook(activity, book, true);
				return true;
			case ActionCode.DELETE_BOOK:
				tryToDeleteBook(activity, book, false);
				return true;
			case ActionCode.DELETE_DEMO:
				tryToDeleteBook(activity, book, true);
				return true;
			case ActionCode.BUY_DIRECTLY:
				doBuyDirectly(activity, book);
				return true;
			case ActionCode.BUY_IN_BROWSER:
				doBuyInBrowser(activity, book);
				return true;
			case ActionCode.ADD_BOOK_TO_BASKET:
				book.Link.basket().add(book);
				return true;
			case ActionCode.REMOVE_BOOK_FROM_BASKET:
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
					NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
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

		final Runnable buyRunnable = new Runnable() {
			public void run() {
				try {
					mgr.purchaseBook(book);
					final NetworkLibrary library = NetworkLibrary.Instance();
					library.invalidateVisibility();
					library.synchronize();
				} catch (final ZLNetworkException e) {
					final String buttonKey;
					final DialogInterface.OnClickListener action;
					if (NetworkException.ERROR_PURCHASE_NOT_ENOUGH_MONEY.equals(e.getCode())) {
						buttonKey = "topup";
						action = new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								final BookBuyUrlInfo info = book.buyInfo();
								Money price = info != null ? info.Price : null;
								System.err.println("price = " + price);
								if (price != null) {
									final Money account = mgr.currentAccount();
									System.err.println("account = " + account);
									if (account != null) {
										price = price.subtract(account);
									}
								}
								System.err.println("price = " + price);
								TopupMenuActivity.runMenu(activity, book.Link, price);
							}
						};
					} else {
						buttonKey = "ok";
						action = null;
					}
					final ZLResource boxResource = dialogResource.getResource("networkError");
					activity.runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(activity)
								.setTitle(boxResource.getResource("title").getValue())
								.setMessage(e.getMessage())
								.setIcon(0)
								.setPositiveButton(buttonResource.getResource(buttonKey).getValue(), action)
								.create().show();
						}
					});
					final NetworkLibrary library = NetworkLibrary.Instance();
					library.invalidateVisibility();
					library.synchronize();
				}
			}
		};

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_NEGATIVE) {
					return;
				}
				if (!mgr.needPurchase(book)) {
					return;
				}
				UIUtil.wait("purchaseBook", buyRunnable, activity);
				final boolean downloadBook = which == DialogInterface.BUTTON_NEUTRAL;
				if (downloadBook) {
					activity.runOnUiThread(new Runnable() {
						public void run() {
							doDownloadBook(activity, book, false);
						}
					});
				}
			} 
		};

		final Runnable buyDialogRunnable = new Runnable() {
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
				buyDialogRunnable.run();
			} else {
				final String signInKey = "signIn";
				final String registerKey = "signUp";
				final String quickBuyKey = "quickBuy";

				final ArrayList<String> items = new ArrayList<String>();
				items.add(signInKey);
				if (Util.isRegistrationSupported(activity, book.Link)) {
					items.add(registerKey);
				}
				if (Util.isAutoSignInSupported(activity, book.Link)) {
					items.add(quickBuyKey);
				}

				if (items.size() > 1) {
					final ZLResource box = dialogResource.getResource("purchaseActions");

					CharSequence[] names = new CharSequence[items.size()];
					for (int i = 0; i < names.length; ++i) {
						names[i] = box.getResource(items.get(i)).getValue();
					}

					new AlertDialog.Builder(activity)
						.setIcon(0)
						.setTitle(box.getResource("title").getValue())
						.setItems(names, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								final String item = items.get(which);
								if (signInKey.equals(item)) {
									Util.runAuthenticationDialog(activity, book.Link, new Runnable() {
										public void run() {
											activity.runOnUiThread(buyDialogRunnable);
										}
									});
								} else if (registerKey.equals(item)) {
									Util.runRegistrationDialog(activity, book.Link);
									// TODO: buy on success
								} else if (quickBuyKey.equals(item)) {
									Util.runAutoSignInDialog(activity, book.Link);
									// TODO: buy on success
								}
							}
						})
						.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
						.create().show();
				} else {
					Util.runAuthenticationDialog(activity, book.Link, new Runnable() {
						public void run() {
							activity.runOnUiThread(buyDialogRunnable);
						}
					});
				}
			}
		} catch (ZLNetworkException e) {
		}
	}

	private static void doBuyInBrowser(Activity activity, final NetworkBookItem book) {
		BookUrlInfo reference = book.reference(UrlInfo.Type.BookBuyInBrowser);
		if (reference != null) {
			Util.openInBrowser(activity, reference.Url);
		}
	}

}
