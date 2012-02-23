/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.formats.fb2.FB2Plugin;
import org.geometerplus.fbreader.formats.oeb.OEBPlugin;
import org.geometerplus.fbreader.formats.pdb.MobipocketPlugin;
import org.geometerplus.fbreader.filetype.*;

public class PluginCollection {
	static {
		System.loadLibrary("NativeFormats-v1");
	}

	private static PluginCollection ourInstance;

	private final Map<FormatPlugin.Type,List<FormatPlugin>> myPlugins =
		new HashMap<FormatPlugin.Type,List<FormatPlugin>>();
	public ZLStringOption DefaultLanguageOption;
	public ZLStringOption DefaultEncodingOption;
	public ZLBooleanOption LanguageAutoDetectOption;

	public static PluginCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new PluginCollection();
			ourInstance.runTests();
		}
		return ourInstance;
	}

	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance = null;
		}
	}

	private PluginCollection() {
		LanguageAutoDetectOption = new ZLBooleanOption("Format", "AutoDetect", true);
		DefaultLanguageOption = new ZLStringOption("Format", "DefaultLanguage", "en");
		DefaultEncodingOption = new ZLStringOption("Format", "DefaultEncoding", "windows-1252");

		addPlugin(new FB2Plugin());
		addPlugin(new MobipocketPlugin());
		addPlugin(new OEBPlugin());

		for (FormatPlugin p : nativePlugins()) {
			addPlugin(p);
		}
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

	public FormatPlugin getPlugin(ZLFile file) {
		return getPlugin(file, FormatPlugin.Type.ANY);
	}

	public FormatPlugin getPlugin(ZLFile file, FormatPlugin.Type formatType) {
		final FileType fileType = FileTypeCollection.Instance.typeForFile(file);
		return getPlugin(fileType, formatType);
	}

	public FormatPlugin getPlugin(FileType fileType, FormatPlugin.Type formatType) {
		if (fileType == null) {
			return null;
		}

		if (formatType == FormatPlugin.Type.ANY) {
			FormatPlugin p = getPlugin(fileType, FormatPlugin.Type.NATIVE);
			if (p == null) {
				p = getPlugin(fileType, FormatPlugin.Type.JAVA);
			}
			return p;
		} else {
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

	private native FormatPlugin[] nativePlugins();
	private native void free();

	// called from native code
	public String getDefaultLanguage() {
		return DefaultLanguageOption.getValue();
	}

	// called from native code
	public String getDefaultEncoding() {
		return DefaultEncodingOption.getValue();
	}

	// called from native code
	public boolean isLanguageAutoDetectEnabled() {
		return LanguageAutoDetectOption.getValue();
	}

	protected void finalize() throws Throwable {
		deleteInstance();
		myPlugins.clear();
		free();
		super.finalize();
	};

	private native void runTests();
}
