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

        // define for driver
        private final static int WF_MODE_INIT = 0;
        private final static int WF_MODE_DU = 1;
        private final static int WF_MODE_GC16 = 2; // 16 grays (flashing)
        private final static int WF_MODE_GL16 = 3;
        private final static int WF_MODE_GLR16 = 4;
        private final static int WF_MODE_GLD16 = 5;
        private final static int WF_MODE_A2 = 6;

        // update type
        private final static int UPD_FULL = 0x33;
        private final static int UPD_PART = 0x35;

        /**
         * @hide
         */
        public int getInternalValue() {
            switch (this) {
            case WAVEFORM_DEFAULT:
                return WF_MODE_INIT;
            case WAVEFORM_DU:
                return WF_MODE_DU;
            case WAVEFORM_GC_PARTIAL:
            case WAVEFORM_GC_FULL:
                return WF_MODE_GC16;
            case WAVEFORM_GL:
                return WF_MODE_GL16;
            case WAVEFORM_GLR:
                return WF_MODE_GLR16;
            case WAVEFORM_GLD:
                return WF_MODE_GLD16;
            case WAVEFORM_A2:
                return WF_MODE_A2;
            default:
                return WF_MODE_GC16;
            }
        }

        /**
         * @hide
         */
        public int getUpdateType() {
            switch (this) {
            case WAVEFORM_A2:
            case WAVEFORM_DU:
                return UPD_PART;
            case WAVEFORM_GC_PARTIAL:
                return UPD_PART;
            case WAVEFORM_GC_FULL:
            case WAVEFORM_GL:
            case WAVEFORM_GLD:
            case WAVEFORM_GLR:
                return UPD_FULL;
            default:
                return UPD_FULL;
            }
        }
    }

    public enum Dithering {
        /**
         * @hide
         */
        DITHER_DEFAULT, DITHER_NONE, DITHER_ATKINSON, DITHER_ATKINSON_BINARY, DITHER_FLOYD_STEINBERG, DITHER_FLOYD_STEINBERG_BINARY, BLACK_AND_WHITE_ONLY;
    }

    public abstract void addViewToBS(View v, ViewGroup.LayoutParams lp);

    public abstract void removeViewFromBS(View v);

    public abstract void addBSParentView(Waveform mInitialWaveform, Dithering mInitialDithering);

    public abstract void removeBSParentView();

    public abstract Context getBSContext();

    public abstract LayoutInflater getBSLayoutInflater();

    public abstract void updateViewLayout(int visibility);

}
