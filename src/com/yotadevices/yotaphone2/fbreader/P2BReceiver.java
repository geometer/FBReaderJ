package com.yotadevices.yotaphone2.fbreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.yotadevices.sdk.helper.HelperConstant;
import com.yotadevices.sdk.helper.IFrameworkService;
import com.yotadevices.sdk.utils.FrameworkUtils;
import com.yotadevices.sdk.utils.RotationAlgorithm;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class P2BReceiver extends BroadcastReceiver {
    public P2BReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("yotaphone.intent.action.p2b".equalsIgnoreCase(intent.getAction())) {
            final FBReaderApp app = (FBReaderApp)FBReaderApp.Instance();
            if (app != null) {
	            RotationAlgorithm.getInstance(context.getApplicationContext()).issueStandardToastAndVibration();
	            RotationAlgorithm.getInstance(context.getApplicationContext()).turnScreenOffIfRotated(0, new RotationAlgorithm.OnPhoneRotatedListener() {
				            @Override
				            public void onPhoneRotatedToFS() {

				            }

				            @Override
				            public void onPhoneRotatedToBS() {
					            app.runAction(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN);
				            }

				            @Override
				            public void onRotataionCancelled() {

				            }
			            });
            }
        }
        if ("yotaphone.intent.action.IS_BS_SUPPORTED".equalsIgnoreCase(intent.getAction())) {
			/*IBinder binder = peekService(context, new Intent(HelperConstant.FRAMEWORK_SERVICE_ACTION));
	        boolean isMirroringOn = false;
	        if (binder != null) {
		        try {
			        isMirroringOn = IFrameworkService.Stub.asInterface(binder).isMirroringOn();
		        }
		        catch (RemoteException e) {
			        Log.e("YD_FBReader", "Can not connect to service", e);
		        }
	        }
	        else {
		        Log.d("YD_FBReader", "P2BReceiver p2b action binder is null");
	        }*/
	        boolean isMirroringOn = Boolean.valueOf(getSystemProperty("debug.bsmanager.mirroring", "false"));
	        Log.d("YD_FBReader", "P2BReceiver p2b action mirroring is on " + isMirroringOn);
	        Bundle b = new Bundle();
	        b.putInt("support_bs", !isMirroringOn ? 1 : 0); //p2b works only if mirroring not enabled
	        setResultExtras(b);
        }

    }

	private String getSystemProperty(String prop, String def) {
		Class clazz = null;
		try {
			clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getDeclaredMethod("get", String.class);
			String value = (String) method.invoke(null, prop);
			if (value == null) value = def;
			return value;
		}
		catch (Exception e) {
			return def;
		}
	}
}