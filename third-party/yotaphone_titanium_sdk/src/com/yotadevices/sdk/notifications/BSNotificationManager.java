/***********************************************************************************
 *
 *  Copyright 2012-2013 Yota Devices LLC, Russia
 *
 *  This source code is Yota Devices Confidential Proprietary
 *  This software is protected by copyright. All rights and titles are reserved.
 *  You shall not use, copy, distribute, modify, decompile, disassemble or reverse
 *  engineer the software. Otherwise this violation would be treated by law and
 *  would be subject to legal prosecution. Legal use of the software provides
 *  receipt of a license from the right holder only.
 *
 ************************************************************************************/

package com.yotadevices.sdk.notifications;

import com.yotadevices.sdk.helper.HelperConstant;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

/**
 * @hide
 * Notifications SDK
 * 
 * @author Arseniy Nikolaev
 */
public class BSNotificationManager {

    public interface OnNotificationVisibleListener {
        public void onNotificationVisible(int id, boolean isVisible);
    }

    private final static String TAG = "BSNotificationManager";

    Context mContext;

    public BSNotificationManager(Context context) {
        mContext = context;
        mConnection = new NotifyServiceConnection(context);
    }

    NotifyServiceConnection mConnection;

    private final class NotifyServiceConnection implements ServiceConnection {

        Context ctx;
        BSNotification notification;
        int mId;
        IBSNotification mBSNotificationService;

        NotifyServiceConnection(Context ctx) {
            this.ctx = ctx;
        }

        void setReceivedNotification(BSNotification n) {
            notification = n;
        }

        void setNotificationId(int id) {
            mId = id;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBSNotificationService = IBSNotification.Stub.asInterface(service);
            try {
                // add package name
                notification.packageName = ctx.getPackageName();
                notification.when = System.currentTimeMillis();
                mBSNotificationService.drawNotification(mId, notification);
                notification = null;
            } catch (Throwable unused) {
                Log.d(TAG, "don't send BSNotification");
            } finally {
                unbind();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBSNotificationService = null;
        }

        private void unbind() {
            try {
                mContext.unbindService(this);
            } catch (Exception unused) {
            }
        }

    }

    private void sendNotification(int id, BSNotification n) {
        mConnection.setReceivedNotification(n);
        mConnection.setNotificationId(id);

        boolean b = mContext.bindService(getFrameforkIntent(), mConnection, Context.BIND_AUTO_CREATE);
        if (!b) {
            Log.d(TAG, "can't to bind yotaphone service");
        }

    }

    private Intent getFrameforkIntent() {
        return new Intent(HelperConstant.FRAMEWORK_SERVICE_ACTION);
    }

    /**
     * Cancel a previously shown notification.
     * 
     * @param id
     *            notification ID
     */
    public void cancel(int id) {
        return;
    }

    /**
     * Cancel all previously shown notifications.
     */
    public void cancelAll() {
        return;
    }

    /**
     * Post a notification to be shown in the back screen.
     * 
     * @param id
     *            notification ID
     * @param notification
     *            BSNotification to be shown
     */
    public void notify(int id, BSNotification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("BSNotification can't be null");
        }

        BSNotification mNotificationToSend = notification;
        if (mNotificationToSend.icon != 0) {
            mNotificationToSend.iconBitmap = BitmapFactory.decodeResource(mContext.getResources(), notification.icon);
        }

        sendNotification(id, notification);
    }

    /**
     * Use this to deermine if notification is corrently displayed on back
     * screen
     * 
     * @param id
     *            notification ID
     * @param listener
     *            the listener which will receve the callback
     */
    public void isNotificationVisible(final int id, final OnNotificationVisibleListener listener) {
        mContext.bindService(getFrameforkIntent(), new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName arg0) {

            }

            @Override
            public void onServiceConnected(ComponentName cn, IBinder service) {
                IBSNotification mService = IBSNotification.Stub.asInterface(service);
                try {
                    boolean isVisible = mService.isNotificationVisible(id);
                    if (listener != null) {
                        listener.onNotificationVisible(id, isVisible);
                    }
                } catch (Throwable unused) {

                } finally {
                    unbind();
                }
            }

            private void unbind() {
                try {
                    mContext.unbindService(this);
                } catch (Exception unused) {
                }
            }

        }, Context.BIND_AUTO_CREATE);
    }
}
