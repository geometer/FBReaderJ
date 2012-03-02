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

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.fbreader.filetype.*;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class Formats {

	private static String JAVA_FILETYPES = "fb2;epub;mobipocket";
	private static String NATIVE_FILETYPES = "";//TODO

	private static String PREDEFINED_FILETYPES = "html;pdf;djvu";

	public static String JAVA_OPTION = "fbreader_java";
	public static String NATIVE_OPTION = "fbreader_native";

	public static String filetypeToOption(String filetype) {
		filetype = filetype.toLowerCase();
		return "FILETYPE_" + filetype;
	}

	public static String optionTofiletype(String option) {
		return option.replace("FILETYPE_", "");
	}

	public static ArrayList<String> getCustomExternalFormats() {
		return listFromString(new ZLStringOption("Formats", "ExternalFormats", "").getValue());
	}

	public static ArrayList<String> getPredefinedExternalFormats() {
		return listFromString(PREDEFINED_FILETYPES);
	}

	public static ArrayList<String> getJavaFormats() {
		return listFromString(JAVA_FILETYPES);
	}

	public static ArrayList<String> getNativeFormats() {
		return listFromString(NATIVE_FILETYPES);
	}

	private static ArrayList<String> listFromString(String s) {
		if (!s.equals("")) {
			return new ArrayList<String>(Arrays.asList(s.split(";")));
		} else {
			return new ArrayList<String>();
		}
	}

	private static boolean isValid(String filetype) {
		if (filetype.equals("")) return false;
		if (filetype.contains(";")) return false;
		if (filetype.contains(" ")) return false;
		if (filetype.contains(".")) return false;
		return true;
	}

	public static FileType getExistingFileType(String extension) {
		for (String s : getJavaFormats()) {
			FileType type = FileTypeCollection.Instance.typeById(s);
			if (type.acceptsExtension(extension)) {
				return type;
			}
		}
		for (String s : getNativeFormats()) {
			FileType type = FileTypeCollection.Instance.typeById(s);
			if (type.acceptsExtension(extension)) {
				return type;
			}
		}
		for (String s : getPredefinedExternalFormats()) {
			FileType type = FileTypeCollection.Instance.typeById(s);
			if (type.acceptsExtension(extension)) {
				return type;
			}
		}
		for (String s : getCustomExternalFormats()) {
			FileType type = FileTypeCollection.Instance.typeById(s);
			if (type.acceptsExtension(extension)) {
				return type;
			}
		}
		return null;
	}

	public static boolean addFormat(String filetype) {
		filetype = filetype.toLowerCase();
		if (!isValid(filetype)) {
			return false;
		}
		if (getExistingFileType(filetype) != null) {
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
		if (getJavaFormats().contains(filetype)) {
			return new ZLStringOption("Formats", filetypeToOption(filetype), JAVA_OPTION);
		} else if (getNativeFormats().contains(filetype)) {
			return new ZLStringOption("Formats", filetypeToOption(filetype), NATIVE_OPTION);
		} else {
			ZLStringOption opt = new ZLStringOption("Formats", filetypeToOption(filetype), "");
			return new ZLStringOption("Formats", filetypeToOption(filetype), "");
		}
	}

	public static FormatPlugin.Type getStatus(String filetype) {
		filetype = filetype.toLowerCase();
		String pkg = filetypeOption(filetype).getValue();
		if (pkg.equals(JAVA_OPTION)) return FormatPlugin.Type.JAVA;
		if (pkg.equals(NATIVE_OPTION)) return FormatPlugin.Type.NATIVE;
		if (pkg.equals("")) return FormatPlugin.Type.NONE;
		return FormatPlugin.Type.EXTERNAL;
	}
}
