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

package org.geometerplus.fbreader.formats;

import java.util.*;

import android.os.Build;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filetypes.*;

public class PluginCollection {
	static {
		System.loadLibrary("NativeFormats-v4");
	}

	private static PluginCollection ourInstance;

	private final List<BuiltinFormatPlugin> myBuiltinPlugins =
		new LinkedList<BuiltinFormatPlugin>();
	private final List<ExternalFormatPlugin> myExternalPlugins =
		new LinkedList<ExternalFormatPlugin>();

	public static PluginCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new PluginCollection();

			// This code can not be moved to constructor because nativePlugins() is a native method
			for (NativeFormatPlugin p : ourInstance.nativePlugins()) {
				ourInstance.myBuiltinPlugins.add(p);
				System.err.println("native plugin: " + p);
			}
		}
		return ourInstance;
	}

	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance = null;
		}
	}

	private PluginCollection() {
		if (Build.VERSION.SDK_INT >= 8) {
			myExternalPlugins.add(new DjVuPlugin());
			myExternalPlugins.add(new PDFPlugin());
		}
	}

	public FormatPlugin getPlugin(ZLFile file) {
		final FileType fileType = FileTypeCollection.Instance.typeForFile(file);
		final FormatPlugin plugin = getPlugin(fileType);
		if (plugin instanceof ExternalFormatPlugin) {
			return file == file.getPhysicalFile() ? plugin : null;
		}
		return plugin;
	}

	public FormatPlugin getPlugin(FileType fileType) {
		if (fileType == null) {
			return null;
		}

		for (FormatPlugin p : myBuiltinPlugins) {
			if (fileType.Id.equalsIgnoreCase(p.supportedFileType())) {
				return p;
			}
		}
		for (FormatPlugin p : myExternalPlugins) {
			if (fileType.Id.equalsIgnoreCase(p.supportedFileType())) {
				return p;
			}
		}
		return null;
	}

	private native NativeFormatPlugin[] nativePlugins();
	private native void free();

	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}
}
