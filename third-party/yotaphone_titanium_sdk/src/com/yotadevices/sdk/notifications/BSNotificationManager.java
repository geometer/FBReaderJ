package com.yotadevices.sdk.notifications;

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

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.lang.reflect.Method;

/**
 * 
 * BSNotificationManager - This class manages back screen notifications: <BR>
 * - can post notification on the Back Screen <BR>
 * - can detect is notification visible or not;
 * 
 * <BR>
 * <BR>
 * Refer to <a href=
 * "http://developer.yotaphone.com/docs/getting-started/building-apps-bs-notifications/"
 * >Building Apps with BS Notifications</a> for more details.
 * 
 */
public class BSNotificationManager {

    /**
     * interface OnNotificationVisibleListener - This interface is used in
     * {@link BSNotificationManager#isNotificationVisible} to determine if
     * notification is visible or not
     */
    public interface OnNotificationVisibleListener {
        public void onNotificationVisible(int id, boolean isVisible);
    }

    private final static String TAG = "BSNotificationManager";

    /**
     * @hide
     */
    public final static String SERVICE_NAME = "com.yotadevices.yotaphone2.titanium.notifications";

    /**
     * @hide
     */
    public final static String EXTRA_NOTIFICATION = "extra_bs_notification";
    /**
     * @hide
     */
    public final static String EXTRA_NOTIFICATION_ID = "extra_bs_notification_id";

    /**
     * @hide
     */
    public final static String EXTRA_NOTIFICATION_PACKAGE = "extra_bs_notification_package";

    /**
     * @hide
     */
    public final static int WHAT_NOTIFICATION_NOTIFY = 1;
    /**
     * @hide
     */
    public final static int WHAT_NOTIFICATION_CANCEL = 2;

    Context mContext;
    Messenger mMessenger;

    public BSNotificationManager(Context context) {
        mContext = context;
        IBinder binder = getNotificationBinder();
        if (binder != null) {
            mMessenger = new Messenger(binder);
        }
    }

    private static IBinder getNotificationBinder() {
        try {
            Class clazz = Class.forName("android.os.ServiceManager");
            Method method = clazz.getMethod("checkService", String.class);
            return (IBinder) method.invoke(null, SERVICE_NAME);
        } catch (Exception unused) {
            return null;
        }
    }

    private void sendNotification(int id, BSNotification n) {
        sendCommand(WHAT_NOTIFICATION_NOTIFY, n, id);
    }

    private void cancelNotification(int id) {
        sendCommand(WHAT_NOTIFICATION_CANCEL, null, id);
    }

    private void sendCommand(int what, BSNotification n, int id) {
        if (mMessenger != null) {
            Message m = Message.obtain(null, what, Binder.getCallingPid(), 0);
            Bundle data = new Bundle();
            data.putInt(EXTRA_NOTIFICATION_ID, id);
            data.putString(EXTRA_NOTIFICATION_PACKAGE, mContext.getPackageName());
            if (what == WHAT_NOTIFICATION_NOTIFY) {
                Bundle b = new Bundle();
                b.putParcelable("contentIntent", n.contentIntent);
                b.putString("contentTitle", n.contentTitle);
                b.putString("contentText", n.contentText);
                b.putParcelable("fullScreenIntent", n.fullScreenIntent);
                b.putParcelable("largeIcon", n.largeIcon);
                b.putInt("smallIcon", n.smallIcon);
                b.putInt("notificationCategory", n.notificationCategory);
                b.putInt("notificationType", n.notificationType);
                b.putLong("transientTime", n.transientTime);
                data.putBundle(EXTRA_NOTIFICATION, b);
                // data.putParcelable(EXTRA_NOTIFICATION, n);
            }
            m.setData(data);
            try {
                mMessenger.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * cancel - Cancel a previously shown notification.
     * 
     * @param id
     *            notification ID
     * 
     */
    public void cancel(int id) {
        cancelNotification(id);
    }

    /**
     * @hide cancelAll - Cancel all previously shown notifications.
     */
    public void cancelAll() {
        return;
    }

    /**
     * notify - Post a notification to be shown in the back screen.
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
        sendNotification(id, notification);
    }

}
