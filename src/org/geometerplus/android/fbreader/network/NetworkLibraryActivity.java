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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSCustomNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.*;

public class NetworkLibraryActivity extends NetworkBaseActivity {
	private volatile Intent myIntent;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		myIntent = getIntent();

		if (!NetworkView.Instance().isInitialized()) {
			if (NetworkInitializer.Instance == null) {
				new NetworkInitializer(this);
				NetworkInitializer.Instance.start();
			} else {
				NetworkInitializer.Instance.setActivity(this);
			}
		} else {
			onModelChanged();
			if (myIntent != null) {
				processIntent(myIntent);
				myIntent = null;
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		processIntent(intent);
	}

	void processSavedIntent() {
		if (myIntent != null) {
			processIntent(myIntent);
			myIntent = null;
		}
	}

	private void processIntent(Intent intent) {
		if (AddCustomCatalogActivity.ADD_CATALOG.equals(intent.getAction())) {
			final ICustomNetworkLink link = AddCustomCatalogActivity.getLinkFromIntent(intent);
			if (link != null) {
				runOnUiThread(new Runnable() {
					public void run() {
						final NetworkLibrary library = NetworkLibrary.Instance();
						library.addCustomLink(link);
						library.synchronize();
						NetworkView.Instance().fireModelChangedAsync();
						getListView().invalidateViews();
					}
				});
			}
		}
	}

	@Override
	public void onDestroy() {
		if (!NetworkView.Instance().isInitialized() && NetworkInitializer.Instance != null) {
			NetworkInitializer.Instance.setActivity(null);
		}
		super.onDestroy();
	}

	protected MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
		final String label = NetworkLibrary.resource().getResource("menu").getResource(resourceKey).getValue();
		return menu.add(0, index, Menu.NONE, label).setIcon(iconId);
	}


	private static final int MENU_SEARCH = 1;
	private static final int MENU_REFRESH = 2;
	private static final int MENU_ADD_CATALOG = 3;
	private static final int MENU_LANGUAGE_FILTER = 4;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, MENU_SEARCH, "networkSearch", R.drawable.ic_menu_search);
		addMenuItem(menu, MENU_ADD_CATALOG, "addCustomCatalog", R.drawable.ic_menu_add);
		addMenuItem(menu, MENU_REFRESH, "refreshCatalogsList", R.drawable.ic_menu_refresh);
		addMenuItem(menu, MENU_LANGUAGE_FILTER, "languages", R.drawable.ic_menu_languages);
		return true;
	}

	private static boolean searchIsInProgress() {
		return ItemsLoadingService.getRunnable(
			NetworkLibrary.Instance().getSearchItemTree()
		) != null;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(MENU_SEARCH).setEnabled(!searchIsInProgress());
		return true;
	}

	private void runLanguageFilterDialog() {
		final NetworkLibrary library = NetworkLibrary.Instance();

		final List<String> allLanguageCodes = library.languageCodes();
		Collections.sort(allLanguageCodes, new ZLLanguageUtil.CodeComparator());
		final Collection<String> activeLanguageCodes = library.activeLanguageCodes();
		final CharSequence[] languageNames = new CharSequence[allLanguageCodes.size()];
		final boolean[] checked = new boolean[allLanguageCodes.size()];

		for (int i = 0; i < allLanguageCodes.size(); ++i) {
			final String code = allLanguageCodes.get(i);
			languageNames[i] = ZLLanguageUtil.languageName(code);
			checked[i] = activeLanguageCodes.contains(code);
		}

		final DialogInterface.OnMultiChoiceClickListener listener =
			new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked[which] = isChecked;
				}
			};
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final AlertDialog dialog = new AlertDialog.Builder(this)
			.setMultiChoiceItems(languageNames, checked, listener)
			.setTitle(dialogResource.getResource("languageFilterDialog").getResource("title").getValue())
			.setPositiveButton(dialogResource.getResource("button").getResource("ok").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					final TreeSet<String> newActiveCodes = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
					for (int i = 0; i < checked.length; ++i) {
						if (checked[i]) {
							newActiveCodes.add(allLanguageCodes.get(i));
						}
					}
					library.setActiveLanguageCodes(newActiveCodes);
					library.synchronize();
					NetworkView.Instance().fireModelChanged();
				}
			})
			.create();
		dialog.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SEARCH:
				return onSearchRequested();
			case MENU_ADD_CATALOG:
				AddCustomCatalogItemActions.addCustomCatalog(this);
				return true;
			case MENU_REFRESH:
				refreshCatalogsList();
				return true;
			case MENU_LANGUAGE_FILTER:
				runLanguageFilterDialog();
				return true;
			default:
				return true;
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (searchIsInProgress()) {
			return false;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	@Override
	public void onModelChanged() {
		getListView().invalidateViews();
		((NetworkLibraryAdapter)getListAdapter()).replaceAll(getCurrentTree().subTrees());
	}

	private void refreshCatalogsList() {
		final NetworkView view = NetworkView.Instance();

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj == null) {
					view.finishBackgroundUpdate();
				} else {
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource boxResource = dialogResource.getResource("networkError");
					final ZLResource buttonResource = dialogResource.getResource("button");
					new AlertDialog.Builder(NetworkLibraryActivity.this)
						.setTitle(boxResource.getResource("title").getValue())
						.setMessage((String) msg.obj)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
						.create().show();
				}
			}
		};

		UIUtil.wait("updatingCatalogsList", new Runnable() {
			public void run() {
				String error = null;
				try {
					view.runBackgroundUpdate(true);
				} catch (ZLNetworkException e) {
					error = e.getMessage();
				}
				handler.sendMessage(handler.obtainMessage(0, error));
			}
		}, this);
	}
}
