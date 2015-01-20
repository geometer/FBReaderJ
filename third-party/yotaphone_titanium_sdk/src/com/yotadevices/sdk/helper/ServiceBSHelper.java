package com.yotadevices.sdk.helper;

/**
 * Copyright 2012 Yota Devices LLC, Russia
 * 
 * This source code is Yota Devices Confidential Proprietary
 * This software is protected by copyright.  All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or
 * reverse engineer the software. Otherwise this violation would be treated by 
 * law and would be subject to legal prosecution.  Legal use of the software 
 * provides receipt of a license from the right holder only.
 * 
 * */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * @hide
 */
public class ServiceBSHelper {

    public interface OnSuccesBinding {
        void onBind(IFrameworkService service);

        void onError();
    }

    private final static String TAG = "BSLockManager";

    private Context mContext;

    FrameforkServiceConnection mConnection;
    IFrameworkService mFrameforkService;

    private final class FrameforkServiceConnection implements ServiceConnection {

        OnSuccesBinding listener;

        void setListener(OnSuccesBinding l) {
            listener = l;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mFrameforkService = IFrameworkService.Stub.asInterface(service);
            if (listener != null) {
                listener.onBind(mFrameforkService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mFrameforkService = null;
        }

    }

    public ServiceBSHelper(Context ctx) {
        mContext = ctx;
        mConnection = new FrameforkServiceConnection();
    }

    public void getAsyncService(OnSuccesBinding listener) {
        if (mFrameforkService != null) {
            listener.onBind(mFrameforkService);
        } else {
            bind(listener);
        }
    }

    public void unbind() {
        try {
            mContext.unbindService(mConnection);
        } catch (Exception unused) {
        }
    }

    private void bind(OnSuccesBinding listener) {
        Intent intent = new Intent(HelperConstant.FRAMEWORK_SERVICE_ACTION);

        mConnection.setListener(listener);
        boolean b = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (!b) {
            Log.d(TAG, "can't to bind yotaphone service");
            if (listener != null) {
                listener.onError();
            }
        }
    }

}
