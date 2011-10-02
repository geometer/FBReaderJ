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

import java.util.*;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.urlInfo.BookBuyUrlInfo;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.network.*;

public class BuyBooksActivity extends Activity {
	public static void run(Activity activity, NetworkBookTree tree) {
		run(activity, Collections.singletonList(tree));
	}

	public static void run(Activity activity, List<NetworkBookTree> trees) {
		final Intent intent = new Intent(activity, BuyBooksActivity.class);
		final ArrayList<NetworkTree.Key> keys =
			new ArrayList<NetworkTree.Key>(trees.size());
		for (NetworkBookTree t : trees) {
			keys.add(t.getUniqueKey());
		}
		intent.putExtra(NetworkLibraryActivity.TREE_KEY_KEY, keys);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.buy_book);

		final NetworkLibrary library = NetworkLibrary.Instance();

		final List<NetworkTree.Key> keys =
			(List<NetworkTree.Key>)getIntent().getSerializableExtra(
				NetworkLibraryActivity.TREE_KEY_KEY
			);
		if (keys == null || keys.isEmpty()) {
			finish();
			return;
		}
		final List<NetworkBookItem> books = new ArrayList<NetworkBookItem>(keys.size()); 
		for (NetworkTree.Key k : keys) {
			final NetworkTree tree = library.getTreeByKey(k);
			if (tree instanceof NetworkBookTree) {
				books.add(((NetworkBookTree)tree).Book);
			} else {
				finish();
				return;
			}
		}

		// we assume all the books are from the same catalog
		final INetworkLink link = books.get(0).Link;
		final NetworkAuthenticationManager mgr = link.authenticationManager();
		if (mgr == null) {
			finish();
			return;
		}

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");

		final TextView textArea = (TextView)findViewById(R.id.buy_book_text);
		final Button okButton =
			(Button)findViewById(R.id.buy_book_buttons).findViewById(R.id.ok_button);
		final Button cancelButton =
			(Button)findViewById(R.id.buy_book_buttons).findViewById(R.id.cancel_button);

		final Runnable buyRunnable = new Runnable() {
			public void run() {
				Money cost = Money.ZERO;
				System.err.println("cost = " + cost);
				try {
					final Money account = mgr.currentAccount();
					System.err.println("account = " + account);
					if (account != null) {
						for (NetworkBookItem b : books) {
							final BookBuyUrlInfo info = b.buyInfo();
							if (b.getStatus() != NetworkBookItem.Status.CanBePurchased) {
								continue;
							}
							if (info == null || info.Price == null) {
								cost = null;
								break;
							}
							cost = cost.add(info.Price);
							System.err.println("cost = " + cost);
						}
						cost = cost.subtract(account);
						System.err.println("cost = " + cost);
					} else {
						cost = null;
					}
					System.err.println("cost = " + cost);
					if (cost != null && cost.compareTo(Money.ZERO) > 0 && books.size() > 1) {
						// we only throw this exception if there are more than 1 book in list
						// for 1 book we prefer to send request to server and got an error
						throw new ZLNetworkException(NetworkException.ERROR_PURCHASE_NOT_ENOUGH_MONEY);
					}
					
					for (final NetworkBookItem b : books) {
						if (b.getStatus() != NetworkBookItem.Status.CanBePurchased) {
							continue;
						}
						mgr.purchaseBook(b);
						runOnUiThread(new Runnable() {
							public void run() {
								Util.doDownloadBook(BuyBooksActivity.this, b, false);
							}
						});
					}
					finish();
				} catch (final ZLNetworkException e) {
					if (NetworkException.ERROR_PURCHASE_NOT_ENOUGH_MONEY.equals(e.getCode())) {
						TopupMenuActivity.runMenu(BuyBooksActivity.this, link, cost);
						finish();
					} else {
						final ZLResource boxResource = dialogResource.getResource("networkError");
						runOnUiThread(new Runnable() {
							public void run() {
								new AlertDialog.Builder(BuyBooksActivity.this)
									.setTitle(boxResource.getResource("title").getValue())
									.setMessage(e.getMessage())
									.setIcon(0)
									.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
									.create().show();
							}
						});
					}
				} finally {
					library.invalidateVisibility();
					library.synchronize();
				}
			}
		};

		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				UIUtil.wait("purchaseBook", buyRunnable, BuyBooksActivity.this);
			} 
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			} 
		});

		if (books.size() > 1 || books.get(0).getStatus() == NetworkBookItem.Status.CanBePurchased) {
			final ZLResource boxResource = dialogResource.getResource("purchaseConfirmBox");
			if (books.size() == 1) {
				setTitle(boxResource.getResource("title").getValue());
				textArea.setText(
					boxResource.getResource("message").getValue().replace("%s", books.get(0).Title)
				);
			} else {
				setTitle(boxResource.getResource("titleSeveralBooks").getValue());
				textArea.setText(
					boxResource.getResource("messageSeveralBooks").getValue()
						.replace("%s", String.valueOf(books.size()))
				);
			}
			okButton.setText(buttonResource.getResource("buy").getValue());
			cancelButton.setText(buttonResource.getResource("cancel").getValue());
		} else {
			final ZLResource boxResource = dialogResource.getResource("alreadyPurchasedBox");
			setTitle(boxResource.getResource("title").getValue());
			textArea.setText(boxResource.getResource("message").getValue());
			cancelButton.setText(buttonResource.getResource("ok").getValue());
			okButton.setVisibility(View.GONE);
		}
	}
}
