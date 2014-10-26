/***********************************************************************************
 *
 *  Copyright 2012 Yota Devices LLC, Russia
 *
 ************************************************************************************/

package com.yotadevices.yotaphone2.fbreader;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.sdk.Constants;
import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.BitmapUtils;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.actions.ProcessHyperlinkBSAction;
import com.yotadevices.yotaphone2.fbreader.actions.ToggleBarsAction;

import org.geometerplus.android.fbreader.SelectionBookmarkAction;
import org.geometerplus.android.fbreader.SelectionCopyAction;
import org.geometerplus.android.fbreader.YotaBSSelectionBookmarkAction;
import org.geometerplus.android.fbreader.YotaBSSelectionCopyAction;
import org.geometerplus.android.fbreader.YotaDefineBSPopup;
import org.geometerplus.android.fbreader.YotaSelectionBSPopup;
import org.geometerplus.android.fbreader.YotaSelectionDefineAction;
import org.geometerplus.android.fbreader.YotaSelectionHidePanelAction;
import org.geometerplus.android.fbreader.YotaSelectionPopup;
import org.geometerplus.android.fbreader.YotaSelectionShareAction;
import org.geometerplus.android.fbreader.YotaSelectionShowPanelAction;
import org.geometerplus.android.fbreader.YotaSelectionTranslateAction;
import org.geometerplus.android.fbreader.YotaTranslateBSPopup;
import org.geometerplus.android.fbreader.YotaTranslatePopup;
import org.geometerplus.android.fbreader.YotaUpdateBackScreen;
import org.geometerplus.android.fbreader.YotaUpdateWidgetAction;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.TurnPageAction;
import org.geometerplus.fbreader.fbreader.VolumeKeyTurnPageAction;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author ASazonov
 */
public class FBReaderYotaService extends BSActivity implements ZLApplicationWindow {
    public static final String KEY_BACK_SCREEN_IS_ACTIVE =
            "com.yotadevices.yotaphone2.fbreader.backScreenIsActive";

    public final static String TAG = FBReaderYotaService.class.getSimpleName();

    public static YotaBackScreenWidget mWidget;
    private Book myCurrentBook;
    private FBReaderApp myFBReaderApp;
    private View mRootView;
    private int mBatteryLevel;

    private BSReadingActionBar mActionBar;
    private BSReadingStatusBar mStatusBar;
	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onBSCreate() {
        super.onBSCreate();
        myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
        if (myFBReaderApp == null) {
            myFBReaderApp = new FBReaderApp(new BookCollectionShadow());
        }
        myFBReaderApp.setWindow(this);
        myFBReaderApp.ViewOptions.YotaDrawOnBackScreen.setValue(true);
        mRootView = getBSDrawer().getBSLayoutInflater().inflate(R.layout.bs_main, null);
        mWidget = (YotaBackScreenWidget) mRootView.findViewById(R.id.bs_main_widget);
        mWidget.setIsBsActive(true);

        if (myFBReaderApp.getPopupById(YotaSelectionBSPopup.ID) == null) {
            new YotaSelectionBSPopup(myFBReaderApp, getBsContext());
        }
        if (myFBReaderApp.getPopupById(YotaTranslateBSPopup.ID) == null) {
            new YotaTranslateBSPopup(myFBReaderApp, getBsContext(), getApplicationContext().getContentResolver());
        }
        if (myFBReaderApp.getPopupById(YotaDefineBSPopup.ID) == null) {
            new YotaDefineBSPopup(myFBReaderApp, getBsContext(), getApplicationContext().getContentResolver());
        }
        ((YotaSelectionBSPopup)myFBReaderApp.getPopupById(YotaSelectionBSPopup.ID)).setRootView(mRootView);
        ((YotaTranslateBSPopup)myFBReaderApp.getPopupById(YotaTranslateBSPopup.ID)).setRootView(mRootView);
        ((YotaDefineBSPopup)myFBReaderApp.getPopupById(YotaDefineBSPopup.ID)).setRootView(mRootView);

        registerActions();
    }

