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

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RemoteViews;

/**
 * @hide
 * Notifications SDK
 * 
 * @author Arseniy Nikolaev
 */
public class BSNotification implements Parcelable {


    /**
     * Bit to be bitwise-ored into the flags field that should be set if you
     * want the sound and/or vibration play each time the notification is sent,
     * even if it has not been canceled before that
     */
    public static final int FLAG_ONLY_ALERT_ONCE = 8;


    /**
     * Bit to be bitwise-ored into the flags field that should be set if this notification is in reference to something that is ongoing, like a phone call.
     */
    public static final int FLAG_ONGOING_EVENT = 2;


    /**
     * Bit to be bitwise-ored into the flags field that should be set if the notification should not be canceled when the user clicks the Clear all button
     */
    public static final int FLAG_NO_CLEAR = 32;


    /**
     * Bit to be bitwise-ored into the flags field that if set, the audio will be repeated until the notification is cancelled or the notification window is opened.|
     */
    public static final int FLAG_INSISTENT = 4;


    /**
     * Bit to be bitwise-ored into the flags field that should be set if the notification should be canceled when it is clicked by the user.
     */
    public static final int FLAG_AUTO_CANCEL = 16;

    // Default notification priority.

    /**
     * Full screen notification
     */
    public static final int FULL_SCREEN_NOTIFICATION = 65536;

    /**
     * Half screen notification
     */
    public static final int HALF_SCREEN_NOTIFICATION = 131072;

    /**
     * Bar notification
     */
    public static final int BAR_NOTIFICATION = 262144;

    /**
     * Counter notification
     */
    public static final int COUNTER_NOTIFICATION = 524288;

    /**
     * Time between transformations is ms
     */
    public static final int TRANSIENT_TIME = 30000;

    /**
     * Time between transformations from Full\Half to Bar is ms
     */
    public static final int TRANSIENT_TIME_FULL = 60000;

    /**
     * The audio stream type to use when playing the sound.
     */
    public int audioStreamType;

    /**
     * A large-format version of contentView, giving the Notification an opportunity to show more detail.
     */
    public RemoteViews bigContentView;

    /**
     * The intent to execute when the expanded status entry is clicked.
     */
    public PendingIntent contentIntent;


    /**
     * The intent to execute when the notification is explicitly dismissed by the user, either with the "Clear All" button or by swiping it away individually.
     */
    public PendingIntent deleteIntent;

    //
    public int flags;

    /**
     * The intent to execute when long tap gesture is made
     */
    public PendingIntent longTaptIntent;


    /**
     * The view that will represent this notification in the expanded status bar.
     */
    public RemoteViews contentView;

    public String contentText;
    public String contentTitle;

    public boolean isStateChanged = false;


    /**
     * The resource id of a drawable to use as the icon in the status bar.
     */
    public int icon;

    /**
     * The resource id of a drawable to use as the icon in the status bar.
     */
    public Bitmap iconBitmap;

    /**
     * The bitmap to draw in case of fullScreen notification
     */
    public Bitmap fullScreenBitmap;

    /**
     * The resource id of a drawable to use as the icon in the status bar.
     */
    public Bitmap halfScreenBitmap;

    /**
     * @hide
     */
    public Bitmap transientBitmap;

    /**
     * Relative priority for this notification.
     */
    public int priority;

    /**
     * The sound to play.
     */
    public Uri sound;

    /**
     * The pattern with which to vibrate.
     */
    public long[] vibrate;


    /**
     * A timestamp related to this notification, in milliseconds since the epoch.
     */
    public long when;


    /**
     * Android Notification to start simultaneously
     */
    public Notification androidNotification;



    /**
     * Can be FULL_SCREEN_NOTIFICATION, HALF_SCREEN_NOTIFICATION, BAR_NOTIFICATION, COUNTER
     */
    public int BSNotificationType;

    /**
     * @hide
     */
    public int transientNotificationType;

