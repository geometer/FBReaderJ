package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class UIUtils {
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;

    }

	public static boolean isArabic() {
		final Locale locale = Locale.getDefault();
		final String lang = locale.getLanguage();
		return lang.equals("ar");
	}

	public static String convertStringWithNumbersToArabic(String str) {
		char[] arabicChars = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
		StringBuilder builder = new StringBuilder();
		for(int i =0;i<str.length();i++)
		{
			if(Character.isDigit(str.charAt(i)))
			{
				builder.append(arabicChars[(int)(str.charAt(i))-48]);
			}
			else
			{
				builder.append(str.charAt(i));
			}
		}
		return builder.toString();
	}
}