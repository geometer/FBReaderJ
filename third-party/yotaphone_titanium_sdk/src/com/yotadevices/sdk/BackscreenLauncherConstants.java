package com.yotadevices.sdk;

/**
 * Created by ASazonov on 15.07.2014.
 */
public class BackscreenLauncherConstants {
    // Options name for bundle
    public static final String OPTION_WIDGET_SIZE = "bslauncher.widget.size";
    public static final String OPTION_WIDGET_DISPLAY = "bslauncher.widget.display";

    // Options values for widget size
    public static final int WIDGET_SIZE_LARGE = 0;
    public static final int WIDGET_SIZE_MEDIUM = 1;
    public static final int WIDGET_SIZE_MEDIUM_HALF = 2;
    public static final int WIDGET_SIZE_SMALL = 3;
    public static final int WIDGET_SIZE_TINY = 4;

    // Options values for widget display
    public static final int WIDGET_BACK_SCREEN = 0;
    public static final int WIDGET_FRONT_SCREEN = 1;

    // widget is visible
    public static final String ACTION_APPWIDGET_VISIBILITY_CHANGED = "com.yotadevices.yotaphone.action.APPWIDGET_VISIBILITY_CHANGED";
    public static final String ACTION_APPWIDGET_EXTRA_VISIBLE = "com.yotadevices.yotaphone.extras.VISIBLE";
    public static final String ACTION_APPWIDGET_EXTRA_INVISIBLE = "com.yotadevices.yotaphone.extras.INVISIBLE";
}
