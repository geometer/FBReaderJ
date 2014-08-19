package com.yotadevices.yotaphone2.fbreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class P2BReceiver extends BroadcastReceiver {
    public P2BReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("yotaphone.intent.action.p2b".equalsIgnoreCase(intent.getAction())) {
            FBReaderApp app = (FBReaderApp)FBReaderApp.Instance();
            if (app != null) {
                app.runAction(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN);
            }
        }
        if ("yotaphone.intent.action.IS_BS_SUPPORTED".equalsIgnoreCase(intent.getAction())) {
            Log.d("YD_FBReader", "P2BReceiver p2b action is supported");
            Bundle b = new Bundle();
            b.putInt("support_bs", 1);
            setResultExtras(b);
        }
    }
}