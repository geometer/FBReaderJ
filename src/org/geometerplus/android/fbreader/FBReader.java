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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yotadevices.sdk.utils.BitmapUtils;
import com.yotadevices.yotaphone2.fbreader.Consts;
import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;
import com.yotadevices.yotaphone2.fbreader.YotaBookContentPopup;
import com.yotadevices.yotaphone2.fbreader.YotaSettingsPopup;

import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.library.*;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.fbreader.options.CancelMenuHelper;
import org.geometerplus.fbreader.formats.ExternalFormatPlugin;
import org.geometerplus.fbreader.network.sync.SyncData;
import org.geometerplus.fbreader.tips.TipsManager;

import org.geometerplus.android.fbreader.api.*;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.httpd.DataService;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.sync.SyncOperations;
import org.geometerplus.android.fbreader.tips.TipsActivity;

import org.geometerplus.android.util.*;

public final class FBReader extends Activity implements ZLApplicationWindow, FBReaderApp.Notifier {
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

	final DataService.Connection DataConnection = new DataService.Connection();

	volatile boolean IsPaused = false;
	private volatile long myResumeTimestamp;
	volatile Runnable OnResumeAction = null;

	private Intent myCancelIntent = null;
	private Intent myOpenBookIntent = null;

	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);

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
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				myFBReaderApp.openBook(myBook, bookmark, new Runnable() {
					public void run() {
						if (action != null) {
							action.run();
						}
						hideBars();
						if ((DeviceType.Instance() == DeviceType.YOTA_PHONE) || (DeviceType.Instance() == DeviceType.YOTA_PHONE2)) {
							updateCoverOnYotaWidget(myFBReaderApp.Model.Book);
						}
					}
				}, FBReader.this);
				AndroidFontUtil.clearFontCache();
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
		if (DeviceType.Instance().isYotaPhone()) {
			setTheme(R.style.ActivityWithWhiteActionBar);
		}
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

		bindService(
			new Intent(this, DataService.class),
			DataConnection,
			DataService.BIND_AUTO_CREATE
		);

		final Config config = Config.Instance();
		config.runOnConnect(new Runnable() {
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
		if (!myShowActionBarFlag && !DeviceType.Instance().isYotaPhone()) {
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

		myFBReaderApp.setExternalFileOpener(new ExternalFileOpener(this));

		final ActionBar bar = getActionBar();
		bar.setDisplayOptions(
			ActionBar.DISPLAY_SHOW_CUSTOM,
			ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
		);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !DeviceType.Instance().isYotaPhone()) {
			bar.setDisplayUseLogoEnabled(false);
		}

		if (DeviceType.Instance().isYotaPhone()) {
			final View titleContainer = (View)getLayoutInflater().inflate(R.layout.yota_title_view, null);
			final TextView titleView = (TextView)titleContainer.findViewById(R.id.title);
			titleContainer.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					myFBReaderApp.runAction(ActionCode.SHOW_BOOK_INFO);
				}
			});

			bar.setCustomView(titleContainer);
			bar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
			bar.setLogo(new ColorDrawable(Color.WHITE));
			bar.setDisplayHomeAsUpEnabled(true);

			//Book currentBook = myFBReaderApp.Model.Book;
		}
		else {
			final TextView titleView = (TextView)getLayoutInflater().inflate(R.layout.title_view, null);
			titleView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					myFBReaderApp.runAction(ActionCode.SHOW_BOOK_INFO);
				}
			});

			bar.setCustomView(titleView);
			setTitle(myFBReaderApp.getTitle());
		}

		if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(YotaSelectionPopup.ID) == null) {
			new YotaSelectionPopup(myFBReaderApp, this);
		}
		if (myFBReaderApp.getPopupById(YotaTranslatePopup.ID) == null) {
			new YotaTranslatePopup(myFBReaderApp, this, getContentResolver());
		}
		if (myFBReaderApp.getPopupById(YotaDefinePopup.ID) == null) {
			new YotaDefinePopup(myFBReaderApp, this, getContentResolver());
		}
		if (myFBReaderApp.getPopupById(YotaSettingsPopup.ID) == null) {
			new YotaSettingsPopup(myFBReaderApp, this);
		}
		if (myFBReaderApp.getPopupById(YotaBookContentPopup.ID) == null) {
			new YotaBookContentPopup(myFBReaderApp, this, false);
		}
		myFBReaderApp.setFrontScreenActionMap();
		myFBReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.TOGGLE_BARS, new ToggleBarsAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(this, myFBReaderApp));
		if (DeviceType.Instance() == DeviceType.YOTA_PHONE || DeviceType.Instance() == DeviceType.YOTA_PHONE2) {
			myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new YotaSelectionShowPanelAction(this, myFBReaderApp, YotaSelectionPopup.ID));
			myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new YotaSelectionHidePanelAction(this, myFBReaderApp, YotaSelectionPopup.ID));
			myFBReaderApp.addAction(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, new YotaSwitchScreenAction(this, myFBReaderApp, true));
			myFBReaderApp.addAction(ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, new YotaSwitchScreenAction(this, myFBReaderApp, false));
			myFBReaderApp.addAction(ActionCode.YOTA_UPDATE_WIDGET, new YotaUpdateWidgetAction(this, myFBReaderApp));
			myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new YotaSelectionTranslateAction(this, myFBReaderApp, false));
			myFBReaderApp.addAction(ActionCode.SELECTION_DEFINE, new YotaSelectionDefineAction(this, myFBReaderApp, false));

			myFBReaderApp.addAction(ActionCode.YOTA_FONT_SETTINGS, new ShowYotaSettingsAction(this, myFBReaderApp));
			myFBReaderApp.addAction(ActionCode.YOTA_SEARCH_ACTION, new ShowYotaBookContentsAction(this, myFBReaderApp));
			myFBReaderApp.addAction(ActionCode.YOTA_ADD_BOOKMARK, new YotaToggleBookmark(this, myFBReaderApp));
		}
		else {
			myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
			myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
			myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, myFBReaderApp));
		}
		myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, myFBReaderApp));
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

