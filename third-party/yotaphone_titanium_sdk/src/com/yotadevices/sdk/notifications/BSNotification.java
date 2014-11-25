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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RemoteViews;

/**
 * 
 * This is the main class for creating a back screen notification.
 * 
 * <BR>
 * <BR>
 * Refer to <a href=
 * "http://developer.yotaphone.com/docs/getting-started/building-apps-bs-notifications/"
 * >Building Apps with BS Notifications</a> for more details.
 */
public class BSNotification implements Parcelable {

    public static final int TYPE_FULL_SCREEN = 1;
    public static final int TYPE_BAR = 2;
    public static final int TYPE_COUNTER = 3;

    /**
     * @hide
     */
    public static final int CATEGORY_CALLS = 1;
    /**
     * @hide
     */
    public static final int CATEGORY_SMS = 2;
    /**
     * @hide
     */
    public static final int CATEGORY_EMAIL = 3;
    public static final int CATEGORY_OTHER = 4;

    public static final int TRANSIENT_TIME = 2 * 60 * 1000;
    public static final int TRANSIENT_TIME_NO_LIMIT = -1;

    /**
     * contentIntent - The intent to execute when notification on back screen is
     * swiping to right.
     */
    public PendingIntent contentIntent;

    public Intent fullScreenIntent;

    /**
     * @hide
     */
    public int flags;

    /**
     * @hide The view that will represent this notification in the expanded
     *       status bar.
     */
    public RemoteViews contentView;

    /**
     * contentText - Notification text in the bar notification.
     */
    public String contentText;

    /**
     * contentTitle - Notification title in the bar notification.
     */
    public String contentTitle;

    /**
     * The resource id of a drawable to use as the icon in case of bar
     * notification. See also {@link BSNotification#iconBitmap}
     */
    public int smallIcon;

    /**
     * iconBitmap - The bitmap to be used as an icon for bar notification.
     */
    public Bitmap largeIcon;

    /**
     * @hide The sound to play.
     */
    public Uri sound;

    /**
     * A timestamp related to this notification, in milliseconds since the
     * epoch.
     */
    public long when;

    /**
     * @hide Android Notification to start simultaneously.
     */
    public Notification androidNotification;

    /**
     * BSNotificationType - Notification type on the back screen. Can be
     * {@link BSNotification#FULL_SCREEN_NOTIFICATION},
     * {@link BSNotification#HALF_SCREEN_NOTIFICATION},
     * {@link BSNotification#BAR_NOTIFICATION}
     * 
     * <BR>
     * <BR>
     * Refer to <a href=
     * "http://developer.yotaphone.com/docs/getting-started/building-apps-bs-notifications/"
     * >Building Apps with BS Notifications</a> for more details.
     */
    public int notificationType = TYPE_BAR;
    public int notificationCategory = CATEGORY_OTHER;

    /**
     * * Time between transformations from Full to Bar is ms
     */
    public long transientTime = TRANSIENT_TIME;

    public BSNotification() {
    }

    public BSNotification(Parcel parcel) {
        readFromParcel(parcel);
    }

    /**
     * @hide
     * @param b
     */
    public BSNotification(Bundle b) {
        if (b != null) {
            contentIntent = b.getParcelable("contentIntent");
            contentTitle = b.getString("contentTitle");
            contentText = b.getString("contentText");
            fullScreenIntent = b.getParcelable("fullScreenIntent");
            largeIcon = b.getParcelable("largeIcon");
            smallIcon = b.getInt("smallIcon");
            notificationCategory = b.getInt("notificationCategory");
            notificationType = b.getInt("notificationType");
            transientTime = b.getLong("transientTime");
        }
    }

    public BSNotification(Notification n) {
        contentIntent = n.contentIntent;
        when = n.when;

        Bundle b = n.extras;
        if (b != null) {
            contentTitle = getCharSequence(b, Notification.EXTRA_TITLE);
            if (contentTitle == null) {
                contentTitle = getCharSequence(b, Notification.EXTRA_TITLE_BIG);
            }
            contentText = getCharSequence(b, Notification.EXTRA_TEXT);
            if (contentText == null) {
                contentText = getCharSequence(b, Notification.EXTRA_SUB_TEXT);
            }

            smallIcon = b.getInt(Notification.EXTRA_SMALL_ICON);
            largeIcon = b.getParcelable(Notification.EXTRA_LARGE_ICON);
        }
    }

