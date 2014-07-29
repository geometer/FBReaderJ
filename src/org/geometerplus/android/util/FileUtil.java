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

package org.geometerplus.android.util;

import java.io.*;

import android.content.Context;
import android.os.Build;

import nextapp.mediafile.MediaFile;

public abstract class FileUtil {
	public static OutputStream createOutputStream(Context context, File file) throws IOException {
		try {
			return new FileOutputStream(file);
		} catch (IOException e) {
			if (Build.VERSION.SDK_INT < 19/*Build.VERSION_CODES.KITKAT*/) {
				throw e;
			}
			return new MediaFile(context.getContentResolver(), file).write();
		}
	}

	public static boolean mkdirs(Context context, File file) {
		if (file.mkdirs()) {
			return true;
		}
		if (Build.VERSION.SDK_INT < 19/*Build.VERSION_CODES.KITKAT*/) {
			return false;
		}
		final File parent = file.getParentFile();
		if (parent != null && parent.getPath().length() < file.getPath().length()) {
			if (!parent.exists() && !mkdirs(context, parent)) {
				return false;
			}
		}
		try {
			return new MediaFile(context.getContentResolver(), file).mkdir();
		} catch (IOException e) {
			return false;
		}
	}
}
