package org.geometerplus.zlibrary.core.image;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.ui.android.R;

import android.content.Context;
import android.graphics.*;

public class DefaultImage {
	public static boolean saveToFile(Context context, String title, String url) {
		Bitmap cover = getDefaultCover(context, title);
		OutputStream outputStream = null;
		final File file = new File(url);
		final File parent = file.getParentFile();
		parent.mkdirs();
		try {
			outputStream = new FileOutputStream(file);
			cover.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	private static Bitmap getDefaultCover(Context context, String title) {
		final Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cover);
		final Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
		final Canvas canvas = new Canvas(mutableBitmap);
		final Paint paint = getPaint();
		final List<String> text = wrapText(title, image.getWidth() - 80, paint);
		int i = 1;
		for (String line : text) {
			if (i > 4) {
				break;
			}
			if (i == 4) {
				line = "...";
			}
			canvas.drawText(line, (image.getWidth() - paint.measureText(line)) / 2, image.getHeight() * i / Math.min(text.size(), 4) / 2 + image.getHeight() / 4, paint);	
			i++;
		}
		return mutableBitmap;
	}

	private static Paint getPaint() {
		final Typeface font = Typeface.create("Arial", Typeface.BOLD);
		final Paint paint = new Paint();
		paint.setTypeface(font);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint.setShadowLayer(2, 1, 1, Color.BLACK);
		final float fontSize = 50;
		paint.setTextSize(fontSize);
		return paint;
	}

	private static List<String> wrapText(String text, int width, Paint paint) {
		final List<String> result = new ArrayList<String>();
		String remaining = text;
		while (remaining.length() >= 0) {
			int index = getSplitIndex(remaining, width, paint);
			if (index == -1) {
				break;
			}
			result.add(remaining.substring(0, index));
			remaining = remaining.substring(index);
			if (index == 0) {
				break;
			}
		}
		return result;
	}

	private static int getSplitIndex(String bigString, int width, Paint paint) {
		int index = -1;
		int lastSpace = -1;
		String smallString="";
		boolean spaceEncountered = false;
		boolean maxWidthFound = false;
		for (int i=0; i < bigString.length(); i++) {
			char current = bigString.charAt(i);
			smallString += current;
			if (current == ' ') {
				lastSpace = i;
				spaceEncountered = true;
			}
			if (paint.measureText(smallString) > width) {
				if (spaceEncountered) {
					index = lastSpace + 1;
				} else {
					index = i;
				}
				maxWidthFound = true;
				break;	
			}	
		}
		if (!maxWidthFound) {
			index = bigString.length();
		}
		return index;
	}
}
