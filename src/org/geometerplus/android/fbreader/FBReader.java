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
import java.lang.reflect.Field;
import java.util.*;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filetypes.FileType;
import org.geometerplus.zlibrary.core.filetypes.FileTypeCollection;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;
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
import org.geometerplus.fbreader.formats.*;
import org.geometerplus.fbreader.tips.TipsManager;

import org.geometerplus.android.fbreader.api.*;
import org.geometerplus.android.fbreader.httpd.DataService;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tips.TipsActivity;

import org.geometerplus.android.util.*;

public final class FBReader extends Activity implements ZLApplicationWindow {
	private class ExtFileOpener implements FBReaderApp.ExternalFileOpener {
		private void showErrorDialog(final String errName) {
			runOnUiThread(new Runnable() {
				public void run() {
					final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
					final AlertDialog dialog = new AlertDialog.Builder(FBReader.this)
						.setTitle(title)
						.setIcon(0)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.create();
					if (myIsPaused) {
						myDialogToShow = dialog;
					} else {
						dialog.show();
					}
				}
			});
		}

		public boolean openFile(ZLFile f, String appData) {
			if (f == null) {
				showErrorDialog("unzipFailed");
				return false;
			}
			String extension = f.getExtension();
			Uri uri = Uri.parse("file://" + f.getPath());
			Intent launchIntent = new Intent(Intent.ACTION_VIEW);
			launchIntent.setPackage(appData);
			launchIntent.setData(uri);
			FileType ft = FileTypeCollection.Instance.typeForFile(f);
			for (MimeType type : ft.mimeTypes()) {
				launchIntent.setDataAndType(uri, type.Name);
				try {
					startActivity(launchIntent);
					return true;
				} catch (ActivityNotFoundException e) {
				}
			}
			showErrorDialog("externalNotFound");
			return false;
		}
	}

