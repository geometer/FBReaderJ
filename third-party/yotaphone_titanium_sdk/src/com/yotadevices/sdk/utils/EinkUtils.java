package com.yotadevices.sdk.utils;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.Drawer.Waveform;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RemoteViews;

/*
 *
 * Copyright (C) 2012-2014 Yota Devices
 *
 */

/**
 * @hide Utils for the back screen
 */
public class EinkUtils {

    /*
     * static { System.loadLibrary("yotadevices_platinum_jni"); // TODO: }
     */

    public static final int NO_DITHERING = 0;
    public static final int ATKINSON_DITHERING = 1;
    public static final int FLOYD_STEINBERG_DITHERING = 2;

    /**
     * DEVICE_COLOR_BLACK = "white": Constant used to show that user is using a
     * white device. Use {@link EinkUtils#getDeviceColor} to determine what
     * color of device user is using.
     */
    public final static String DEVICE_COLOR_WHITE = "white";
    /**
     * DEVICE_COLOR_BLACK = "black": Constant used to show that user is using a
     * black device. Use {@link EinkUtils#getDeviceColor} to determine what
     * color of device user is using.
     */
    public final static String DEVICE_COLOR_BLACK = "black";

    /**
     * @hide TODO: Not working yet
     */
    public static Bitmap captureScreenshot() {
        throw new IllegalStateException("Not working yet");
    }

    /**
     * @hide TODO: Not working yet
     * 
     *       getDeviceColor - Color of the device.
     * 
     * @return Can return {@link EinkUtils#DEVICE_COLOR_WHITE} or
     *         {@link EinkUtils#DEVICE_COLOR_BLACK}
     * 
     */
    public static String getDeviceColor() {
        throw new IllegalStateException("Not working yet");
        // return android.os.SystemProperties.get("service.platinumd.skucolor");
    }

    /**
     * @hide TODO: Not working yet
     */
    public static Bitmap ditherBitmap(Bitmap bmp, int ditheringAlgorithm) {
        throw new IllegalStateException("Not working yet");
    }

    /**
     * @hide TODO: Not working yet
     */
    public static Bitmap ditherBitmapBin(Bitmap bmp, int ditheringAlgorithm) {
        throw new IllegalStateException("Not working yet");
    }

    public static void setViewWaveform(View view, Drawer.Waveform waveform) {
        //if (view != null) {
        //    view.setEpdViewWaveFormMode(waveform.ordinal());
        //}
        Log.d("EinkUtils", "setViewWaveform " +waveform +" "+view);
	    com.yotadevices.yotaphone2.sdk.EpdUtils.setEpdViewWaveFormMode(view, waveform.ordinal());
    }

    public static Drawer.Waveform getViewWaveform(View view) {
        Drawer.Waveform waveform;
        if (view != null) {
            //return Drawer.Waveform.values()[view.getEpdViewWaveformMode()];
            waveform =  Drawer.Waveform.values()[com.yotadevices.yotaphone2.sdk.EpdUtils.getEpdViewWaveformMode(view)];
        } else {
            waveform = Drawer.Waveform.WAVEFORM_DEFAULT;
        }
        Log.d("EinkUtils", "getViewWaveform " +waveform +" "+view);
        return waveform;
    }

    public static void disableViewDithering(View view) {
        if (view != null) {
            //view.setEpdViewDithering(Drawer.Dithering.DITHER_NONE.ordinal());
	        com.yotadevices.yotaphone2.sdk.EpdUtils.setEpdViewDithering(view, Drawer.Dithering.DITHER_NONE.ordinal());
        }
    }

    public static void setViewDithering(View view, Drawer.Dithering dithering) {
        Log.d("EinkUtils", "setViewDithering " +dithering +" "+view);
        if (view != null) {
	        com.yotadevices.yotaphone2.sdk.EpdUtils.setEpdViewDithering(view, dithering.ordinal());
        }
    }

    public static void setRemoteViewsDithering(RemoteViews remoteViews, int viewId, Drawer.Dithering dithering) {
        Log.d("EinkUtils", "setRemoteViewsDithering " +dithering +" "+remoteViews);
        if (remoteViews != null) {
            remoteViews.setInt(viewId, "setEpdViewDithering", dithering.ordinal());
        }
    }
    public static void setRemoteViewsWaveform(RemoteViews remoteViews, int viewId, Drawer.Waveform waveform) {
        Log.d("EinkUtils", "setRemoteViewsWaveform " +waveform +" "+remoteViews);
        if (remoteViews != null) {
            remoteViews.setInt(viewId, "setEpdViewWaveFormMode", waveform.ordinal());
        }
    }

    public static Drawer.Dithering getViewDithering(View view) {
        Drawer.Dithering dithering;
        if (view != null) {
            //return Drawer.Dithering.values()[view.getEpdViewDithering()];
            dithering = Drawer.Dithering.values()[com.yotadevices.yotaphone2.sdk.EpdUtils.getEpdViewDithering(view)];
        } else {
            dithering = Drawer.Dithering.DITHER_DEFAULT;
        }
        Log.d("EinkUtils", "getViewDithering " +dithering +" "+view);
        return dithering;
    }

    public static void performSingleUpdate(View view, Drawer.Waveform waveform) {
        performSingleUpdate(view, waveform, Drawer.Dithering.DITHER_DEFAULT);
    }

    // update with delay
    public static void performSingleUpdate(final View view, final Drawer.Waveform waveform, final Drawer.Dithering dithering, final int delay) {
        if (view != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    performSingleUpdate(view, waveform, dithering);
                }
            }, delay);

        }
    }

    /**
     * 
     * @param context
     *            - current context for binding to BackScreenManager
     * @param waveform
     *            - waveform for a single update EPD screen
     */
    public static void performSingleUpdate(Context context, Waveform waveform) {
        if (waveform == null) {
            throw new IllegalArgumentException("Waveform parameter can't be null");
        }
        FrameworkUtils.performSingleUpdate(context, waveform);
    }

    public static void performSingleUpdate(final View view, final Drawer.Waveform waveform, final Drawer.Dithering dithering) {
        com.yotadevices.yotaphone2.sdk.EpdUtils.performSingleUpdate(view, waveform.ordinal(), dithering.ordinal());
    }

    public static void addViewEpd(WindowManager wm, View view, ViewGroup.LayoutParams lp, Drawer.Waveform waveform, Drawer.Dithering dithering) {
        com.yotadevices.yotaphone2.sdk.EpdUtils.addViewEpd(wm, view, lp, waveform.ordinal(), dithering.ordinal());
    }

    public static void removeViewEpd(WindowManager wm, View view) {
        com.yotadevices.yotaphone2.sdk.EpdUtils.removeViewWithoutUpdate(wm, view);
    }

    public static void setVisibilityWithoutUpdate(View view, int visibility) {
        com.yotadevices.yotaphone2.sdk.EpdUtils.setVisibilityWithoutUpdate(view, visibility);

    }
}
