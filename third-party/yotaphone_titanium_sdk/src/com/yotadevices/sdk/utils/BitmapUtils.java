package com.yotadevices.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;

import com.yotadevices.sdk.Drawer;

/**
 * @hide
 * 
 *       Utils for bitmap preprocessing for displaying them on back screen.
 * 
 * 
 */
public class BitmapUtils {
    public static final int DITHER_ATKINSON = 1;
    public static final int DITHER_FLOYD_STEINBERG= 2;
    /**
     * Makes standard preprocessing for the image that should be drawn on BS: <BR>
     * - Bitmap sharpening <BR>
     * - Contrast and brightness adjustment <BR>
     * <BR>
     * This function is for back-compatibility
     *
     * @param bitmap
     *            input bitmap
     * @return new bitmap
     */
    public static Bitmap prepareImageForBS(Bitmap bitmap) {
        return BitmapUtils.sharpenBitmap(BitmapUtils.changeBitmapContrastBrightness(bitmap, 1.2f, -30), 0.15f);
    }

    /**
     * Sharpens the Bitmap
     *
     * @param bitmap
     *            input bitmap
     * @param weight
     *            Should >= 0. 0 is default
     * @return new bitmap
     */
    public static Bitmap sharpenBitmap(Bitmap bitmap, float weight) {
        com.yotadevices.yotaphone2.sdk.EpdUtils.sharpenBitmap(bitmap, weight);
        return bitmap;
    }

    /**
     * Dithers the Bitmap
     *
     * @param bitmap
     *            input bitmap
     * @param ditheringAlgorithm
     *            Dithering algorithm to use. Can be DITHER_ATKINSON or DITHER_FLOYD_STEINBERG
     * @param binary
     *            true if resulting pircure should have only pure black and pure white pixels. Use this for drawing fast animations with WAVEFORM_A2;<br/>
     *            false if resulting pircure should have full 16-colors greyscale. Use this for drawing beautiful well-detailed images.
     * @return new bitmap<br/><br/>
     *
     * The palette of returned bitmap will consist only of following colors. Please notice that all transparency will be gone.<br/>
     * 0xff000000<br/>
     * 0xff101010<br/>
     * 0xff212121<br/>
     * 0xff313131<br/>
     * 0xff434343<br/>
     * 0xff535353<br/>
     * 0xff646464<br/>
     * 0xff747474<br/>
     * 0xff8b8b8b<br/>
     * 0xff9b9b9b<br/>
     * 0xffacacac<br/>
     * 0xffbbbbbb<br/>
     * 0xffcecece<br/>
     * 0xffdedede<br/>
     * 0xffefefef<br/>
     * 0xffffffff<br/>
     */
    public static Bitmap ditherBitmap(Bitmap bitmap, int ditheringAlgorithm, boolean binary) {
        com.yotadevices.yotaphone2.sdk.EpdUtils.ditherBitmap(bitmap, ditheringAlgorithm, binary);
        return bitmap;
    }

    /**
     * Sharpens the Bitmap
     * 
     * @param context
     *            context
     * @param bitmap
     *            input bitmap
     * @param weight
     *            Should >= 0. 0 is default
     * @return new bitmap
     */
    public static Bitmap sharpenBitmap(Context context, Bitmap bitmap, float weight) {
        RenderScript mRS = RenderScript.create(context);
        Allocation mInAllocation = Allocation.createFromBitmap(mRS, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation mOutAllocation = Allocation.createTyped(mRS, mInAllocation.getType());

        ScriptIntrinsicConvolve3x3 d = ScriptIntrinsicConvolve3x3.create(mRS, Element.U8_4(mRS));
        d.setCoefficients(new float[] { -weight, -weight, -weight, -weight, 8 * weight + 1, -weight, -weight, -weight, -weight });
        d.setInput(mInAllocation);
        d.forEach(mOutAllocation);
        mOutAllocation.copyTo(bitmap);
        mRS.destroy();
        return bitmap;
    }

    /**
     * Changes the contrast and brightness of the bitmap
     * 
     * @param bitmap
     *            input bitmap
     * @param contrast
     *            0..10 1 is default
     * @param brightness
     *            -255..255 0 is default
     * @return new bitmap
     */
    public static Bitmap changeBitmapContrastBrightness(Bitmap bitmap, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[] { contrast, 0, 0, 0, brightness, 0, contrast, 0, 0, brightness, 0, 0, contrast, 0, brightness, 0, 0, 0, 1, 0 });
        Bitmap ret = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return ret;
    }

    /**
     * Makes grayscale-bitmap
     * 
     * @param bmpOriginal
     *            - input bitmap
     * @param width
     *            - width of input bitmap
     * @param height
     *            - height of input bitmap
     * @return new grayscaled bitmap
     */
    public static final Bitmap toGrayscale(Bitmap bmpOriginal, int width, int height) {
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        paint.setFilterBitmap(true);
        c.drawBitmap(bmpOriginal, null, new Rect(0, 0, width, height), paint);
        return bmpGrayscale;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
}
