/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

public class PluginCollection {
	private static PluginCollection ourInstance;

	private final ArrayList<FormatPlugin> myPlugins = new ArrayList<FormatPlugin>();
	public ZLStringOption DefaultLanguageOption;
	public ZLStringOption DefaultEncodingOption;
	public ZLBooleanOption LanguageAutoDetectOption;
	
	public static PluginCollection Instance() {
		if (ourInstance == null) {
			ourInstance = new PluginCollection();
			ourInstance.myPlugins.add(new FB2Plugin());
			//ourInstance.myPlugins.add(new PluckerPlugin());
			//ourInstance->myPlugins.push_back(new DocBookPlugin());
			//ourInstance.myPlugins.add(new HtmlPlugin());
			//ourInstance.myPlugins.add(new TxtPlugin());
			//ourInstance.myPlugins.add(new PalmDocPlugin());
			ourInstance.myPlugins.add(new MobipocketPlugin());
			//ourInstance.myPlugins.add(new ZTXTPlugin());
			//ourInstance.myPlugins.add(new TcrPlugin());
			//ourInstance.myPlugins.add(new CHMPlugin());
			ourInstance.myPlugins.add(new OEBPlugin());
			//ourInstance.myPlugins.add(new RtfPlugin());
			//ourInstance.myPlugins.add(new OpenReaderPlugin());
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
	}
		
	public FormatPlugin getPlugin(ZLFile file) {
		for (FormatPlugin plugin : myPlugins) {
			if (plugin.acceptsFile(file)) {
				return plugin;
			}
		}
		return null;
	}
}
