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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.filetypes.*;

import org.geometerplus.fbreader.formats.fb2.FB2Plugin;
import org.geometerplus.fbreader.formats.fb2.FB2ZipExternalPlugin;
import org.geometerplus.fbreader.formats.oeb.OEBPlugin;
import org.geometerplus.fbreader.formats.pdb.MobipocketPlugin;

public class PluginCollection {
	static {
		System.loadLibrary("NativeFormats-v4");
	}

	private static PluginCollection ourInstance;

	private final Map<FormatPlugin.Type,List<FormatPlugin>> myPlugins =
		new HashMap<FormatPlugin.Type,List<FormatPlugin>>();

	public static PluginCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new PluginCollection();

			// This code can not be moved to constructor because nativePlugins() is a native method
			for (NativeFormatPlugin p : ourInstance.nativePlugins()) {
				ourInstance.addPlugin(p);
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
		addPlugin(new FB2Plugin());
		addPlugin(new MobipocketPlugin());
		addPlugin(new OEBPlugin());
		addPlugin(new FB2ZipExternalPlugin());
		addPlugin(new PdfPluginFormatPlugin());
	}

	private void addPlugin(FormatPlugin plugin) {
		final FormatPlugin.Type type = plugin.type();
		List<FormatPlugin> list = myPlugins.get(type);
		if (list == null) {
			list = new ArrayList<FormatPlugin>();
			myPlugins.put(type, list);
		}
		list.add(plugin);
	}

	public List<String> getPluginPackages() {
		ArrayList<String> list = new ArrayList<String>();
		for (FormatPlugin p : myPlugins.get(FormatPlugin.Type.PLUGIN)) {
			list.add(((PluginFormatPlugin)p).getPackage());
		}
		return list;
	}
	
	public FormatPlugin getPlugin(ZLFile file) {
		final FileType fileType = FileTypeCollection.Instance.typeForFile(file);
		if (fileType == null) {
			return null;
		}
		if (file instanceof ZLResourceFile) {
			return getPlugin(fileType, FormatPlugin.Type.JAVA);
		}
		return getPlugin(fileType, Formats.getStatus(fileType.Id));
	}
	
	private FormatPlugin getOrCreateExternalPlugin(FileType fileType) {
		boolean exists = true;
		final List<FormatPlugin> list = myPlugins.get(FormatPlugin.Type.EXTERNAL);
		if (list == null) {
			exists = false;
		}
		if (exists) {
			for (FormatPlugin p : list) {
				if (fileType.Id.equalsIgnoreCase(p.supportedFileType())) {
					return p;
				}
			}
		}

		FormatPlugin plugin;
		FormatPlugin builtInPlugin = getPlugin(fileType, FormatPlugin.Type.NATIVE);
		if (builtInPlugin == null) {
			builtInPlugin = getPlugin(fileType, FormatPlugin.Type.JAVA);
		}
		if (builtInPlugin != null) {
			plugin = new ExternalFormatPlugin(fileType.Id, builtInPlugin);
		} else {
			plugin = new ExternalFormatPlugin(fileType.Id);
		}
		addPlugin(plugin);
		return plugin;
	}

	public FormatPlugin getPlugin(FileType fileType, FormatPlugin.Type formatType) {
		if (fileType == null) {
			return null;
		}

		switch (formatType) {
			case NONE:
				return null;
			case EXTERNAL:
				return getOrCreateExternalPlugin(fileType);
			case ANY:
			{
				FormatPlugin p = getPlugin(fileType, FormatPlugin.Type.NATIVE);
				if (p == null) {
					p = getPlugin(fileType, FormatPlugin.Type.JAVA);
				}
				if (p == null) {
					p = getPlugin(fileType, FormatPlugin.Type.PLUGIN);
				}
				return p;
			}
			default:
			{
				final List<FormatPlugin> list = myPlugins.get(formatType);
				if (list == null) {
					return null;
				}
				for (FormatPlugin p : list) {
					if (fileType.Id.equalsIgnoreCase(p.supportedFileType())) {
						return p;
					}
				}
				return null;
			}
		}
	}

	private native NativeFormatPlugin[] nativePlugins();
	private native void free();

	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}
}
