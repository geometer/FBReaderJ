/***********************************************************************************
 *
 *  Copyright 2012 Yota Devices LLC, Russia
 *
 *  This source code is Yota Devices Confidential Proprietary
 *  This software is protected by copyright.  All rights and titles are reserved.
 *  You shall not use, copy, distribute, modify, decompile, disassemble or reverse
 *  engineer the software. Otherwise this violation would be treated by law and
 *  would be subject to legal prosecution.  Legal use of the software provides
 *  receipt of a license from the right holder only.
 *
 ************************************************************************************/

package com.yotadevices.sdk.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * @hide
 */
public abstract class InfiniteIntentService extends Service {
    protected volatile ServiceHandler mServiceHandler;

    private volatile Looper mServiceLooper;

    private String mName;

    private boolean mRedelivery;

    protected final class ServiceHandler extends Handler {
        public ServiceHandler() {
            super();
        }

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (canHandleIntent()) {
                onHandleIntent((Intent) msg.obj);
            }
        }
    }

    public InfiniteIntentService(String name) {
        super();
        mName = name;
    }

    /**
     * Control redelivery of intents. If called with true,
     * {@link #onStartCommand(Intent, int, int)} will return
     * {@link Service#START_REDELIVER_INTENT} instead of
     * {@link Service#START_NOT_STICKY}, so that if this service's process is
     * called while it is executing the Intent in
     * {@link #onHandleIntent(Intent)}, then when later restarted the same
     * Intent will be re-delivered to it, to retry its execution.
     */
    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        // mServiceHandler = new ServiceHandler(mServiceLooper);
        mServiceHandler = new ServiceHandler();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean canHandleIntent() {
        return true;
    }

    /**
     * Invoked on the Handler thread with the {@link Intent} that is passed to
     * {@link #onStart}. Note that this will be invoked from a different thread
     * than the one that handles the {@link #onStart} call.
     */
    protected abstract void onHandleIntent(Intent intent);
}
