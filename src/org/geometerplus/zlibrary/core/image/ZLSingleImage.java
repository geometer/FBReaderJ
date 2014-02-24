/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.R;

import android.content.Context;
import android.graphics.*;

public abstract class ZLSingleImage implements ZLImage {
	private final MimeType myMimeType;

	public ZLSingleImage(final MimeType mimeType) {
		myMimeType = mimeType;
	}

	public abstract InputStream inputStream();

	public final MimeType mimeType() {
		return myMimeType;
	}
	
	@Override
	public boolean saveToFile(Context context, String url, boolean force) {
		if (!saveExistingCover(url)) {
			if (force) {
				return saveDefaultCover(context, url);
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean saveExistingCover(String url) {
		final InputStream inputStream = inputStream();
		if (inputStream == null) {
			return false;
		}

		OutputStream outputStream = null;
		final File file = new File(url);
		final File parent = file.getParentFile();
		parent.mkdirs();
		try {
			outputStream = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
	
	private boolean saveDefaultCover(Context context, String url) {
		final Bitmap cover = makeDefaultCover(context);
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
	
	private Bitmap makeDefaultCover(Context context) {
		final String title = "Text example";
		final Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cover);
		final Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
		final Canvas canvas = new Canvas(mutableBitmap);
		final Paint paint = makePaint();
		canvas.drawText(title, (image.getWidth() - paint.measureText(title)) / 2, image.getHeight() / 2, paint);
		return mutableBitmap;
	}
	
	private Paint makePaint() {
		final Typeface font = Typeface.create("Arial", Typeface.BOLD);
		final Paint paint = new Paint();
		paint.setTypeface(font);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);
		final float fontSize = 50;
		paint.setTextSize(fontSize);
		return paint;
	}
}
