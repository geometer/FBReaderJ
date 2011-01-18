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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.BaseAdapter;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkLibrary;

public class NetworkLibraryActivity extends NetworkBaseActivity {
	private NetworkTree myTree;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	}

	private void prepareView() {
		if (myTree == null) {
			myTree = NetworkLibrary.Instance().getTree();
			setListAdapter(new LibraryAdapter());
			getListView().invalidateViews();
		}
	}

	private static Initializator myInitializator; 

	@Override
	public void onResume() {
		super.onResume();
		if (!NetworkView.Instance().isInitialized()) {
			if (myInitializator == null) {
				myInitializator = new Initializator(this);
				myInitializator.start();
			} else {
				myInitializator.setActivity(this);
			}
		} else {
			prepareView();
		}
	}

	@Override
	public void onDestroy() {
		if (!NetworkView.Instance().isInitialized()
				&& myInitializator != null) {
			myInitializator.setActivity(null);
		}
		super.onDestroy();
	}

	private static class Initializator extends Handler {
		private NetworkLibraryActivity myActivity;

		public Initializator(NetworkLibraryActivity activity) {
			myActivity = activity;
		}

		public void setActivity(NetworkLibraryActivity activity) {
			myActivity = activity;
		}

		final DialogInterface.OnClickListener myListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					Initializator.this.start();
				} else if (myActivity != null) {
					myActivity.finish();
				}
			}
		};

		// run this method only if myActivity != null
		private void runInitialization() {
			UIUtil.wait("loadingNetworkLibrary", new Runnable() {
				public void run() {
					String error = null;
					try {
						NetworkView.Instance().initialize();
					} catch (ZLNetworkException e) {
						error = e.getMessage();
					}
					Initializator.this.end(error);
				}
			}, myActivity);
		}

		// run this method only if myActivity != null
		private void processResults(String error) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			final ZLResource boxResource = dialogResource.getResource("networkError");
			final ZLResource buttonResource = dialogResource.getResource("button");
			new AlertDialog.Builder(myActivity)
				.setTitle(boxResource.getResource("title").getValue())
				.setMessage(error)
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("tryAgain").getValue(), myListener)
				.setNegativeButton(buttonResource.getResource("cancel").getValue(), myListener)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						myListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
					}
				})
				.create().show();
		}

		@Override
		public void handleMessage(Message message) {
			if (myActivity == null) {
				return;
			} else if (message.what == 0) {
				runInitialization(); // run initialization process
			} else if (message.obj == null) {
				myActivity.startService(new Intent(myActivity.getApplicationContext(), LibraryInitializationService.class));
				myActivity.prepareView(); // initialization is complete successfully
			} else {
				processResults((String) message.obj); // handle initialization error
			}
		}

		public void start() {
			sendEmptyMessage(0);
		}

		private void end(String error) {
			sendMessage(obtainMessage(1, error));
		}
	}


	private final class LibraryAdapter extends BaseAdapter {
		public final int getCount() {
			if (!NetworkView.Instance().isInitialized()) {
				return 0;
			}
			return myTree.subTrees().size() + 2; // subtrees + <search item>
		}

		public final NetworkTree getItem(int position) {
			final int size = myTree.subTrees().size();
			if (position == 0) {
				return NetworkView.Instance().getSearchItemTree();
			} else if (position > 0 && position <= size) {
				return (NetworkTree)myTree.subTrees().get(position - 1);
			} else if (position == size + 1) {
				return NetworkView.Instance().getAddCustomCatalogItemTree();
			}
			return null;
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final NetworkTree tree = getItem(position);
			return setupNetworkTreeItemView(convertView, parent, tree);
		}
	}


	protected MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
		final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean searchInProgress = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
		menu.findItem(MENU_SEARCH).setEnabled(!searchInProgress);
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
					library.invalidateChildren();
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
		if (NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY)) {
			return false;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	@Override
	public void onModelChanged() {
		getListView().invalidateViews();
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
