package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
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

	public enum Region {
		RUSSIA("RU1"), EUROPE("EU1"), MEA("ME1"), FLIPKART("FK1"), VODAFONE_ITALY("VIT"), H3G_ITALY("HIT"), APAC("APC"), THAILAND("THA"), CHINA("CN1"), CANADA(
				"CAN"), USA("US1"), LATAM("LTM"), UNKNOWN("UNKNOWN");

		private String mLetter;

		private Region(String letter) {
			mLetter = letter;
		}

		public static Region getRegion() {
			String version = Build.VERSION.INCREMENTAL;
			for (Region r : Region.values()) {
				if (version.contains(r.mLetter))
					return r;
			}
			return Region.UNKNOWN;
		}

		public Locale getLocale() {
			switch (this) {
				case RUSSIA:
					return new Locale("ru", "RU");
				case EUROPE:
				case MEA:
				case FLIPKART:
				case APAC:
					return Locale.UK;
				case VODAFONE_ITALY:
				case H3G_ITALY:
					return Locale.ITALIAN;
				case THAILAND:
					return new Locale("th", "TH");
				case CHINA:
					return Locale.SIMPLIFIED_CHINESE;
				case CANADA:
				case USA:
				case LATAM:
				default:
					return Locale.US;
			}
		}
	}
}