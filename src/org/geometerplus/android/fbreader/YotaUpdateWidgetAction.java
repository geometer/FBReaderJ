package org.geometerplus.android.fbreader;

import android.content.Context;
import android.content.Intent;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaUpdateWidgetAction extends FBAndroidContextAction {
    private final static String YOTA_WIDGET_UPDATE_ACTION = "com.yotadevices.yotaphone2.yotareader.update_widget";

    public YotaUpdateWidgetAction(Context baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object... params) {
        Intent i = new Intent(YOTA_WIDGET_UPDATE_ACTION);
        mContext.sendBroadcast(i);
    }
}