    @Override
    public void onBSResume() {
        super.onBSResume();
        registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        setBSContentView(mRootView);

        EinkUtils.setViewDithering(mRootView, Drawer.Dithering.DITHER_ATKINSON_BINARY);
        EinkUtils.setViewWaveform(mRootView, Drawer.Waveform.WAVEFORM_A2);
        getCollection().bindToService(this, new Runnable() {
            public void run() {
                if (myCurrentBook == null) {
                    myCurrentBook = myFBReaderApp.Collection.getRecentBook(0);
                }
                if (mWidget != null) {
	                ZLAndroidPaintContext.AntiAliasOption.setValue(true);
                    myFBReaderApp.openBook(myCurrentBook, null, new Runnable() {
	                    public void run() {
		                    myFBReaderApp.initWindow();
		                    initBookView(true);
		                    updateCoverOnYotaWidget(myFBReaderApp.Model.Book);
		                    if (firstStart()) {
			                    showActionBar();
			                    showStatusBar();
		                    } else {
			                    hideStatusBar();
		                    }
	                    }
                    }, null);
                    AndroidFontUtil.clearFontCache();
	                if (myFBReaderApp.Model != null && myFBReaderApp.Model.Book != null) {
		                ZLTextHyphenator.Instance().load(myFBReaderApp.Model.Book.getLanguage());
		                myFBReaderApp.clearTextCaches();
		                if (getViewWidget() != null) {
			                getViewWidget().repaint();
		                }
	                }
	                if (myFBReaderApp.getTextView() != null) {
		                myFBReaderApp.getTextView().clearSelection();
		                myFBReaderApp.hideActivePopup();
	                }
                }
	            if (!firstStart()) {
		            showActionBar();
		            showStatusBar();
		            mHandler.postDelayed(new Runnable() {
			            @Override
			            public void run() {
				            hideActionBar();
				            hideStatusBar();
			            }
		            }, 1500);
	            }
	            setNotFirstStart();
            }
        });
    }

    @Override
    protected void onBSPause() {
        super.onBSPause();
    }

    @Override
    public void onBSDestroy() {
        try {
            unregisterReceiver(myBatteryInfoReceiver);
        } catch (IllegalArgumentException e) {
        }

        mWidget = null;
        getCollection().unbind();
        super.onBSDestroy();
    }


    private void initBookView(final boolean refresh) {
        Log.d(TAG, "--- init book view:" + refresh);
        if (mWidget != null) {
            mWidget.setBook(myCurrentBook);
	        mWidget.postInvalidate();
        }
    }

