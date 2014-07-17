
package com.yotadevices.sdk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @hide
 */
public abstract class Drawer {

    /**
     * Waveforms that can be used to draw on Back Screen.<br>
     * <br>
     * <b>WAVEFORM_DU</b> - For drawing black and white 2-colors images. Drawing
     * time is around 250ms. This waveform makes full screen update - whole area
     * is updated. Use this to minimize ghosting effect.<br>
     * <b>WAVEFORM_GC_FULL</b> - For drawing 16-colors images. Drawing time is
     * around 500ms. This waveform makes full screen update - whole area is
     * updated. Use this to minimize ghosting effect.<br>
     * <b>WAVEFORM_GC_PARTIAL</b> - For drawing 16-colors images. Drawing time
     * is around 500ms. This waveform makes partial screen update - only those
     * pixels are updated, that were actually changed.<br>
     * <b>WAVEFORM_A2</b> - For drawing black and white 2-colors images. Drawing
     * time is around 120ms. This waveform makes partial screen update - only
     * those pixels are updated, that were actually changed.<br>
     */
    public enum Waveform {
        /**
         * @hide
         */
        WAVEFORM_DEFAULT, WAVEFORM_A2, WAVEFORM_DU, WAVEFORM_GC_PARTIAL, WAVEFORM_GC_FULL, WAVEFORM_GL, WAVEFORM_GLR, WAVEFORM_GLD;
    }

    public enum Dithering {
        /**
         * @hide
         */
        DITHER_DEFAULT, DITHER_NONE, DITHER_ATKINSON, DITHER_ATKINSON_BINARY, DITHER_FLOYD_STEINBERG, DITHER_FLOYD_STEINBERG_BINARY;
    }

    public abstract void addViewToBS(View v, ViewGroup.LayoutParams lp);

    public abstract void removeViewFromBS(View v);

    public abstract void addBSParentView();

    public abstract void removeBSParentView();

    public abstract Context getBSContext();

    public abstract LayoutInflater getBSLayoutInflater();

    public abstract void updateViewLayout(int visibility);

}
