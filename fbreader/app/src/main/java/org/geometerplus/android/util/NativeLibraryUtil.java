/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

public class NativeLibraryUtil {
	public static void init(Context context, String name) {
		try {
			System.loadLibrary(name);
		} catch (UnsatisfiedLinkError e) {
        	final ApplicationInfo appInfo = context.getApplicationInfo();
			final String fileName = "lib" + name + ".so";
			try {
				final ZipFile zipFile = new ZipFile(appInfo.sourceDir);
				final ZipEntry entry = zipFile.getEntry("lib/" + Build.CPU_ABI + "/" + fileName);

				try {
					tryLoad(zipFile, entry, new File(context.getFilesDir(), fileName));
				} catch (IOException e1) {
					tryLoad(zipFile, entry, new File(context.getExternalCacheDir(), fileName));
				}
			} catch (IOException e1) {
			}
		}
	}

	private static void tryLoad(ZipFile zipFile, ZipEntry entry, File localFile) throws IOException {
		localFile.delete();
		InputStream is = null;
		OutputStream os = null;
		try {
			is = zipFile.getInputStream(entry);
			os = new FileOutputStream(localFile);
			final byte[] buffer = new byte[8192];
			while (true) {
				final int len = is.read(buffer);
				if (len <= 0) {
					break;
				}
				os.write(buffer, 0, len);
			}
			System.load(localFile.getPath());
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				// ignore
			}
			try {
				is.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
