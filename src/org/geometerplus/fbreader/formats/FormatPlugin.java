/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import java.io.*;
import java.util.*;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.description.BookDescription.WritableBookDescription;
import org.geometerplus.zlibrary.core.dialogs.ZLOptionsDialog;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.language.ZLLanguageDetector;

public abstract class FormatPlugin {
	public abstract boolean providesMetaInfo();
	public abstract boolean acceptsFile(ZLFile file);
	public abstract String getIconName();
	
	public String tryOpen(String path) {
		return "";
	}
	
	public abstract	boolean readDescription(String path, BookDescription description);
	public abstract boolean readModel(BookDescription description, BookModel model);
	
	public FormatInfoPage createInfoPage(ZLOptionsDialog d, String str) {
		return null;
	}

	public static void detectEncodingAndLanguage(BookDescription description, InputStream stream) throws IOException {	
		String language = description.getLanguage();
		String encoding = description.getEncoding();
		if (encoding.length() == 0 || language.length() == 0) {
			PluginCollection collection = PluginCollection.instance();
			if (language.length() == 0) {
				language = collection.DefaultLanguageOption.getValue();
			}
			if (encoding.length() == 0) {
				encoding = collection.DefaultEncodingOption.getValue();
			}
			if (collection.LanguageAutoDetectOption.getValue() && stream != null) {
				int BUFSIZE = 65536;
				byte[] buffer = new byte[BUFSIZE];
				int size = stream.read(buffer, 0, BUFSIZE);
				stream.close();
				ZLLanguageDetector.LanguageInfo info =
					new ZLLanguageDetector().findInfo(buffer, 0, size);
				buffer = null;
				if (info != null) {
					language = info.Language;
					encoding = info.Encoding;
					if ((encoding == "US-ASCII") || (encoding == "ISO-8859-1")) {
						encoding = "windows-1252";
					}
				}
			}
			new WritableBookDescription(description).setEncoding(encoding);
			new WritableBookDescription(description).setLanguage(language);
		}
	}
	//Last working version
	/*public static void detectEncodingAndLanguage(BookDescription description, InputStream stream) {	
		String encoding = description.getEncoding();
		if (encoding.length() == 0) {
			encoding = EncodingDetector.detect(stream, PluginCollection.instance().DefaultLanguageOption.getValue());
			if (encoding == "unknown") {
				encoding = "windows-1252";
			}
			new WritableBookDescription(description).setEncoding(encoding);
		}

		if (description.getLanguage() == "") {
			if ((encoding.equals("US-ASCII")) ||
					(encoding.equals("ISO-8859-1"))) {
				new WritableBookDescription(description).setLanguage("en");
			} else if ((description.getEncoding().equals("KOI8-R")) ||
					(encoding.equals("windows-1251")) ||
					(encoding.equals("ISO-8859-5")) ||
					(encoding.equals("IBM866"))) {
				new WritableBookDescription(description).setLanguage("ru");
			} /*else if (
	                (PluginCollection.instance().DefaultLanguageOption.getValue() == EncodingDetector.Language.CZECH) &&
					((encoding == "windows-1250") ||
					 (encoding == "ISO-8859-2") ||
					 (encoding == "IBM852"))) {
				new WritableBookDescription(description).setLanguage("cs");
			}*/
		/*}

	}*/
	
	static class FormatInfoPage {
		protected FormatInfoPage() {}
	}
}
