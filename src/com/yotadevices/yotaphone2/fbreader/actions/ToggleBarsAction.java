package com.yotadevices.yotaphone2.fbreader.actions;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.yotaphone2.fbreader.FBBSAction;
import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ToggleBarsAction extends FBBSAction {
    private boolean mShowing = false;
    public ToggleBarsAction(FBReaderYotaService bsActivity, FBReaderApp app) {
        super(bsActivity, app);
    }
    @Override
    protected void run(Object... params) {
        if (!mShowing) {
            mBSActivity.showActionBar();
            mBSActivity.showStatusBar();
        }
        else {
            mBSActivity.hideActionBar();
            mBSActivity.hideStatusBar();
        }
        mShowing = !mShowing;
    }
}