	private class PluginFileOpener implements FBReaderApp.PluginFileOpener {
		private void showErrorDialog(final String errName, final String appData) {
			runOnUiThread(new Runnable() {
				public void run() {
					final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
					final AlertDialog dialog = new AlertDialog.Builder(FBReader.this)
						.setTitle(title)
						.setIcon(0)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setData(Uri.parse("market://search?q=" + appData));
								startActivity(i);
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								onPluginAbsent();
							}
						})
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								onPluginAbsent();
							}
						})
						.create();
					if (myIsPaused) {
						myDialogToShow = dialog;
					} else {
						dialog.show();
					}
				}
			});
		}

		public void openFile(String appData, Book book, Bookmark bookmark) {
			final Intent launchIntent = new Intent("android.fbreader.action.VIEW_PLUGIN");
			launchIntent.setPackage(appData);
			//			Uri uri = Uri.parse("file://" + book.File.getPath());
			//			launchIntent.setData(uri);
			FBReaderIntents.putBookExtra(launchIntent, book);
			FBReaderIntents.putBookmarkExtra(launchIntent, bookmark);
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			try {
				startActivity(launchIntent);
				overridePendingTransition(0, 0);
			} catch (ActivityNotFoundException e) {
				showErrorDialog("noPlugin", appData);
			}
		}
	}

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
	private String myMenuLanguage;

	final DataService.Connection DataConnection = new DataService.Connection();

	private boolean myIsPaused = false;
	private AlertDialog myDialogToShow = null;

	private boolean myNeedToOpenFile = false;
	private Intent myIntentToOpen = null;

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
		if (bookmark == null) {
			Log.d("bookmark", "null!!!!1111");
		}
		if (myBook == null) {
			final Uri data = intent.getData();
			if (data != null) {
				myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
			}
		}
		Log.d("fbreader", "filePath");
		if (myBook != null) {
			Log.d("fbreader", myBook.File.getPath());
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
				myFBReaderApp.openBook(myBook, bookmark, action);
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
		//		if (file.isArchive()) {
		//			for (ZLFile child : file.children()) {
		//				book = myFBReaderApp.Collection.getBookByFile(child);
		//				if (book != null) {
		//					return book;
		//				}
		//			}
		//		}
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

		bindService(
			new Intent(this, DataService.class),
			DataConnection,
			DataService.BIND_AUTO_CREATE
		);

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

		requestWindowFeature(Window.FEATURE_NO_TITLE);
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

		//		if (!myFBReaderApp.externalFileOpenerIsSet()) {
		myFBReaderApp.setExternalFileOpener(new ExtFileOpener());
		//		}
		//		if (!myFBReaderApp.pluginFileOpenerIsSet()) {
		myFBReaderApp.setPluginFileOpener(new PluginFileOpener());
		//		}

		myNeedToOpenFile = true;
		myIntentToOpen = getIntent();
		myNeedToSkipPlugin = true;

		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
		);

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

		myFBReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myFBReaderApp));
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

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
			if ("android.fbreader.action.CLOSE".equals(getIntent().getAction()) ) {
				myCancelCalled = true;
				myCancelIntent = getIntent();
			} else if ("android.fbreader.action.PLUGIN_CRASH".equals(getIntent().getAction())) {
				Log.d("fbj", "crash in oncreate");
				myNeedToSkipPlugin = true;
				myFBReaderApp.Model = null;
				getCollection().bindToService(this, new Runnable() {
					public void run() {
						myFBReaderApp.openBook(myFBReaderApp.Collection.getRecentBook(0), null, null);
					}
				});
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setStatusBarVisibility(true);
		setupMenu(menu);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
		setStatusBarVisibility(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		setStatusBarVisibility(false);
		return super.onOptionsItemSelected(item);
	}

	private boolean myCancelCalled = false;
	private boolean myNeedToSkipPlugin = false;

	private Intent myCancelIntent = null;

	@Override
	protected void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		final Uri data = intent.getData();

		if (Intent.ACTION_VIEW.equals(action)) {
			myNeedToSkipPlugin = true;
		}

		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action)
				   && data != null && "fbreader-action".equals(data.getScheme())) {
			myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
			myNeedToOpenFile = true;
			myIntentToOpen = intent;
			myNeedToSkipPlugin = true;
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
								hideBars();
								myFBReaderApp.showPopup(popup.getId());
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
		} else if ("android.fbreader.action.CLOSE".equals(intent.getAction())) {
			myCancelCalled = true;
			myCancelIntent = intent;
		} else if ("android.fbreader.action.PLUGIN_CRASH".equals(intent.getAction())) {
			Log.d("fbj", "crash");
			long bookid = intent.getLongExtra("BOOKID", -1);
			myNeedToSkipPlugin = true;
			myFBReaderApp.Model = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.openBook(myFBReaderApp.Collection.getRecentBook(0), null, null);
				}
			});
		} else {
			super.onNewIntent(intent);
			if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
				myNeedToOpenFile = true;
				myIntentToOpen = intent;
				myNeedToSkipPlugin = true;
				if (intent.getBooleanExtra("KILL_PLUGIN", false)) {
					Log.d("fbreader", "killing plugin");
					if (myFBReaderApp.Model != null && myFBReaderApp.Model.Book != null) {
						final FormatPlugin p = PluginCollection.Instance().getPlugin(myFBReaderApp.Model.Book.File);
						if (p.type() == FormatPlugin.Type.PLUGIN) {
							String pack = ((PluginFormatPlugin)p).getPackage();
							final Intent i = new Intent("android.fbreader.action.KILL_PLUGIN");
							i.setPackage(pack);
							Log.d("fbreader", pack);
							try {
								startActivity(i);
							} catch (ActivityNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
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

				myFBReaderApp.getViewWidget().repaint();
			}
		});

		initPluginActions();

		final ZLAndroidLibrary zlibrary = getZLibrary();

		Config.Instance().runOnStart(new Runnable() {
			public void run() {
				final boolean showStatusBar = zlibrary.ShowStatusBarOption.getValue();
				if (showStatusBar != myShowStatusBarFlag) {
					finish();
					startActivity(new Intent(FBReader.this, FBReader.class));
				}
				zlibrary.ShowStatusBarOption.saveSpecialValue();
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
			int index = 0;
			while (index < myPluginActions.size()) {
				myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
			}
			myPluginActions.clear();
		}

		sendOrderedBroadcast(
			new Intent(PluginApi.ACTION_REGISTER),
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
		myIsPaused = false;
		if (myDialogToShow != null) {
			myDialogToShow.show();
			myDialogToShow = null;
		}

		SetScreenOrientationAction.setOrientation(this, ZLibrary.Instance().getOrientationOption().getValue());
		Log.d("fbreader", "onresume");
		if (myCancelCalled) {
			myCancelCalled = false;
			if (myCancelIntent != null) {
				final Intent ci = myCancelIntent;
				myCancelIntent = null;
				getCollection().bindToService(this, new Runnable() {
					public void run() {
						runCancelAction(ci);
					}
				});
			} else {
				finish();
			}
			return;
		} else {
			if (myFBReaderApp.Model != null && myFBReaderApp.Model.Book != null) {
				final FormatPlugin p = PluginCollection.Instance().getPlugin(myFBReaderApp.Model.Book.File);
				Log.d("fbj", "onresume: current book is: " + myFBReaderApp.Model.Book.File.getPath());
				if (p.type() == FormatPlugin.Type.PLUGIN) {
					if (!myNeedToSkipPlugin) {
						Log.d("fbj", "opening book from onresume");
						getCollection().bindToService(this, new Runnable() {
							public void run() {
								myFBReaderApp.openBook(myFBReaderApp.Model.Book, null, null);
							}
						});
					} else {
						Log.d("fbj", "skipping");
					}
				}
			}
			myNeedToSkipPlugin = false;
		}

		if (myNeedToOpenFile) {
			Log.d("fbj", "needtoopen");
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					openBook(myIntentToOpen, null, true);
					myIntentToOpen = null;
				}
			});
			myNeedToOpenFile = false;
		}
		PopupPanel.restoreVisibilities(myFBReaderApp);
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
	}

	@Override
	protected void onPause() {
		myIsPaused = true;
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
		hideBars();
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
		hideBars();
		myFBReaderApp.showPopup(SelectionPopup.ID);
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
	protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_PREFERENCES:
				if (resultCode != RESULT_DO_NOTHING && data != null) {
					final Book book = FBReaderIntents.getBookExtra(data);
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
				if (resultCode != RESULT_CANCELED && resultCode != -1) {
					myNeedToSkipPlugin = true;
				}
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

	private NavigationPopup myNavigationPopup;

	boolean barsAreShown() {
		return myNavigationPopup != null;
	}

	void hideBars() {
		if (myNavigationPopup != null) {
			myNavigationPopup.stopNavigation();
			myNavigationPopup = null;
		}
	}

	void showBars() {
		final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);

		if (myNavigationPopup == null) {
			myFBReaderApp.hideActivePopup();
			myNavigationPopup = new NavigationPopup(myFBReaderApp);
			myNavigationPopup.runNavigation(this, root);
		}
	}

	private Menu addSubmenu(Menu menu, String id) {
		return menu.addSubMenu(ZLResource.resource("menu").getResource(id).getValue());
	}

	private void addMenuItem(Menu menu, String actionId, Integer iconId, String name) {
		if (name == null) {
			name = ZLResource.resource("menu").getResource(actionId).getValue();
		}
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
		}
		menuItem.setOnMenuItemClickListener(myMenuListener);
		myMenuItemMap.put(menuItem, actionId);
	}

	private void addMenuItem(Menu menu, String actionId, String name) {
		addMenuItem(menu, actionId, null, name);
	}

	private void addMenuItem(Menu menu, String actionId, int iconId) {
		addMenuItem(menu, actionId, iconId, null);
	}

	private void addMenuItem(Menu menu, String actionId) {
		addMenuItem(menu, actionId, null, null);
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
		final String menuLanguage = ZLResource.getLanguageOption().getValue();
		if (menuLanguage.equals(myMenuLanguage)) {
			return;
		}
		myMenuLanguage = menuLanguage;

		menu.clear();
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

		setupMenu(menu);

		return true;
	}

	protected void onPluginAbsent() {
		myFBReaderApp.Model = null;
		getCollection().bindToService(this, new Runnable() {
			public void run() {
				myFBReaderApp.openBook(myFBReaderApp.Collection.getRecentBook(0), null, null);
			}
		});
	}

	private void setStatusBarVisibility(boolean visible) {
		final ZLAndroidLibrary zlibrary = getZLibrary();
		if (DeviceType.Instance() != DeviceType.KINDLE_FIRE_1ST_GENERATION && !myShowStatusBarFlag) {
			if (visible) {
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			} else {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
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
		try {
			final WindowManager.LayoutParams attrs = getWindow().getAttributes();
			final Class<?> cls = attrs.getClass();
			final Field fld = cls.getField("buttonBrightness");
			if (fld != null && "float".equals(fld.getType().toString())) {
				fld.setFloat(attrs, enabled ? -1.0f : 0.0f);
				getWindow().setAttributes(attrs);
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
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
}
