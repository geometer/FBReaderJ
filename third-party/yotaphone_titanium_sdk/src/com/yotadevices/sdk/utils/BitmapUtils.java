package com.yotadevices.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;

/**
 * @hide
 * 
 *       Utils for bitmap preprocessing for displaying them on back screen.
 * 
 * 
 */
public class BitmapUtils {

    /**
     * Makes standard preprocessing for the image that should be drawn on BS: <BR>
     * - Bitmap sharpening <BR>
     * - Contrast and brightness adjustment <BR>
     * <BR>
     * This function is for back-compatibility
     * 
     * @param context
     *            context
     * @param bitmap
     *            input bitmap
     * @return new bitmap
     */
    public static Bitmap prepareImageForBS(Context context, Bitmap bitmap) {
        return BitmapUtils.changeBitmapContrastBrightness(BitmapUtils.sharpenBitmap(context, bitmap, 0.15f), 1.2f, -30);
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
        ColorMatrix cm = new ColorMatrix(new float[] { contrast, 0, 0, 0, brightness, 0, contrast, 0, 0, brightness, 0, 0, contrast, 0, brightness, 0, 0, 0, 1,
                0 });

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

}
