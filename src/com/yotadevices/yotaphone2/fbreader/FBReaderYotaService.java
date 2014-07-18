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
import android.util.Log;
import android.view.View;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.sdk.Constants;
import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.actions.ToggleBarsAction;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.TurnPageAction;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;

import java.util.HashMap;

/**
 * @author ASazonov
 */
public class FBReaderYotaService extends BSActivity implements ZLApplicationWindow {
    public static final String KEY_BACK_SCREEN_IS_ACTIVE =
            "com.yotadevices.yotaphone2.fbreader.backScreenIsActive";

    public static YotaBackScreenWidget mWidget;
    private Book myCurrentBook;
    private FBReaderApp myFBReaderApp;
    private View mRootView;
    private int mBatteryLevel;

    private BSReadingActionBar mActionBar;
    private BSReadingStatusBar mStatusBar;

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
    }

    @Override
    public void onBSResume() {
        super.onBSResume();

        registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        setBSContentView(mRootView);
        hideActionBar();
        hideStatusBar();
        EinkUtils.setViewDithering(mRootView, Drawer.Dithering.DITHER_FLOYD_STEINBERG_BINARY);

        getCollection().bindToService(this, new Runnable() {
            public void run() {
                myCurrentBook = myFBReaderApp.Collection.getRecentBook(0);
                myFBReaderApp.openBook(myCurrentBook, null, new Runnable() {
                    public void run() {
                        myFBReaderApp.initWindow();
                        initBookView(true);
                    }
                });
                AndroidFontUtil.clearFontCache();
            }
        });
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

        mWidget.setBook(myCurrentBook);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(KEY_BACK_SCREEN_IS_ACTIVE)) {
            boolean isActive = intent.getBooleanExtra(KEY_BACK_SCREEN_IS_ACTIVE, false);
            if (isActive) {
                myFBReaderApp.addAction(ActionCode.TOGGLE_BARS, new ToggleBarsAction(this, myFBReaderApp));
                myFBReaderApp.addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(myFBReaderApp, true));
                myFBReaderApp.addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(myFBReaderApp, false));
            }
            mWidget.setIsBsActive(isActive);
            mWidget.repaint();
        } else {
            //myBackScreenIsActive = new ViewOptions().YotaDrawOnBackScreen.getValue();
        }
    }

    private BookCollectionShadow getCollection() {
        return (BookCollectionShadow) myFBReaderApp.Collection;
    }

    @Override
    public void setWindowTitle(String title) {

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
                            uiPostAction.run();
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
            mActionBar = new BSReadingActionBar(getBSDrawer().getBSContext(), mRootView, myFBReaderApp, mOnFontChangedListener);
        }
        mActionBar.show();
    }

    public void showStatusBar() {
        //setSystemBSUiVisibility(Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_VISIBLE);
        if (mStatusBar == null) {
            mStatusBar = new BSReadingStatusBar(getBSDrawer().getBSContext(), mRootView, myFBReaderApp);
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
        //setSystemBSUiVisibility(Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION | Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_STATUS_BAR);
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
            hideActionBar();
            hideStatusBar();
        }
    };
}
