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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.urlInfo.BookBuyUrlInfo;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.util.SimpleDialogActivity;

public class BuyBooksActivity extends SimpleDialogActivity implements NetworkLibrary.ChangeListener {
	private final BookCollectionShadow myBookCollection = new BookCollectionShadow();

	public static void run(Activity activity, NetworkBookTree tree) {
		run(activity, Collections.singletonList(tree));
	}

	public static void run(Activity activity, List<NetworkBookTree> trees) {
		if (trees.isEmpty()) {
			return;
		}

		final Intent intent = new Intent(activity, BuyBooksActivity.class);
		final ArrayList<NetworkTree.Key> keys =
			new ArrayList<NetworkTree.Key>(trees.size());
		for (NetworkBookTree t : trees) {
			keys.add(t.getUniqueKey());
		}
		intent.putExtra(NetworkLibraryActivity.TREE_KEY_KEY, keys);
		activity.startActivity(intent);
	}

	private NetworkLibrary myLibrary;
	// we assume all the books are from the same catalog
	private INetworkLink myLink;
	private List<NetworkBookItem> myBooks;
	private Money myCost;
	private Money myAccount;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		myBookCollection.bindToService(this, null);
		myLibrary = Util.networkLibrary(this);

		final List<NetworkTree.Key> keys =
			(List<NetworkTree.Key>)getIntent().getSerializableExtra(
				NetworkLibraryActivity.TREE_KEY_KEY
			);
		if (keys == null || keys.isEmpty()) {
			finish();
			return;
		}
		myBooks = new ArrayList<NetworkBookItem>(keys.size());
		for (NetworkTree.Key k : keys) {
			final NetworkTree tree = myLibrary.getTreeByKey(k);
			if (tree instanceof NetworkBookTree) {
				myBooks.add(((NetworkBookTree)tree).Book);
			} else {
				finish();
				return;
			}
		}

		myLink = myBooks.get(0).Link;
		final NetworkAuthenticationManager mgr = myLink.authenticationManager();
		if (mgr == null) {
			finish();
			return;
		}

		try {
			if (!mgr.isAuthorised(true)) {
				buttonsView().setVisibility(View.GONE);
				AuthorisationMenuActivity.runMenu(this, myLink, 1);
			}
		} catch (ZLNetworkException e) {
		}

		myCost = calculateCost();
		if (myCost == null) {
			// TODO: error message
			finish();
			return;
		}

		myAccount = mgr.currentAccount();

		setupUI(AuthorisationState.Authorised);

		myLibrary.addChangeListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		buttonsView().setVisibility(View.VISIBLE);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private static enum AuthorisationState {
		Authorised,
		NotAuthorised
	};

	private void setupUI(final AuthorisationState state) {
		runOnUiThread(new Runnable() {
			public void run() {
				setupUIInternal(state);
			}
		});
	}

