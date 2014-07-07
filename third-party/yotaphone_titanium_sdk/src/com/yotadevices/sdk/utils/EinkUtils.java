package com.yotadevices.sdk.utils;

import com.yotadevices.sdk.Drawer;

import android.graphics.Bitmap;
import android.view.View;

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
         view.setEpdViewWaveFormMode(waveform.ordinal());
    }

    public static Drawer.Waveform getViewWaveform(View view) {
        //return null;
        return Drawer.Waveform.values()[view.getEpdViewWaveformMode()];
    }

    public static void setViewDithering(View view) {
        view.setEpdViewDithering(View.DITHER_ATKINSON);
    }

    public static int getViewDithering(View view) {
        //return 0;
        return view.getEpdViewDithering();
    }
}
