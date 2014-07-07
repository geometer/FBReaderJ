package com.yotadevices.sdk.notifications;

import com.yotadevices.sdk.notifications.BSNotification;

interface IBSNotification {

    void drawNotification(in int id, in BSNotification notification);
    
    boolean isNotificationVisible(in int id);
}