    private String getCharSequence(Bundle b, String extraKey) {
        CharSequence value = b.getCharSequence(extraKey);
        return (value != null) ? value.toString() : null;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(1);
        if (largeIcon != null) {
            dest.writeInt(1);
            largeIcon.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (contentView != null) {
            dest.writeInt(1);
            contentView.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }

        if (contentIntent != null) {
            dest.writeInt(1);
            contentIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (fullScreenIntent != null) {
            dest.writeInt(1);
            fullScreenIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (sound != null) {
            dest.writeInt(1);
            sound.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }

        if (androidNotification != null) {
            dest.writeInt(1);
            androidNotification.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }

        dest.writeInt(this.flags);
        dest.writeInt(smallIcon);
        dest.writeLong(when);
        dest.writeInt(notificationType);
        dest.writeString(contentTitle);
        dest.writeString(contentText);
        dest.writeInt(notificationCategory);
        dest.writeLong(transientTime);
    }

    public void readFromParcel(Parcel p) {
        int version = p.readInt();

        if (p.readInt() != 0) {
            largeIcon = Bitmap.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            contentView = RemoteViews.CREATOR.createFromParcel(p);
        }

        if (p.readInt() != 0) {
            contentIntent = PendingIntent.CREATOR.createFromParcel(p);
        }
        if (p.readInt() != 0) {
            fullScreenIntent = Intent.CREATOR.createFromParcel(p);
        }

        if (p.readInt() != 0) {
            sound = Uri.CREATOR.createFromParcel(p);
        }

        if (p.readInt() != 0) {
            androidNotification = Notification.CREATOR.createFromParcel(p);
        }

        flags = p.readInt();
        smallIcon = p.readInt();
        when = p.readLong();
        notificationType = p.readInt();
        contentTitle = p.readString();
        contentText = p.readString();
        notificationCategory = p.readInt();
        transientTime = p.readLong();
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

    /**
     * This class is used for building a back screen notification.
     */
    public static class Builder {
        BSNotification n = new BSNotification();

        public Builder() {
        }

        /**
         * BSNotification build - Combines all of the options that have been set
         * and return a new BSNotification object.
         * 
         * @return Resulted BSNotification
         */
        public BSNotification build() {
            return n;
        }

        /**
         * setContentIntent - Sets the PendingIntent to be sent when the
         * notification is activated.
         * 
         * @param intent
         *            PendingIntent to be sent
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setContentIntent(PendingIntent intent) {
            n.contentIntent = intent;
            return this;
        }

        /**
         * setDeleteIntent - Sets the PendingIntent to send when notification is
         * cleared explicitly by the user.
         * 
         * @param intent
         *            PendingIntent to be sent
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setFullScreenIntent(Intent intent) {
            n.fullScreenIntent = intent;
            return this;
        }

        // Add a large icon to the notification (and the ticker on some
        // devices).
        /*
         * BSNotification.Builder setLargeIcon(Bitmap icon) { return this; }
         */

        /**
         * setSmallIcon - Sets the small icon resource, which will be used to
         * represent the notification in case of bar notification.
         * 
         * @param icon
         *            Icon resource
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setSmallIcon(int icon) {
            n.smallIcon = icon;
            return this;
        }

        /**
         * @hide Sets the sound to play
         * @param sound
         *            The sound to play
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setSound(Uri sound) {
            n.sound = sound;
            return this;
        }

        /**
         * setWhen - Adds a timestamp pertaining to the notification (usually
         * the time when event occurred).
         * 
         * @param when
         *            The timestamp
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setWhen(long when) {
            n.when = when;
            return this;
        }

        /**
         * /** setNotificationType - Sets notification type.
         * 
         * @param notificationType
         *            The notification type. Can be
         *            {@link BSNotification#FULL_SCREEN_NOTIFICATION},
         *            {@link BSNotification#HALF_SCREEN_NOTIFICATION},
         *            {@link BSNotification#BAR_NOTIFICATION}
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setNotificationType(int notificationType) {
            n.notificationType = notificationType;
            return this;
        }

        public BSNotification.Builder setNotificationCategory(int notificationCategory) {
            n.notificationCategory = notificationCategory;
            return this;
        }

        /**
         * setContentText - Sets the content text that will be displayed in bar
         * notification.
         * 
         * @param contentText
         *            The content text
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setContentText(String contentText) {
            n.contentText = contentText;
            return this;
        }

        /**
         * setContentTitle - Sets the notification title to be displayed in bar
         * notification.
         * 
         * @param contentTitle
         *            The notification title
         * @return BSNotification.Builder
         */
        public Builder setContentTitle(String contentTitle) {
            n.contentTitle = contentTitle;
            return this;
        }

        /**
         * Set transient time .
         * 
         * @param transientTime
         *            Time between transformations is ms
         * @return BSNotification.Builder
         */
        public BSNotification.Builder setTransientTime(long transientTime) {
            n.transientTime = transientTime;
            return this;
        }

    }

}
