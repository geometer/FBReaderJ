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

public class BuyBookActivity extends Activity {
	public static void run(Activity activity, NetworkBookTree tree) {
		final Intent intent = new Intent(activity, BuyBookActivity.class);
		intent.putExtra(NetworkLibraryActivity.TREE_KEY_KEY, tree.getUniqueKey());
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.buy_book);

		final NetworkLibrary library = NetworkLibrary.Instance();
		final NetworkTree tree = library.getTreeByKey(
			(NetworkTree.Key)getIntent().getSerializableExtra(
				NetworkLibraryActivity.TREE_KEY_KEY
			)
		);

		final NetworkBookItem book;
		if (tree instanceof NetworkBookTree) {
			book = ((NetworkBookTree)tree).Book;
		} else {
			finish();
			return;
		}

		final NetworkAuthenticationManager mgr = book.Link.authenticationManager();
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
				try {
					mgr.purchaseBook(book);
					runOnUiThread(new Runnable() {
						public void run() {
							Util.doDownloadBook(BuyBookActivity.this, book, false);
						}
					});
					finish();
				} catch (final ZLNetworkException e) {
					if (NetworkException.ERROR_PURCHASE_NOT_ENOUGH_MONEY.equals(e.getCode())) {
						final BookBuyUrlInfo info = book.buyInfo();
						Money price = info != null ? info.Price : null;
						if (price != null) {
							final Money account = mgr.currentAccount();
							if (account != null) {
								price = price.subtract(account);
							}
						}
						TopupMenuActivity.runMenu(BuyBookActivity.this, book.Link, price);
						finish();
					} else {
						final ZLResource boxResource = dialogResource.getResource("networkError");
						runOnUiThread(new Runnable() {
							public void run() {
								new AlertDialog.Builder(BuyBookActivity.this)
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
				UIUtil.wait("purchaseBook", buyRunnable, BuyBookActivity.this);
			} 
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			} 
		});

		if (book.getStatus() == NetworkBookItem.Status.CanBePurchased) {
			final ZLResource boxResource = dialogResource.getResource("purchaseConfirmBox");
			setTitle(boxResource.getResource("title").getValue());
			textArea.setText(
				boxResource.getResource("message").getValue().replace("%s", book.Title)
			);
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