    protected void registerActions() {
        myFBReaderApp.setBackScreenActionMap();
        myFBReaderApp.addAction(ActionCode.TOGGLE_BARS, new ToggleBarsAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(myFBReaderApp, true));
        myFBReaderApp.addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(myFBReaderApp, false));
        myFBReaderApp.addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyTurnPageAction(myFBReaderApp, true));
        myFBReaderApp.addAction(ActionCode.VOLUME_KEY_SCROLL_BACK, new VolumeKeyTurnPageAction(myFBReaderApp, false));
        myFBReaderApp.addAction(ActionCode.YOTA_UPDATE_WIDGET, new YotaUpdateWidgetAction(getBsContext(), myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new YotaSelectionShowPanelAction(getBsContext(), myFBReaderApp, YotaSelectionBSPopup.ID));
        myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new YotaSelectionHidePanelAction(getBsContext(), myFBReaderApp, YotaSelectionBSPopup.ID));
        myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new YotaBSSelectionBookmarkAction(getBsContext(), myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new YotaBSSelectionCopyAction(getBsContext(), myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new YotaSelectionTranslateAction(getBsContext(), myFBReaderApp, true));
        myFBReaderApp.addAction(ActionCode.SELECTION_DEFINE, new YotaSelectionDefineAction(getBsContext(), myFBReaderApp, true));
	    myFBReaderApp.addAction(ActionCode.SELECTION_SHARE, new YotaSelectionShareAction(getBsContext(), myFBReaderApp));
	    myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkBSAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.YOTA_PERFORM_FULL_UPDATE, new YotaUpdateBackScreen(this, myFBReaderApp));
    }

    private Context getBsContext() {
        return getBSDrawer().getBSContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Book intentBook = FBReaderIntents.getBookExtra(intent);
        if (intentBook != null) {
            myCurrentBook = intentBook;
        }
        if (intent.hasExtra(KEY_BACK_SCREEN_IS_ACTIVE)) {
            boolean isActive = intent.getBooleanExtra(KEY_BACK_SCREEN_IS_ACTIVE, false);
            if (isActive) {
	            ZLAndroidPaintContext.AntiAliasOption.setValue(true);
	            registerActions();
            }
            mWidget.setIsBsActive(isActive);
            mWidget.repaint();
        } else {
            //myBackScreenIsActive = new ViewOptions().YotaDrawOnBackScreen.getValue();
        }
    }

	public void performSingleFullUpdate() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				EinkUtils.performSingleUpdate(mRootView, Drawer.Waveform.WAVEFORM_GC_FULL);
			}
		});
	}

    private BookCollectionShadow getCollection() {
        return (BookCollectionShadow) myFBReaderApp.Collection;
    }

    @Override
    public void setWindowTitle(String title) {

    }

	@Override
	public void setWindowTitle(String title, String author) {

	}

    @Override
    public void showErrorMessage(String resourceKey) {

    }

    @Override
    public void showErrorMessage(String resourceKey, String parameter) {

    }

    @Override
    public ZLApplication.SynchronousExecutor createExecutor(final String key) {
        return new ZLApplication.SynchronousExecutor() {
            private final ZLResource myResource =
                    ZLResource.resource("dialog").getResource("waitMessage");
            private final String myMessage = myResource.getResource(key).getValue();

            public void execute(final Runnable action, final Runnable uiPostAction) {
                final Thread runner = new Thread() {
                    public void run() {
                        action.run();
                        if (uiPostAction != null) {
                            mHandler.post(uiPostAction);
                        }
                    }
                };
                runner.setPriority(Thread.MAX_PRIORITY);
                runner.start();
            }

            private void setMessage(final ProgressDialog progress, final String message) {
                if (progress == null) {
                    return;
                }
//                activity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        progress.setMessage(message);
//                    }
//                });
            }

            public void executeAux(String key, Runnable runnable) {
                runnable.run();
            }
        };
    }

    @Override
    public void processException(Exception e) {

    }

    @Override
    public void refresh() {
	    if (mStatusBar != null) {
		    mStatusBar.updateData();
	    }
    }

    @Override
    public ZLViewWidget getViewWidget() {
        return mWidget;
    }

    @Override
    public void close() {

    }

    @Override
    public int getBatteryLevel() {
        return mBatteryLevel;
    }

    private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final int level = intent.getIntExtra("level", 100);
            setBatteryLevel(level);
        }
    };

    private void setBatteryLevel(int level) {
        mBatteryLevel = level;
    }

    public void showActionBar() {
        if (mActionBar == null) {
            mActionBar = new BSReadingActionBar(getBsContext(), mRootView, myFBReaderApp, mOnFontChangedListener);
        }
        mActionBar.show();
    }

    public void showStatusBar() {
        setSystemBSUiVisibility(Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_VISIBLE);
        if (mStatusBar == null) {
            mStatusBar = new BSReadingStatusBar(getBsContext(), mRootView, myFBReaderApp);
        }
        mStatusBar.show();
    }

    public void hideActionBar() {
        if (mActionBar != null) {
            mActionBar.hide();
        }
    }

    public void hideStatusBar() {
        if (mStatusBar != null) {
            mStatusBar.hide();
        }
        setSystemBSUiVisibility(Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION | Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_STATUS_BAR);
    }

    public BSReadingActionBar geActionBar() {
        return mActionBar;
    }

    public BSReadingStatusBar getStatusBar() {
        return mStatusBar;
    }

    private FontSettingsPopup.OnFontChangeListener mOnFontChangedListener = new FontSettingsPopup.OnFontChangeListener() {
        @Override
        public void fontChanged() {
            //hideActionBar();
            //hideStatusBar();
            //EinkUtils.performSingleUpdate(mRootView, Drawer.Waveform.WAVEFORM_GC_FULL);
        }
    };

    protected void onVolumeButtonsEvent(Constants.VolumeButtonsEvent event) {
        switch (event) {
            case VOLUME_MINUS_DOWN:
                myFBReaderApp.runAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD);
                break;
            case VOLUME_PLUS_DOWN:
                myFBReaderApp.runAction(ActionCode.VOLUME_KEY_SCROLL_BACK);
                break;
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
			final DisplayMetrics metrics = getBsContext().getResources().getDisplayMetrics();

			final int maxHeight = metrics.heightPixels * 2/3;
			final int maxWidth = metrics.widthPixels * 2/3;
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
					} catch (IOException e) {

					}
				}
			}
			this.sendBroadcast(i);
		} else {
			Intent i = new Intent(Consts.YOTA_WIDGET_SET_COVER_ACTION);
			this.sendBroadcast(i);
		}
	}

	private boolean firstStart() {
		final SharedPreferences prefs = getSharedPreferences(Consts.YOTA_READER_BS_SETTINGS, MODE_PRIVATE);
		final boolean firstStart = prefs.getBoolean(Consts.YOTA_READER_FIRST_START, true);
		return firstStart;
	}

	private void setNotFirstStart() {
		final SharedPreferences prefs = getSharedPreferences(Consts.YOTA_READER_BS_SETTINGS, MODE_PRIVATE);
		final SharedPreferences.Editor ed = prefs.edit();
		ed.putBoolean(Consts.YOTA_READER_FIRST_START, false);
		ed.commit();
	}
}
