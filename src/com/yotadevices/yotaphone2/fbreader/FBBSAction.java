package com.yotadevices.yotaphone2.fbreader;

import com.yotadevices.sdk.BSActivity;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public abstract class FBBSAction extends FBAction {
    private BSActivity mBSActivity;

    public FBBSAction(BSActivity bsActivity, FBReaderApp fbreader) {
        super(fbreader);
        mBSActivity = bsActivity;
    }
}