    /**
     * Can be FULL_SCREEN_NOTIFICATION, HALF_SCREEN_NOTIFICATION, BAR_NOTIFICATION, COUNTER
     */
    public int currentState;


    /**
     * starting contentIntent after rotation device // default - true
     */
    public boolean enableRotationContentIntent = true;

    private Bitmap bitmap2Draw;

    /**
     * Don't use for external api
     * 
     * @return packageName application. which send notification
     */

    /**
     * @hide
     */
    public String packageName;

    public void setBitmap2Draw(Bitmap bitmap) {
        bitmap2Draw = bitmap;
    }

    public Bitmap getBitmap2Draw() {
        return bitmap2Draw;
    }

    public BSNotification() {
    }

    public BSNotification(Parcel parcel) {
        readFromParcel(parcel);
    }

    public BSNotification(Notification n) {
        contentIntent = n.contentIntent;
        deleteIntent = n.deleteIntent;
        when = n.when;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(1);
        if (fullScreenBitmap != null) {
            dest.writeInt(1);
            fullScreenBitmap.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (halfScreenBitmap != null) {
            dest.writeInt(1);
            halfScreenBitmap.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (transientBitmap != null) {
            dest.writeInt(1);
            transientBitmap.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (iconBitmap != null) {
            dest.writeInt(1);
            iconBitmap.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (contentView != null) {
            dest.writeInt(1);
            contentView.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (bigContentView != null) {
            dest.writeInt(1);
            bigContentView.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (contentIntent != null) {
            dest.writeInt(1);
            contentIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (deleteIntent != null) {
            dest.writeInt(1);
            deleteIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (longTaptIntent != null) {
            dest.writeInt(1);
            longTaptIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (sound != null) {
            dest.writeInt(1);
            sound.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }

        if (packageName != null) {
            dest.writeInt(1);
            dest.writeString(packageName);
        } else {
            dest.writeInt(0);
        }

        if (androidNotification != null) {
            dest.writeInt(1);
            androidNotification.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }

        dest.writeInt(currentState);
        dest.writeInt(this.flags);
        dest.writeInt(icon);
        dest.writeLong(when);
        dest.writeLongArray(vibrate);
        dest.writeInt(audioStreamType);
        dest.writeInt(BSNotificationType);
        dest.writeInt(transientNotificationType);
        dest.writeString(contentTitle);
        dest.writeString(contentText);
        dest.writeInt(priority);
        dest.writeInt(enableRotationContentIntent ? 1 : 0);

    }

    public void readFromParcel(Parcel p) {
        int version = p.readInt();
        if (p.readInt() != 0) {
            fullScreenBitmap = Bitmap.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            halfScreenBitmap = Bitmap.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            transientBitmap = Bitmap.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            iconBitmap = Bitmap.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            contentView = RemoteViews.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            bigContentView = RemoteViews.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            contentIntent = PendingIntent.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            deleteIntent = PendingIntent.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            longTaptIntent = PendingIntent.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            sound = Uri.CREATOR.createFromParcel(p);
        }

        if (p.readInt() != 0) {
            packageName = p.readString();
        }

        if (p.readInt() != 0) {
            androidNotification = Notification.CREATOR.createFromParcel(p);
        }

        currentState = p.readInt();
        flags = p.readInt();
        icon = p.readInt();
        when = p.readLong();
        vibrate = p.createLongArray();
        audioStreamType = p.readInt();
        BSNotificationType = p.readInt();
        transientNotificationType = p.readInt();
        contentTitle = p.readString();
        contentText = p.readString();
        priority = p.readInt();
        enableRotationContentIntent = p.readInt() == 1;
    }

    public static final Parcelable.Creator<BSNotification> CREATOR = new Parcelable.Creator<BSNotification>() {
        @Override
        public BSNotification createFromParcel(Parcel in) {
            return new BSNotification(in);
        }

        @Override
        public BSNotification[] newArray(int size) {
            return new BSNotification[size];
        }
    };

    public static class Builder {
        BSNotification n = new BSNotification();

        public Builder() {
        }


        /** Combines all of the options that have been set and return a new BSNotification object.
         * 
         * @return Resulted BSNotification
         */
        public BSNotification build() {
            return n;
        }

        /** Sets whether this is an "ongoing" notification.
         * 
         * @param ongoing
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setOngoing(boolean ongoing) {
            if (ongoing) {
                n.flags = n.flags | BSNotification.FLAG_ONGOING_EVENT;
            } else {
                n.flags = n.flags & ~BSNotification.FLAG_ONGOING_EVENT;
            }
            return this;
        }

        /** Sets the PendingIntent to be sent when the notification is activated.
         * @param intent PendingIntent to be sent
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setContentIntent(PendingIntent intent) {
            n.contentIntent = intent;
            return this;
        }


        /** Sets the PendingIntent to send when the notification is cleared explicitly by the user.
         * @param intent PendingIntent to be sent
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setDeleteIntent(PendingIntent intent) {
            n.deleteIntent = intent;
            return this;
        }


        /** Sets the PendingIntent to send when user makes long tap on notification
         * @param intent PendingIntent to be sent
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setLongTapIntent(PendingIntent intent) {
            n.longTaptIntent = intent;
            return this;
        }

        /** Sets the full screen notification bitmap
         * @param fullScreenBitmap Full screen notification bitmap
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setFullScreenBitmap(Bitmap fullScreenBitmap) {
            n.fullScreenBitmap = fullScreenBitmap;
            return this;
        }

        /** Sets the half screen notification bitmap
         * @param halfScreenBitmap Half screen notification bitmap
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setHalfScreenBitmap(Bitmap halfScreenBitmap) {
            n.halfScreenBitmap = halfScreenBitmap;
            return this;
        }

        // Add a large icon to the notification (and the ticker on some
        // devices).
        /*
         * BSNotification.Builder setLargeIcon(Bitmap icon) { return this; }
         */

        /** Sets the small icon resource, which will be used to represent the notification in the status bar
         * @param icon Icon resource
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setSmallIcon(int icon) {
            n.icon = icon;
            return this;
        }

        /** Sets the sound to play
         * @param sound The sound to play
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setSound(Uri sound) {
            n.sound = sound;
            return this;
        }

        /** Sets the vibration pattern to use
         * @param pattern The vibration pattern
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setVibrate(long[] pattern) {
            n.vibrate = pattern;
            return this;
        }


        /** Adds a timestamp pertaining to the notification (usually the time the event occurred).
         * @param when The timestamp
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setWhen(long when) {
            n.when = when;
            return this;
        }

        /** Sets notification type. Can be FULL_SCREEN_NOTIFICATION, HALF_SCREEN_NOTIFICATION, BAR_NOTIFICATION, COUNTER
         * @param notificationType The notification type
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setNotificationType(int notificationType) {
            n.BSNotificationType = notificationType;
            n.transientNotificationType = notificationType;
            return this;
        }

        /** Sets the content text that will be displayed in bar notification.
         * @param contentText The content text
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setContentText(String contentText) {
            n.contentText = contentText;
            return this;
        }

        /**
         * Sets the notification title to be displayed in bar notification.
         * 
         * @param contentTitle The notification title
         * @return BSNotification.Builder
         */
        public Builder setContentTitle(String contentTitle) {
            n.contentTitle = contentTitle;
            return this;
        }

        /**
         * Sets the notification priority.
         * 
         * @param priority Priority
         * @return BSNotification.Builder
         */
        public Builder setPriority(int priority) {
            n.priority = priority;
            return this;
        }

        /**
         * Use this to enable or disable Rotation Algorithm execution when user activates the notification. Use this if you start a front screen application when user activated the notification.
         * 
         * @param enableRotation
         * @return BSNotification.Builder
         */
        public Builder enableRotationContentIntent(boolean enableRotation) {
            n.enableRotationContentIntent = enableRotation;
            return this;
        }

    }

}
