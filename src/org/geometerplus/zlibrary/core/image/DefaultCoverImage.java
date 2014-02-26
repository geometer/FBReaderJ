/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.image;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.*;

import org.geometerplus.zlibrary.ui.android.R;

public class DefaultCoverImage {
	public static boolean saveToFile(Context context, String title, String url) {
		final Bitmap cover = getDefaultCover(context, title);
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
		final float textSize = 50;
		final int fieldWidth = 80;
		final int maxLines = 4;

		final Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cover);
		final Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
		final Canvas canvas = new Canvas(mutableBitmap);
		final Paint paint = getPaint(textSize);

		final List<String> text = wrapText(title, image.getWidth() - fieldWidth, paint);
		int height = (image.getHeight() - Math.min(text.size(), maxLines) * (int)textSize) / 2;
		int i = 1;
		for (String line : text) {
			if (i > maxLines) {
				break;
			}
			if (i == maxLines) {
				line = "...";
			}
			canvas.drawText(line, (image.getWidth() - paint.measureText(line)) / 2, height + (i - 1) * textSize, paint);	
			i++;
		}
		return mutableBitmap;
	}

	private static Paint getPaint(float textSize) {
		final Typeface font = Typeface.create("Arial", Typeface.BOLD);
		final Paint paint = new Paint();
		paint.setTypeface(font);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint.setShadowLayer(2, 1, 1, Color.BLACK);
		paint.setTextSize(textSize);
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
		if (result.get(result.size() - 1).equals("")) {
			result.remove(result.size() - 1);
		}
		return result;
	}

	private static int getSplitIndex(String bigString, int width, Paint paint) {
		int index = -1;
		int lastSpace = -1;
		String smallString="";
		boolean spaceEncountered = false;
		boolean maxWidthFound = false;
		for (int i = 0; i < bigString.length(); i++) {
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
