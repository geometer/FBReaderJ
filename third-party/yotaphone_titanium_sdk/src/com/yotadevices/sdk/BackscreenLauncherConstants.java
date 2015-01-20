package com.yotadevices.sdk;

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

/**
 * Created by ASazonov on 15.07.2014.
 */
public class BackscreenLauncherConstants {
    // Options name for bundle
    public static final String OPTION_WIDGET_SIZE = "bslauncher.widget.size";
    public static final String OPTION_WIDGET_DISPLAY = "bslauncher.widget.display";
    public static final String OPTION_WIDGET_DEMO_MODE = "bslauncher.widget.demomode";

    // Options values for widget size
    public static final int WIDGET_SIZE_LARGE = 0;
    public static final int WIDGET_SIZE_MEDIUM = 1;
    public static final int WIDGET_SIZE_MEDIUM_HALF = 2;
    public static final int WIDGET_SIZE_SMALL = 3;
    public static final int WIDGET_SIZE_TINY = 4;
    public static final int WIDGET_SIZE_EXTRA_LARGE = 8;
    public static final int WIDGET_SIZE_FULL_SCREEN = 16;

    // Options values for widget display
    public static final int WIDGET_BACK_SCREEN = 0;
    public static final int WIDGET_FRONT_SCREEN = 1;

    // widget is visible
    public static final String ACTION_APPWIDGET_VISIBILITY_CHANGED = "com.yotadevices.yotaphone.action.APPWIDGET_VISIBILITY_CHANGED";
    public static final String ACTION_APPWIDGET_EXTRA_VISIBLE = "com.yotadevices.yotaphone.extras.VISIBLE";
    public static final String ACTION_APPWIDGET_EXTRA_INVISIBLE = "com.yotadevices.yotaphone.extras.INVISIBLE";
    public static final String ACTION_APPWIDGET_SWIPE_UP = "com.yotadevices.yotaphone.action.SWIPE_UP";
    public static final String ACTION_APPWIDGET_SWIPE_DOWN = "com.yotadevices.yotaphone.action.SWIPE_DOWN";

    // widget update notification
    public static final String ACTION_APPWIDGET_NOTIFICATION = "com.yotadevices.yotaphone.action.APPWIDGET_NOTIFICATION";
    public static final String ACTION_APPWIDGET_EXTRA_WIDGET_ID = "com.yotadevices.yotaphone.extras.WIDGET_ID";
    public static final String ACTION_APPWIDGET_EXTRA_DESCRIPTION = "com.yotadevices.yotaphone.extras.NOTIFICATION_DESCRIPTION";
}
