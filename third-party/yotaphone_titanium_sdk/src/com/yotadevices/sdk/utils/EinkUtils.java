package com.yotadevices.sdk.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;

import com.yotadevices.sdk.Drawer;

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
        if (view!=null) {
            view.setEpdViewWaveFormMode(waveform.ordinal());
        }
    }

    public static Drawer.Waveform getViewWaveform(View view) {
        if (view!=null) {
            return Drawer.Waveform.values()[view.getEpdViewWaveformMode()];
        } else {
            return Drawer.Waveform.WAVEFORM_DEFAULT;
        }
    }

    public static void disableViewDithering(View view) {
        if (view!=null) {
            view.setEpdViewDithering(Drawer.Dithering.DITHER_NONE.ordinal());
        }
    }

    public static void setViewDithering(View view, Drawer.Dithering dithering) {
        if (view!=null) {
            view.setEpdViewDithering(dithering.ordinal());
        }
    }

    public static Drawer.Dithering getViewDithering(View view) {
        if (view!=null) {
            return Drawer.Dithering.values()[view.getEpdViewDithering()];
        } else {
            return Drawer.Dithering.DITHER_DEFAULT;
        }
    }

    public static void performSingleUpdate(View view, Drawer.Waveform waveform) {
        performSingleUpdate(view, waveform, Drawer.Dithering.DITHER_DEFAULT);
    }

    public static void performSingleUpdate(final View view, final Drawer.Waveform waveform, final Drawer.Dithering dithering) {
        final Drawer.Waveform previousWaveform = getViewWaveform(view);
        final Drawer.Dithering previousDithring = getViewDithering(view);
        if (view!=null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setViewWaveform(view, waveform);
                    setViewDithering(view, dithering);
                    view.invalidate();
                    setViewWaveform(view, previousWaveform);
                    setViewDithering(view, previousDithring);
                }
            }, 200);

        }
    }
}