	private void setupUIInternal(AuthorisationState state) {
		final ZLResource dialogResource = ZLResource.resource("dialog");

		final ZLResource resource = ZLResource.resource("buyBook");
		if (myBooks.size() > 1) {
			setTitle(resource.getResource("titleSeveralBooks").getValue());
		} else {
			setTitle(resource.getResource("title").getValue());
		}

		switch (state) {
			case NotAuthorised:
				textView().setText(resource.getResource("notAuthorised").getValue());
				okButton().setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						AuthorisationMenuActivity.runMenu(BuyBooksActivity.this, myLink);
					}
				});
				cancelButton().setOnClickListener(finishListener());
				setButtonTexts("authorise", "cancel");
				break;
			case Authorised:
				if (myAccount == null) {
					textView().setText(resource.getResource("noAccountInformation").getValue());
					okButton().setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							refreshAccountInformation();
						}
					});
					cancelButton().setOnClickListener(finishListener());
					setButtonTexts("refresh", "cancel");
				} else if (myCost.compareTo(myAccount) > 0) {
					if (Money.ZERO.equals(myAccount)) {
						textView().setText(
							resource.getResource("zeroFunds").getValue()
								.replace("%0", myCost.toString())
						);
					} else {
						textView().setText(
							resource.getResource("unsufficientFunds").getValue()
								.replace("%0", myCost.toString())
								.replace("%1", myAccount.toString())
						);
					}
					okButton().setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							TopupMenuActivity.runMenu(BuyBooksActivity.this, myLink, myCost.subtract(myAccount));
						}
					});
					cancelButton().setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							refreshAccountInformation();
						}
					});
					setButtonTexts("pay", "refresh");
				} else {
					okButton().setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							UIUtil.wait("purchaseBook", buyRunnable(), BuyBooksActivity.this);
						}
					});
					cancelButton().setOnClickListener(finishListener());
					if (myBooks.size() > 1) {
						textView().setText(
							resource.getResource("confirmSeveralBooks").getValue()
								.replace("%s", String.valueOf(myBooks.size()))
						);
						setButtonTexts("buy", "cancel");
					} else if (myBooks.get(0).getStatus(myBookCollection) == NetworkBookItem.Status.CanBePurchased) {
						textView().setText(
							resource.getResource("confirm").getValue().replace("%s", myBooks.get(0).Title)
						);
						setButtonTexts("buy", "cancel");
					} else {
						textView().setText(resource.getResource("alreadyBought").getValue());
						setButtonTexts(null, "ok");
					}
				}
				break;
		}
	}

	@Override
	protected void onDestroy() {
		myLibrary.removeChangeListener(this);
		myBookCollection.unbind();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateAuthorisationState();
	}

	private Money calculateCost() {
		Money cost = Money.ZERO;
		for (NetworkBookItem b : myBooks) {
			if (b.getStatus(myBookCollection) != NetworkBookItem.Status.CanBePurchased) {
				continue;
			}
			final BookBuyUrlInfo info = b.buyInfo();
			if (info == null || info.Price == null) {
				return null;
			}
			cost = cost.add(info.Price);
		}
		return cost;
	}

	private void refreshAccountInformation() {
		runOnUiThread(new Runnable() {
			public void run() {
				refreshAccountInformationInternal();
			}
		});
	}

	private void refreshAccountInformationInternal() {
		UIUtil.wait(
			"updatingAccountInformation",
			new Runnable() {
				public void run() {
					final NetworkAuthenticationManager mgr = myLink.authenticationManager();
					try {
						boolean updated = false;

						mgr.refreshAccountInformation();
						final Money account = mgr.currentAccount();
						if (account != null && !account.equals(myAccount)) {
							myAccount = account;
							updated = true;
						}

						final Money cost = calculateCost();
						if (cost != null && !cost.equals(myCost)) {
							myCost = cost;
							updated = true;
						}

						if (updated) {
							runOnUiThread(new Runnable() {
								public void run() {
									setupUI(AuthorisationState.Authorised);
								}
							});
						}

						myLibrary.invalidateVisibility();
						myLibrary.synchronize();
					} catch (ZLNetworkException e) {
						// ignore
					}
				}
			},
			this
		);
	}

	private Runnable buyRunnable() {
		return new Runnable() {
			public void run() {
				try {
					final NetworkAuthenticationManager mgr = myLink.authenticationManager();
					for (final NetworkBookItem b : myBooks) {
						if (b.getStatus(myBookCollection) != NetworkBookItem.Status.CanBePurchased) {
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
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource buttonResource = dialogResource.getResource("button");
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
				} finally {
					myLibrary.invalidateVisibility();
					myLibrary.synchronize();
				}
			}
		};
	}

	// method from NetworkLibrary.ChangeListener
	public void onLibraryChanged(final NetworkLibrary.ChangeListener.Code code, final Object[] params) {
		switch (code) {
			case SignedIn:
				updateAuthorisationState();
				break;
		}
	}

	private void updateAuthorisationState() {
		new Thread(new Runnable() {
			public void run() {
				final NetworkAuthenticationManager mgr = myLink.authenticationManager();
				try {
					if (mgr.isAuthorised(true)) {
						setupUI(AuthorisationState.Authorised);
						refreshAccountInformation();
					} else {
						setupUI(AuthorisationState.NotAuthorised);
					}
				} catch (ZLNetworkException e) {
					e.printStackTrace();
					setupUI(AuthorisationState.NotAuthorised);
				}
			}
		}).start();
	}
}
