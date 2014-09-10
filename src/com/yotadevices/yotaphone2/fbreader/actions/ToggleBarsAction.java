package com.yotadevices.yotaphone2.fbreader.actions;

import android.util.Log;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.yotaphone2.fbreader.BSReadingActionBar;
import com.yotadevices.yotaphone2.fbreader.BSReadingStatusBar;
import com.yotadevices.yotaphone2.fbreader.FBBSAction;
import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ToggleBarsAction extends FBBSAction {
    public ToggleBarsAction(FBReaderYotaService bsActivity, FBReaderApp app) {
        super(bsActivity, app);
    }
    @Override
    protected void run(Object... params) {
        BSReadingStatusBar statusBar = mBSActivity.getStatusBar();
        BSReadingActionBar actionBar = mBSActivity.geActionBar();
        boolean showing = statusBar != null && actionBar != null && actionBar.isShowing() && statusBar.isShowing();
        if (!showing) {
            mBSActivity.showActionBar();
            mBSActivity.showStatusBar();
        }
        else {
            mBSActivity.hideActionBar();
            mBSActivity.hideStatusBar();
        }
    }
}
