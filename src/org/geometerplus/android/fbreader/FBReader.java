/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yotadevices.fbreader.FBReaderYotaService;

import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.library.*;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.fbreader.options.CancelMenuHelper;
import org.geometerplus.fbreader.tips.TipsManager;

import org.geometerplus.android.fbreader.api.*;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tips.TipsActivity;

import org.geometerplus.android.util.*;

public final class FBReader extends Activity implements ZLApplicationWindow {
	static final int ACTION_BAR_COLOR = Color.DKGRAY;

	public static final int REQUEST_PREFERENCES = 1;
	public static final int REQUEST_CANCEL_MENU = 2;

	public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
	public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;

	public static void openBookActivity(Context context, Book book, Bookmark bookmark) {
		final Intent intent = new Intent(context, FBReader.class)
			.setAction(FBReaderIntents.Action.VIEW)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		FBReaderIntents.putBookExtra(intent, book);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		context.startActivity(intent);
	}

	private static ZLAndroidLibrary getZLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}

	private FBReaderApp myFBReaderApp;
	private volatile Book myBook;

	private RelativeLayout myRootView;
	private ZLAndroidWidget myMainView;

	private volatile boolean myShowStatusBarFlag;
	private volatile boolean myShowActionBarFlag;
	private volatile boolean myActionBarIsVisible;

	private static final String PLUGIN_ACTION_PREFIX = "___";
	private final List<PluginApi.ActionInfo> myPluginActions =
		new LinkedList<PluginApi.ActionInfo>();
	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo>getParcelableArrayList(PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					int index = 0;
					while (index < myPluginActions.size()) {
						myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						myFBReaderApp.addAction(
							PLUGIN_ACTION_PREFIX + index++,
							new RunPluginAction(FBReader.this, myFBReaderApp, info.getId())
						);
					}
					if (!myPluginActions.isEmpty()) {
						invalidateOptionsMenu();
					}
				}
			}
		}
	};

	private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
		if (!force && myBook != null) {
			return;
		}

		myBook = FBReaderIntents.getBookExtra(intent);
		final Bookmark bookmark = FBReaderIntents.getBookmarkExtra(intent);
		if (myBook == null) {
			final Uri data = intent.getData();
			if (data != null) {
				myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
			}
		}
		if (myBook != null) {
			ZLFile file = myBook.File;
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}
				UIUtil.showErrorMessage(this, "fileNotFound", file.getPath());
				myBook = null;
			}
		}
		Config.Instance().runOnStart(new Runnable() {
			public void run() {
				myFBReaderApp.openBook(myBook, bookmark, new Runnable() {
					public void run() {
						if (action != null) {
							action.run();
						}
						hideBars();
						if (DeviceType.Instance() == DeviceType.YOTA_PHONE) {
							refreshYotaScreen();
						}
					}
				});
			}
		});
	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myFBReaderApp.Collection.getBookByFile(file);
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myFBReaderApp.Collection.getBookByFile(child);
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	private Runnable getPostponedInitAction() {
		return new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						new TipRunner().start();
						DictionaryUtil.init(FBReader.this, null);
						final Intent intent = getIntent();
						if (intent != null && FBReaderIntents.Action.PLUGIN.equals(intent.getAction())) {
							new RunPluginAction(FBReader.this, myFBReaderApp, intent.getData()).run();
						}
					}
				});
			}
		};
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

		startService(new Intent(this, org.geometerplus.android.fbreader.httpd.DataService.class));

		final Config config = Config.Instance();
		config.runOnStart(new Runnable() {
			public void run() {
				config.requestAllValuesForGroup("Options");
				config.requestAllValuesForGroup("Style");
				config.requestAllValuesForGroup("LookNFeel");
				config.requestAllValuesForGroup("Fonts");
				config.requestAllValuesForGroup("Colors");
				config.requestAllValuesForGroup("Files");
			}
		});

		final ZLAndroidLibrary zlibrary = getZLibrary();
		myShowStatusBarFlag = zlibrary.ShowStatusBarOption.getValue();
		myShowActionBarFlag = zlibrary.ShowActionBarOption.getValue();
		myActionBarIsVisible = myShowActionBarFlag;

		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
		);
		if (!myShowActionBarFlag) {
			requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}
		setContentView(R.layout.main);
		myRootView = (RelativeLayout)findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget)findViewById(R.id.main_view);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		zlibrary.setActivity(this);

		myFBReaderApp = (FBReaderApp)FBReaderApp.Instance();
		if (myFBReaderApp == null) {
			myFBReaderApp = new FBReaderApp(new BookCollectionShadow());
		}
		getCollection().bindToService(this, null);
		myBook = null;

		myFBReaderApp.setWindow(this);
		myFBReaderApp.initWindow();

		final ActionBar bar = getActionBar();
		bar.setDisplayOptions(
			ActionBar.DISPLAY_SHOW_CUSTOM,
			ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
		);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			bar.setDisplayUseLogoEnabled(false);
		}
		final TextView titleView = (TextView)getLayoutInflater().inflate(R.layout.title_view, null);
		titleView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				myFBReaderApp.runAction(ActionCode.SHOW_BOOK_INFO);
			}
		});
		bar.setCustomView(titleView);
		bar.setBackgroundDrawable(new ColorDrawable(ACTION_BAR_COLOR));

		setTitle(myFBReaderApp.getTitle());

		if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myFBReaderApp);
		}

		myFBReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.TOGGLE_BARS, new ToggleBarsAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SENSOR));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
		myFBReaderApp.addAction(ActionCode.OPEN_WEB_HELP, new OpenWebHelpAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.INSTALL_PLUGINS, new InstallPluginsAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, new YotaSwitchScreenAction(this, myFBReaderApp, true));
		myFBReaderApp.addAction(ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, new YotaSwitchScreenAction(this, myFBReaderApp, false));
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		final Uri data = intent.getData();

		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action)
				   && data != null && "fbreader-action".equals(data.getScheme())) {
			myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					openBook(intent, null, true);
				}
			});
		} else if (FBReaderIntents.Action.PLUGIN.equals(action)) {
			new RunPluginAction(this, myFBReaderApp, data).run();
		} else if (Intent.ACTION_SEARCH.equals(action)) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup)myFBReaderApp.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					myFBReaderApp.MiscOptions.TextSearchPattern.setValue(pattern);
					if (myFBReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								myFBReaderApp.showPopup(popup.getId());
								hideBars();
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FBReader.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		getCollection().bindToService(this, new Runnable() {
			public void run() {
				new Thread() {
					public void run() {
						openBook(getIntent(), getPostponedInitAction(), false);
						myFBReaderApp.getViewWidget().repaint();
					}
				}.start();

				myFBReaderApp.getViewWidget().repaint();
			}
		});

		initPluginActions();

		final ZLAndroidLibrary zlibrary = getZLibrary();

		Config.Instance().runOnStart(new Runnable() {
			public void run() {
				final boolean showStatusBar = zlibrary.ShowStatusBarOption.getValue();
				final boolean showActionBar = zlibrary.ShowActionBarOption.getValue();
				if (showStatusBar != myShowStatusBarFlag || showActionBar != myShowActionBarFlag) {
					finish();
					startActivity(new Intent(FBReader.this, FBReader.class));
				}
				zlibrary.ShowStatusBarOption.saveSpecialValue();
				zlibrary.ShowActionBarOption.saveSpecialValue();
				myFBReaderApp.ViewOptions.ColorProfileName.saveSpecialValue();
				SetScreenOrientationAction.setOrientation(FBReader.this, zlibrary.getOrientationOption().getValue());
			}
		});

		((PopupPanel)myFBReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel)myFBReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		switchWakeLock(hasFocus &&
			getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() <
			myFBReaderApp.getBatteryLevel()
		);
	}

	private void initPluginActions() {
		synchronized (myPluginActions) {
			if (!myPluginActions.isEmpty()) {
				int index = 0;
				while (index < myPluginActions.size()) {
					myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
				}
				myPluginActions.clear();
				invalidateOptionsMenu();
			}
		}

		sendOrderedBroadcast(
			new Intent(PluginApi.ACTION_REGISTER).addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES),
			null,
			myPluginInfoReceiver,
			null,
			RESULT_OK,
			null,
			null
		);
	}

	private class TipRunner extends Thread {
		TipRunner() {
			setPriority(MIN_PRIORITY);
		}

		public void run() {
			final TipsManager manager = TipsManager.Instance();
			switch (manager.requiredAction()) {
				case Initialize:
					startActivity(new Intent(
						TipsActivity.INITIALIZE_ACTION, null, FBReader.this, TipsActivity.class
					));
					break;
				case Show:
					startActivity(new Intent(
						TipsActivity.SHOW_TIP_ACTION, null, FBReader.this, TipsActivity.class
					));
					break;
				case Download:
					manager.startDownloading();
					break;
				case None:
					break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		myStartTimer = true;
		Config.Instance().runOnStart(new Runnable() {
			public void run() {
				final int brightnessLevel =
					getZLibrary().ScreenBrightnessLevelOption.getValue();
				if (brightnessLevel != 0) {
					setScreenBrightness(brightnessLevel);
				} else {
					setScreenBrightnessAuto();
				}
				if (getZLibrary().DisableButtonLightsOption.getValue()) {
					setButtonLight(false);
				}

				getCollection().bindToService(FBReader.this, new Runnable() {
					public void run() {
						final BookModel model = myFBReaderApp.Model;
						if (model == null || model.Book == null) {
							return;
						}
						onPreferencesUpdate(myFBReaderApp.Collection.getBookById(model.Book.getId()));
					}
				});
			}
		});

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		PopupPanel.restoreVisibilities(myFBReaderApp);

		hideBars();

		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
	}

	@Override
	protected void onPause() {
		try {
			unregisterReceiver(myBatteryInfoReceiver);
		} catch (IllegalArgumentException e) {
			// do nothing, this exception means myBatteryInfoReceiver was not registered
		}
		myFBReaderApp.stopTimer();
		if (getZLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(true);
		}
		myFBReaderApp.onWindowClosing();
		super.onPause();
	}

	@Override
	protected void onStop() {
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(myFBReaderApp, this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getCollection().unbind();
		stopService(new Intent(this, org.geometerplus.android.fbreader.httpd.DataService.class));
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		myFBReaderApp.onWindowClosing();
		super.onLowMemory();
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		myFBReaderApp.hideActivePopup();
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
			manager.setOnCancelListener(new SearchManager.OnCancelListener() {
				public void onCancel() {
					if (popup != null) {
						myFBReaderApp.showPopup(popup.getId());
					}
					manager.setOnCancelListener(null);
				}
			});
			startSearch(myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(
				this, FBReader.class, myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface di) {
						if (popup != null) {
							myFBReaderApp.showPopup(popup.getId());
						}
					}
				}
			);
		}
		return true;
	}

	public void showSelectionPanel() {
		final ZLTextView view = myFBReaderApp.getTextView();
		((SelectionPopup)myFBReaderApp.getPopupById(SelectionPopup.ID))
			.move(view.getSelectionStartY(), view.getSelectionEndY());
		myFBReaderApp.showPopup(SelectionPopup.ID);
		hideBars();
	}

	public void hideSelectionPanel() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			myFBReaderApp.hideActivePopup();
		}
	}

	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_PREFERENCES:
				if (resultCode != RESULT_DO_NOTHING) {
					invalidateOptionsMenu();
					final Book book = data != null ? FBReaderIntents.getBookExtra(data) : null;
					if (book != null) {
						getCollection().bindToService(this, new Runnable() {
							public void run() {
								onPreferencesUpdate(book);
							}
						});
					}
				}
				break;
			case REQUEST_CANCEL_MENU:
				runCancelAction(data);
				break;
		}
	}

	private void runCancelAction(Intent intent) {
		final CancelMenuHelper.ActionType type;
		try {
			type = CancelMenuHelper.ActionType.valueOf(
				intent.getStringExtra(FBReaderIntents.Key.TYPE)
			);
		} catch (Exception e) {
			// invalid (or null) type value
			return;
		}
		Bookmark bookmark = null;
		if (type == CancelMenuHelper.ActionType.returnTo) {
			bookmark = FBReaderIntents.getBookmarkExtra(intent);
			if (bookmark == null) {
				return;
			}
		}
		myFBReaderApp.runCancelAction(type, bookmark);
	}

	private Menu addSubMenu(Menu menu, String id) {
		return menu.addSubMenu(ZLResource.resource("menu").getResource(id).getValue());
	}

	private void addMenuItem(Menu menu, String actionId, Integer iconId, String name, boolean showInActionBar) {
		if (name == null) {
			name = ZLResource.resource("menu").getResource(actionId).getValue();
		}
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
			if (showInActionBar) {
				menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			} else {
				menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			}
		}
		menuItem.setOnMenuItemClickListener(myMenuListener);
		myMenuItemMap.put(menuItem, actionId);
	}

	private void addMenuItem(Menu menu, String actionId, String name) {
		addMenuItem(menu, actionId, null, name, false);
	}

	private void addMenuItem(Menu menu, String actionId, int iconId) {
		addMenuItem(menu, actionId, iconId, null, myActionBarIsVisible);
	}

	private void addMenuItem(Menu menu, String actionId) {
		addMenuItem(menu, actionId, null, null, false);
	}

	private void setupMenu(Menu menu) {
		addMenuItem(menu, ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library);
		if (DeviceType.Instance() == DeviceType.YOTA_PHONE) {
			addMenuItem(menu, ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, R.drawable.ic_menu_p2b);
			//addMenuItem(menu, ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, R.drawable.ic_menu_p2b);
		}
		addMenuItem(menu, ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary);
		addMenuItem(menu, ActionCode.SHOW_TOC, R.drawable.ic_menu_toc);
		addMenuItem(menu, ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks);
		addMenuItem(menu, ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night);
		addMenuItem(menu, ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day);
		addMenuItem(menu, ActionCode.SEARCH, R.drawable.ic_menu_search);
		addMenuItem(menu, ActionCode.SHARE_BOOK, R.drawable.ic_menu_search);
		addMenuItem(menu, ActionCode.SHOW_PREFERENCES);
		addMenuItem(menu, ActionCode.SHOW_BOOK_INFO);
		final Menu subMenu = addSubMenu(menu, "screenOrientation");
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_SYSTEM);
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_SENSOR);
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT);
		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE);
		if (ZLibrary.Instance().supportsAllOrientations()) {
			addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		}
		addMenuItem(menu, ActionCode.INCREASE_FONT);
		addMenuItem(menu, ActionCode.DECREASE_FONT);
		addMenuItem(menu, ActionCode.INSTALL_PLUGINS);
		addMenuItem(menu, ActionCode.OPEN_WEB_HELP);
		synchronized (myPluginActions) {
			int index = 0;
			for (PluginApi.ActionInfo info : myPluginActions) {
				if (info instanceof PluginApi.MenuActionInfo) {
					addMenuItem(
						menu,
						PLUGIN_ACTION_PREFIX + index++,
						((PluginApi.MenuActionInfo)info).MenuItemName
					);
				}
			}
		}

		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		setupMenu(menu);

		return true;
	}

	private void setStatusBarVisibility(boolean visible) {
		final ZLAndroidLibrary zlibrary = getZLibrary();
		if (DeviceType.Instance() != DeviceType.KINDLE_FIRE_1ST_GENERATION && !myShowStatusBarFlag) {
			myMainView.setPreserveSize(visible);
			if (visible) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
		}
	}

	private NavigationPopup myNavigationPopup;

	boolean barsAreShown() {
		return myNavigationPopup != null;
	}

	void hideBars() {
		if (myNavigationPopup != null) {
			myNavigationPopup.stopNavigation();
			myNavigationPopup = null;
		}

		final ZLAndroidLibrary zlibrary = getZLibrary();
		if (!myShowActionBarFlag) {
			getActionBar().hide();
			myActionBarIsVisible = false;
			invalidateOptionsMenu();
		}

		if (Build.VERSION.SDK_INT >= 19/*Build.VERSION_CODES.KITKAT*/) {
			if (zlibrary.EnableFullscreenModeOption.getValue()) {
				myRootView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE |
					2048 /*View.SYSTEM_UI_FLAG_IMMERSIVE*/ |
					4096 /*View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY*/ |
					4 /*View.SYSTEM_UI_FLAG_FULLSCREEN*/ |
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				);
			} else if (zlibrary.DisableButtonLightsOption.getValue()) {
				myRootView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE
				);
			}
		} else {
			if (zlibrary.DisableButtonLightsOption.getValue()) {
				myRootView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE
				);
			}
		}

		setStatusBarVisibility(false);
	}

	void showBars() {
		setStatusBarVisibility(true);

		getActionBar().show();
		myActionBarIsVisible = true;
		invalidateOptionsMenu();

		myRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

		if (myNavigationPopup == null) {
			myFBReaderApp.hideActivePopup();
			myNavigationPopup = new NavigationPopup(myFBReaderApp);
			myNavigationPopup.runNavigation(this, myRootView);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		final TextView view = (TextView)getActionBar().getCustomView();
		if (view != null) {
			view.setText(title);
			view.postInvalidate();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	private void setButtonLight(boolean enabled) {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.buttonBrightness = enabled ? -1.0f : 0.0f;
		getWindow().setAttributes(attrs);
	}

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;

	public final void createWakeLock() {
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock =
						((PowerManager)getSystemService(POWER_SERVICE))
							.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			myFBReaderApp.startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra("level", 100);
			final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
			setBatteryLevel(level);
			switchWakeLock(
				hasWindowFocus() &&
				getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level
			);
		}
	};

	private void setScreenBrightnessAuto() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	public void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
	}

	public int getScreenBrightness() {
		final int level = (int)(100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private BookCollectionShadow getCollection() {
		return (BookCollectionShadow)myFBReaderApp.Collection;
	}

	// methods from ZLApplicationWindow interface
	@Override
	public void showErrorMessage(String key) {
		UIUtil.showErrorMessage(this, key);
	}

	@Override
	public void showErrorMessage(String key, String parameter) {
		UIUtil.showErrorMessage(this, key, parameter);
	}

	@Override
	public void runWithMessage(String key, Runnable action, Runnable postAction) {
		UIUtil.runWithMessage(this, key, action, postAction, false);
	}

	private int myBatteryLevel;
	@Override
	public int getBatteryLevel() {
		return myBatteryLevel;
	}
	private void setBatteryLevel(int percent) {
		myBatteryLevel = percent;
	}

	@Override
	public void close() {
		((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).finish();
	}

	@Override
	public ZLViewWidget getViewWidget() {
		return myMainView;
	}

	private final HashMap<MenuItem,String> myMenuItemMap = new HashMap<MenuItem,String>();

	private final MenuItem.OnMenuItemClickListener myMenuListener =
		new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				myFBReaderApp.runAction(myMenuItemMap.get(item));
				return true;
			}
		};

	@Override
	public void refresh() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (Map.Entry<MenuItem,String> entry : myMenuItemMap.entrySet()) {
					final String actionId = entry.getValue();
					final MenuItem menuItem = entry.getKey();
					menuItem.setVisible(myFBReaderApp.isActionVisible(actionId) && myFBReaderApp.isActionEnabled(actionId));
					switch (myFBReaderApp.isActionChecked(actionId)) {
						case B3_TRUE:
							menuItem.setCheckable(true);
							menuItem.setChecked(true);
							break;
						case B3_FALSE:
							menuItem.setCheckable(true);
							menuItem.setChecked(false);
							break;
						case B3_UNDEFINED:
							menuItem.setCheckable(false);
							break;
					}
				}

				if (myNavigationPopup != null) {
					myNavigationPopup.update();
				}
			}
		});
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();

		final Intent intent = new Intent(
			FBReaderIntents.Action.ERROR,
			new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
		);
		intent.putExtra(ErrorKeys.MESSAGE, exception.getMessage());
		final StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		intent.putExtra(ErrorKeys.STACKTRACE, stackTrace.toString());
		/*
		if (exception instanceof BookReadingException) {
			final ZLFile file = ((BookReadingException)exception).File;
			if (file != null) {
				intent.putExtra("file", file.getPath());
			}
		}
		*/
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// ignore
			e.printStackTrace();
		}
	}

	@Override
	public void setWindowTitle(final String title) {
		runOnUiThread(new Runnable() {
			public void run() {
				setTitle(title);
			}
		});
	}

	public void refreshYotaScreen() {
		final Intent intent = new Intent(this, FBReaderYotaService.class);
		intent.putExtra(
			FBReaderYotaService.KEY_BACK_SCREEN_IS_ACTIVE,
			myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.getValue()
		);
		if (myFBReaderApp.Model != null) {
			FBReaderIntents.putBookExtra(intent, myFBReaderApp.Model.Book);
		}
		try {
			startService(intent);
		} catch (Throwable t) {
			// ignore
		}
	}
}
