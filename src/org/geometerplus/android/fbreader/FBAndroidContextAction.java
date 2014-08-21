package org.geometerplus.android.fbreader;

import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

abstract class FBAndroidContextAction extends FBAction{
    protected final Context mContext;

    public FBAndroidContextAction(Context ctx, FBReaderApp fbreader) {
        super(fbreader);
        mContext = ctx;
    }
}
