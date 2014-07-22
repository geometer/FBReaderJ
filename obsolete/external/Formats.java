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

import java.util.Arrays;
import java.util.ArrayList;

import org.geometerplus.zlibrary.core.filetypes.FileType;
import org.geometerplus.zlibrary.core.filetypes.FileTypeCollection;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;

public abstract class Formats {
	private static String PREDEFINED_FILETYPES = "fb2;ePub;Mobipocket;plain text;HTML;RTF;MS Word document;PDF;DjVu";

	public static String JAVA_OPTION = "fbreader_java";
	public static String NATIVE_OPTION = "fbreader_native";
	public static String PLUGIN_OPTION = "fbreader_plugin";

	public static String filetypeToOption(String filetype) {
		filetype = filetype.toLowerCase();
		return "FILETYPE_" + filetype;
	}

	public static String optionTofiletype(String option) {
		return option.replace("FILETYPE_", "");
	}

	public static ArrayList<String> getCustomFormats() {
		return listFromString(new ZLStringOption("Formats", "ExternalFormats", "").getValue());
	}

	public static ArrayList<String> getPredefinedFormats() {
		return listFromString(PREDEFINED_FILETYPES.toLowerCase());
	}

	private static ArrayList<String> listFromString(String s) {
		if (!s.equals("")) {
			return new ArrayList<String>(Arrays.asList(s.split(";")));
		} else {
			return new ArrayList<String>();
		}
	}

	private static boolean isValid(String filetype) {
		if (filetype == null) return false;
		if (filetype.equals("")) return false;
		if (filetype.contains(";")) return false;
		if (filetype.contains(" ")) return false;
		if (filetype.contains(".")) return false;
		return true;
	}

	public static boolean addFormat(String filetype) {
		filetype = filetype.toLowerCase();
		if (!isValid(filetype)) {
			return false;
		}
		ZLStringOption formats = new ZLStringOption("Formats", "ExternalFormats", "");
		if (formats.getValue().equals("")) {
			formats.setValue(filetype);
			return true;
		} else {
			formats.setValue(formats.getValue() + ";" +filetype);
			return true;
		}
	}

	public static void removeFormat(String filetype) {
		filetype = filetype.toLowerCase();
		ZLStringOption formats = new ZLStringOption("Formats", "ExternalFormats", "");
		String s = formats.getValue();
		if (s.equals(filetype)) {
			s = "";
			formats.setValue(s);
			return;
		}
		if (s.startsWith(filetype + ";")) {
			s = s.substring(filetype.length() + 1);
			formats.setValue(s);
			return;
		}
		if (s.endsWith(";" + filetype)) {
			s = s.substring(0, s.length() - filetype.length() - 1);
			formats.setValue(s);
			return;
		}
		s = s.replace(";" + filetype + ";", ";");
		formats.setValue(s);
	}

	public static ZLStringOption filetypeOption(String filetype) {
		filetype = filetype.toLowerCase();
		if ("fb2.zip".equals(filetype)) {
			return filetypeOption("fb2");
		}
		if (getPredefinedFormats().contains(filetype)) {
			FormatPlugin p = PluginCollection.Instance().getPlugin(FileTypeCollection.Instance.typeById(filetype), FormatPlugin.Type.ANY);
			if (p instanceof JavaFormatPlugin) {
				return new ZLStringOption("Formats", filetypeToOption(filetype), JAVA_OPTION);
			}
			if (p instanceof NativeFormatPlugin) {
				return new ZLStringOption("Formats", filetypeToOption(filetype), NATIVE_OPTION);
			}
			if (p instanceof ExternalFormatPlugin) {
				return new ZLStringOption("Formats", filetypeToOption(filetype), PLUGIN_OPTION);
			}
			return new ZLStringOption("Formats", filetypeToOption(filetype), "");
		} else if (getCustomFormats().contains(filetype)) {
			return new ZLStringOption("Formats", filetypeToOption(filetype), "");
		} else {
			return null;
		}
	}

	public static FormatPlugin.Type getStatus(String filetype) {
		filetype = filetype.toLowerCase();
		if ("fb2.zip".equals(filetype)) {
			return getStatus("fb2");
		}
		if (filetypeOption(filetype) == null) {
			return null;
		}
		String pkg = filetypeOption(filetype).getValue();
		if (pkg.equals(JAVA_OPTION)) return FormatPlugin.Type.JAVA;
		if (pkg.equals(NATIVE_OPTION)) return FormatPlugin.Type.NATIVE;
		if (pkg.equals(PLUGIN_OPTION)) return FormatPlugin.Type.EXTERNAL;
		if (pkg.equals("")) return null;
		return FormatPlugin.Type.EXTERNAL_PROGRAM;
	}
}