//		Config.Instance().runOnConnect(new Runnable() {
//			public void run() {
//				if (myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.getValue()) {
//					new YotaSwitchScreenAction(FBReader.this, myFBReaderApp, true).run();
//				}
//			}
//		});

		final Intent intent = getIntent();
		final String action = intent.getAction();

		myOpenBookIntent = intent;
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
			if (FBReaderIntents.Action.CLOSE.equals(action)) {
				myCancelIntent = intent;
				myOpenBookIntent = null;
			} else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(action)) {
				myFBReaderApp.ExternalBook = null;
				myOpenBookIntent = null;
				getCollection().bindToService(this, new Runnable() {
					public void run() {
						myFBReaderApp.openBook(null, null, null, FBReader.this);
					}
				});
			}
		} else if (FBReaderIntents.Action.SHARE.equals(intent.getAction())) {
			final String subject = intent.getStringExtra(android.content.Intent.EXTRA_SUBJECT);
			final String text = intent.getStringExtra(android.content.Intent.EXTRA_TEXT);
			shareText(subject, text);
		}
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
			myOpenBookIntent = intent;
			if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
				final ExternalFormatPlugin plugin =
					(ExternalFormatPlugin)myFBReaderApp.ExternalBook.getPluginOrNull();
				try {
					startActivity(PluginUtil.createIntent(plugin, PluginUtil.ACTION_KILL));
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else if (FBReaderIntents.Action.PLUGIN.equals(action)) {
			new RunPluginAction(this, myFBReaderApp, data).run();
		} else if (FBReaderIntents.Action.SWITCH_YOTA_SCREEN.equals(action)) {
			new YotaSwitchScreenAction(FBReader.this, myFBReaderApp, true).run();
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
		} else if (FBReaderIntents.Action.CLOSE.equals(intent.getAction())) {
			myCancelIntent = intent;
			myOpenBookIntent = null;
		} else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(intent.getAction())) {
			final Book book = FBReaderIntents.getBookExtra(intent);
			myFBReaderApp.ExternalBook = null;
			myOpenBookIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					Book b = myFBReaderApp.Collection.getRecentBook(0);
					if (b.equals(book)) {
						b = myFBReaderApp.Collection.getRecentBook(1);
					}
					myFBReaderApp.openBook(b, null, null, FBReader.this);
				}
			});
		} else if (FBReaderIntents.Action.SHARE.equals(intent.getAction())) {
			final String subject = intent.getStringExtra(android.content.Intent.EXTRA_SUBJECT);
			final String text = intent.getStringExtra(android.content.Intent.EXTRA_TEXT);
			shareText(subject, text);
			setIntent(intent);
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
						getPostponedInitAction().run();
					}
				}.start();
				setTitleFromCurrentBook();
				ZLViewWidget widget = myFBReaderApp.getViewWidget();
				Book recentBook = getCollection().getRecentBook(0);
				if (myFBReaderApp.Model != null && myFBReaderApp.Model.Book != null) {
					if (!recentBook.equals(myFBReaderApp.Model.Book)) {
						myFBReaderApp.openBook(recentBook, null, null, null);
					}
				}
				if (widget != null) {
					widget.repaint();
				}
			}
		});

		initPluginActions();

		final ZLAndroidLibrary zlibrary = getZLibrary();

		Config.Instance().runOnConnect(new Runnable() {
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
				myFBReaderApp.ViewOptions.YotaFSColorProfileName.saveSpecialValue();
				myFBReaderApp.ViewOptions.YotaBSColorProfileName.saveSpecialValue();
				SetScreenOrientationAction.setOrientation(FBReader.this, zlibrary.getOrientationOption().getValue());
			}
		});
		if (DeviceType.Instance().isYotaPhone()) {
			if (myFBReaderApp.getTextView() != null) {
				myFBReaderApp.getTextView().clearSelection();
				myFBReaderApp.hideActivePopup();
			}
			((YotaSelectionPopup) myFBReaderApp.getPopupById(YotaSelectionPopup.ID)).setRootView(myRootView);
			((YotaTranslatePopup) myFBReaderApp.getPopupById(YotaTranslatePopup.ID)).setRootView(myRootView);
			((YotaDefinePopup) myFBReaderApp.getPopupById(YotaDefinePopup.ID)).setRootView(myRootView);
		}
		((PopupPanel) myFBReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel) myFBReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		switchWakeLock(hasFocus &&
			getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() <
			myFBReaderApp.getBatteryLevel()
		);
	}

	private void setTitleFromCurrentBook() {
		if (myFBReaderApp.Model != null && myFBReaderApp.Model.Book != null) {
			Book book = myFBReaderApp.Model.Book;
			final StringBuilder title = new StringBuilder(book.getTitle());
			if (!DeviceType.Instance().isYotaPhone()) {
				if (!book.authors().isEmpty()) {
					boolean first = true;
					for (Author a : book.authors()) {
						title.append(first ? " (" : ", ");
						title.append(a.DisplayName);
						first = false;
					}
					title.append(")");
				}
				setTitle(title.toString());
			} else {
				final StringBuilder authors = new StringBuilder("");
				if (!book.authors().isEmpty()) {
					boolean next = false;
					for (Author a : book.authors()) {
						if (next) {
							authors.append(", ");
						}
						authors.append(a.DisplayName);
						next = true;
					}
				}
				setTitle(title.toString(), authors.toString());
			}
		}
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
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				SyncOperations.enableSync(FBReader.this, myFBReaderApp.SyncOptions);

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

				//boolean startedFromBs = getIntent() != null && FBReaderIntents.Action.SWITCH_YOTA_SCREEN.equals(getIntent().getAction());
				if (myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.getValue()) {
					new YotaSwitchScreenAction(FBReader.this, myFBReaderApp, false).run();
				}
			}
		});

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		IsPaused = false;
		myResumeTimestamp = System.currentTimeMillis();
		if (OnResumeAction != null) {
			final Runnable action = OnResumeAction;
			OnResumeAction = null;
			action.run();
		}

		registerReceiver(mySyncUpdateReceiver, new IntentFilter(SyncOperations.UPDATED));

		SetScreenOrientationAction.setOrientation(this, ZLibrary.Instance().getOrientationOption().getValue());
		if (myCancelIntent != null) {
			final Intent intent = myCancelIntent;
			myCancelIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					runCancelAction(intent);
				}
			});
			return;
		} else if (myOpenBookIntent != null) {
			final Intent intent = myOpenBookIntent;
			myOpenBookIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					openBook(intent, null, true);
				}
			});
		} else if (myFBReaderApp.getCurrentServerBook(null) != null) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.useSyncInfo(true, FBReader.this);
				}
			});
		} else if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.openBook(myFBReaderApp.ExternalBook, null, null, FBReader.this);
				}
			});
		} else {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.useSyncInfo(true, FBReader.this);
				}
			});
		}

		PopupPanel.restoreVisibilities(myFBReaderApp);

		hideBars();

		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
		if (DeviceType.Instance().isYotaPhone()) {
			closeYotaReaderOnBSIfActive();
		}
	}

	@Override
	protected void onPause() {
		SyncOperations.quickSync(this, myFBReaderApp.SyncOptions);

		IsPaused = true;
		try {
			unregisterReceiver(mySyncUpdateReceiver);
		} catch (IllegalArgumentException e) {
		}
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
		unbindService(DataConnection);
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

	private Menu addSubmenu(Menu menu, String id) {
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
		if (DeviceType.Instance().isYotaPhone()) {
			menuItem.setOnMenuItemClickListener(myYotaMenuListener);
		}
		else {
			menuItem.setOnMenuItemClickListener(myMenuListener);
		}
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

	private void fillMenu(Menu menu, List<MenuNode> nodes) {
		for (MenuNode n : nodes) {
			if (n instanceof MenuNode.Item) {
				final Integer iconId = ((MenuNode.Item)n).IconId;
				if (iconId != null) {
					addMenuItem(menu, n.Code, iconId);
				} else {
					addMenuItem(menu, n.Code);
				}
			} else /* if (n instanceof MenuNode.Submenu) */ {
				final Menu submenu = addSubmenu(menu, n.Code);
				fillMenu(submenu, ((MenuNode.Submenu)n).Children);
			}
		}
	}

	private void setupMenu(Menu menu) {
		fillMenu(menu, MenuData.topLevelNodes());

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
		if (DeviceType.Instance().isYotaPhone()) {
			myMenuItemMap.clear();
			addMenuItem(menu, ActionCode.YOTA_FONT_SETTINGS, R.drawable.yota_font_settings_icon, "Font settings", true);
			addMenuItem(menu, ActionCode.YOTA_SEARCH_ACTION, R.drawable.yota_search_icon, "Contents", true);
			addMenuItem(menu, ActionCode.YOTA_ADD_BOOKMARK, R.drawable.yota_bookmark_icon, "Add bookmark", true);
			refresh();
		}
		else {
			setupMenu(menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (DeviceType.Instance().isYotaPhone() && item.getItemId() == android.R.id.home) {
			myFBReaderApp.runAction(ActionCode.SHOW_LIBRARY);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onPluginNotFound(final Book book) {
		getCollection().bindToService(this, new Runnable() {
			public void run() {
				final Book recent = getCollection().getRecentBook(0);
				if (recent != null && !recent.equals(book)) {
					myFBReaderApp.openBook(recent, null, null, null);
				} else {
					myFBReaderApp.openHelpBook();
				}
			}
		});
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

	public boolean barsAreShown() {
		return myNavigationPopup != null;
	}

	public void hideBars() {
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

		if (Build.VERSION.SDK_INT >= 19/*Build.VERSION_CODES.KITKAT*/
				&& zlibrary.EnableFullscreenModeOption.getValue()) {
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
			if (DeviceType.Instance().isYotaPhone()) {
				myNavigationPopup = new YotaNavigationPopup(myFBReaderApp);
			}
			else {
				myNavigationPopup = new NavigationPopup(myFBReaderApp);
			}
			myNavigationPopup.runNavigation(this, myRootView);
		}
	}

	public void setTitle(String titleText, String authorText) {
		final TextView title = (TextView) getActionBar().getCustomView().findViewById(R.id.title);
		final TextView author = (TextView) getActionBar().getCustomView().findViewById(R.id.author);
		if (title != null) {
			title.setText(titleText);
			title.postInvalidate();
		}
		if (author != null) {
			author.setText(authorText.toUpperCase());
			author.postInvalidate();
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		final TextView view = (TextView) getActionBar().getCustomView();
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
	public FBReaderApp.SynchronousExecutor createExecutor(String key) {
		return UIUtil.createExecutor(this, key);
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
		finish();
	}

	@Override
	public ZLViewWidget getViewWidget() {
		if (myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.getValue()) {
			if (com.yotadevices.yotaphone2.fbreader.FBReaderYotaService.mWidget != null) {
				return com.yotadevices.yotaphone2.fbreader.FBReaderYotaService.mWidget;
			}
		}
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

	private final MenuItem.OnMenuItemClickListener myYotaMenuListener =
			new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					myFBReaderApp.runAction(myMenuItemMap.get(item), item.getItemId());
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
					if (actionId.equals(ActionCode.YOTA_ADD_BOOKMARK)) {
						refreshYotaBookmarkState(menuItem);
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
		intent.setPackage(FBReaderIntents.DEFAULT_PACKAGE);
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

	@Override
	public void setWindowTitle(final String title, final String author) {
		runOnUiThread(new Runnable() {
			public void run() {
				setTitle(title, author);
			}
		});
	}

	public void closeYotaReaderOnBSIfActive() {
		Handler halder = new Handler(getMainLooper());
		halder.postDelayed( new Runnable() {
			@Override
			public void run() {
				Intent i = getIntent();
				if (!myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.getValue() &&
						i != null && !FBReaderIntents.Action.SHARE.equals(i.getAction())) {
					stopService(new Intent(FBReader.this, FBReaderYotaService.class));
				}
			}
		}, 1000);
	}

	public void refreshYotaScreen() {
		if (!myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.getValue()) {
			boolean isServiceRunning = false;
			final String serviceClassName = FBReaderYotaService.class.getName();
			final ActivityManager manager =
				(ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClassName.equals(info.service.getClassName())) {
					isServiceRunning = true;
					break;
				}
			}
			if (!isServiceRunning) {
				return;
			}
		}

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

	private void updateCoverOnYotaWidget(Book book) {
		final ZLImage image = BookUtil.getCover(book);
		if (image != null && image instanceof ZLImageProxy) {
			((ZLImageProxy)image).startSynchronization(myImageSynchronizer, new Runnable() {
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							sendCoverToYotaWidget(image);
						}
					});
				}
			});
		} else {
			sendCoverToYotaWidget(image);
		}
	}

	private String getYotaCoverImagePath() {
		final String cardPath = Paths.cardDirectory();
		return cardPath == null ? null : cardPath + File.separatorChar + "Books" + File.separatorChar + "coverimage.png";
	}

	private void sendCoverToYotaWidget(ZLImage cover) {
		final String filePath = getYotaCoverImagePath();
		File coverFile = filePath != null ? new File(getYotaCoverImagePath()) : null;
		if (coverFile != null && coverFile.exists()) {
			coverFile.delete();
		}
		if (cover != null) {
			final ZLAndroidImageData data =
					((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(cover);
			final DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			final int maxHeight = metrics.heightPixels / 3;
			final int maxWidth = metrics.widthPixels / 3;
			Intent i = new Intent(Consts.YOTA_WIDGET_SET_COVER_ACTION);
			if (data != null) {
				final Bitmap coverBitmap = data.getBitmap(maxWidth, maxHeight);
				if (coverBitmap != null && coverFile != null) {
					try {
						Bitmap outBitmap = BitmapUtils.toGrayscale(coverBitmap, coverBitmap.getWidth(), coverBitmap.getHeight());
						outBitmap = BitmapUtils.prepareImageForBS(outBitmap);
						outBitmap = BitmapUtils.ditherBitmap(outBitmap, BitmapUtils.DITHER_ATKINSON, false);

						FileOutputStream out = new FileOutputStream(coverFile);
						outBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.close();
						i.putExtra(Consts.YOTA_COVER_KEY, coverFile.getAbsolutePath());
					}
					catch (IOException e) {

					}
				}
			}
			this.sendBroadcast(i);
		}
		else {
			Intent i = new Intent(Consts.YOTA_WIDGET_SET_COVER_ACTION);
			this.sendBroadcast(i);
		}
	}

	private BroadcastReceiver mySyncUpdateReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			myFBReaderApp.useSyncInfo(myResumeTimestamp + 10 * 1000 > System.currentTimeMillis(), FBReader.this);
		}
	};

	@Override
	public void showMissingBookNotification(SyncData.ServerBookInfo info) {
		final String errorMessage =
			ZLResource.resource("errorMessage").getResource("bookIsMissing").getValue()
				.replace("%s", info.Title);

		final NotificationManager notificationManager =
			(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
		final Notification notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.fbreader)
			.setTicker(info.Title)
			.setContentTitle(info.Title)
			.setContentText(errorMessage)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.build();
		notificationManager.notify(0, notification);
	}

	private void shareText(final String subject, final String text) {
		Handler halder = new Handler(getMainLooper());
		halder.postDelayed( new Runnable() {
			@Override
			public void run() {
				final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
				intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
				Intent chooser = Intent.createChooser(intent, null);;
				startActivity(chooser);
			}
		}, 200);
	}

	private void refreshYotaBookmarkState(MenuItem item) {
		final ZLTextView textView = myFBReaderApp.getTextView();
			if (textView.hasBookmarks()) {
				item.setIcon(R.drawable.yota_delete_bookmark_icon);
			} else {
				item.setIcon(R.drawable.yota_bookmark_icon);
			}
	}
}
