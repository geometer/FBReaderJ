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

package org.geometerplus.fbreader.description;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.*;

import org.geometerplus.fbreader.formats.*;
import org.geometerplus.fbreader.option.FBOptions;

public class BookDescription implements Comparable {
	public final String FileName;

	private Author myAuthor;
	private	String myTitle = "";
	private	String mySeriesName = "";
	private	int myNumberInSeries = 0;
	private	String myLanguage = "";
	private	String myEncoding = "";
	private final static HashMap ourDescriptions = new HashMap();
	
	private static final String EMPTY = "";
	private static final String UNKNOWN = "unknown";
	
	public static BookDescription getDescription(String fileName) {
		return getDescription(fileName, true); 
	} 
	
	public static BookDescription getDescription(String fileName, boolean checkFile) {
		if (fileName == null) {
			return null;
		}
		String physicalFileName = new ZLFile(fileName).getPhysicalFilePath();
		ZLFile file = new ZLFile(physicalFileName);
		if (checkFile && !file.exists()) {
			return null;
		}
		BookDescription description = (BookDescription)ourDescriptions.get(fileName);
		if (description == null) {
			description = new BookDescription(fileName);
			ourDescriptions.put(fileName, description);
		}
		if (!checkFile || BookDescriptionUtil.checkInfo(file)) {
			BookInfo info = new BookInfo(fileName);
			description.myAuthor = Author.SingleAuthor.create(info.AuthorDisplayNameOption.getValue(), info.AuthorSortKeyOption.getValue());
			description.myTitle = info.TitleOption.getValue();
			description.mySeriesName = info.SeriesNameOption.getValue();
			description.myNumberInSeries = info.NumberInSeriesOption.getValue();
			description.myLanguage = info.LanguageOption.getValue();
			description.myEncoding = info.EncodingOption.getValue();
			if (info.isFull()) {
				return description;
			}
		} else {
			if (physicalFileName != fileName) {
				BookDescriptionUtil.resetZipInfo(file);
			}
			BookDescriptionUtil.saveInfo(file);
		}
		ZLFile bookFile = new ZLFile(fileName);
		
		FormatPlugin plugin = PluginCollection.instance().getPlugin(bookFile, false);
		if ((plugin == null) || !plugin.readDescription(fileName, description)) {
			return null;
		}

		if (description.myTitle.length() == 0) {
			description.myTitle = bookFile.getName(true);
		}
		Author author = description.myAuthor;
		if (author == null || author.getDisplayName().length() == 0) {
			description.myAuthor = Author.SingleAuthor.create();
		}
		if (description.myEncoding.length() == 0) {
			description.myEncoding = "auto";
		}
		{
			BookInfo info = new BookInfo(fileName);
			info.AuthorDisplayNameOption.setValue(description.myAuthor.getDisplayName());
			info.AuthorSortKeyOption.setValue(description.myAuthor.getSortKey());
			info.TitleOption.setValue(description.myTitle);
			info.SeriesNameOption.setValue(description.mySeriesName);
			info.NumberInSeriesOption.setValue(description.myNumberInSeries);
			info.LanguageOption.setValue(description.myLanguage);
			info.EncodingOption.setValue(description.myEncoding);
		}
		return description;
	}


	private BookDescription(String fileName) {
		FileName = fileName;
		myAuthor = null;
		myNumberInSeries = 0;
	}

	public Author getAuthor() {
		return myAuthor;
	}
	
	public String getTitle() {
		return myTitle;
	}
	
	public String getSeriesName() {
		return mySeriesName;
	}
	
	public int getNumberInSeries() {
		return myNumberInSeries; 
	}
	
	public String getLanguage() {
		return myLanguage;
	}
	
	public String getEncoding() {
		return myEncoding;
	}
	
	
	
	public static class BookInfo {
		public BookInfo(String fileName) {
			AuthorDisplayNameOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "AuthorDisplayName", EMPTY);
			AuthorSortKeyOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "AuthorSortKey", EMPTY);
			TitleOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Title", EMPTY);
			SeriesNameOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Sequence", EMPTY);
			NumberInSeriesOption = new ZLIntegerRangeOption(FBOptions.BOOKS_CATEGORY, fileName, "Number in seq", 0, 100, 0);
			LanguageOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Language", UNKNOWN);
			EncodingOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, fileName, "Encoding", EMPTY);
		}
	
		public boolean isFull() {
			return
				(AuthorDisplayNameOption.getValue().length() != 0) &&
				(AuthorSortKeyOption.getValue().length() != 0) &&
				(TitleOption.getValue().length() != 0) &&
				(EncodingOption.getValue().length() != 0);
		}
		
		void reset() {
			AuthorDisplayNameOption.setValue(EMPTY);
			AuthorSortKeyOption.setValue(EMPTY);
			TitleOption.setValue(EMPTY);
			SeriesNameOption.setValue(EMPTY);
			NumberInSeriesOption.setValue(0);
			LanguageOption.setValue(UNKNOWN);
			EncodingOption.setValue(EMPTY);
		}

		public final ZLStringOption AuthorDisplayNameOption;
		public final ZLStringOption AuthorSortKeyOption;
		public final ZLStringOption TitleOption;
		public final ZLStringOption SeriesNameOption;
		public final ZLIntegerRangeOption NumberInSeriesOption;
		public final ZLStringOption LanguageOption;
		public final ZLStringOption EncodingOption;
	}

	public int compareTo(Object o) {
		final BookDescription d = (BookDescription)o;

		final Author a1 = getAuthor();
		final Author a2 = d.getAuthor();

		{
			final int result = a1.getSortKey().compareTo(a2.getSortKey());
			if (result != 0) {
				return result;
			}
		}

		{
			final int result = a1.getDisplayName().compareTo(a2.getDisplayName());
			if (result != 0) {
				return result;
			}
		}

		final String seriesName1 = getSeriesName();
		final String seriesName2 = d.getSeriesName();

		if ((seriesName1.length() == 0) && (seriesName2.length() == 0)) {
			return getTitle().compareTo(d.getTitle());
		}
		if (seriesName1.length() == 0) {
			return getTitle().compareTo(seriesName2);
		}
		if (seriesName2.length() == 0) {
			return seriesName1.compareTo(d.getTitle());
		}
		{
			final int result = seriesName1.compareTo(seriesName2);
			if (result != 0) {
				return result;
			}
		}
		return getNumberInSeries() - d.getNumberInSeries();
	}
	
	static public class WritableBookDescription  {
		private final BookDescription myDescription;
		
		public WritableBookDescription(BookDescription description) {
			myDescription = description;
		}
		
		public void addAuthor(String name) {
			addAuthor(name, "");
		}
		
		public void addAuthor(String name, String sortKey) {
			String strippedName = name;
			strippedName.trim();
			if (strippedName.length() == 0) {
				return;
			}

			String strippedKey = sortKey;
			strippedKey.trim();
			if (strippedKey.length() == 0) {
				int index = strippedName.indexOf(' ');
				if (index == -1) {
					strippedKey = strippedName;
				} else {
					strippedKey = strippedName.substring(index + 1);
					while ((index >= 0) && (strippedName.charAt(index) == ' ')) {
						--index;
					}
					strippedName = strippedName.substring(0, index + 1) + ' ' + strippedKey;
				}
			}
			Author author = Author.SingleAuthor.create(strippedName, strippedKey);
			
			if (myDescription.myAuthor == null) {
				myDescription.myAuthor = author;
			} else {
				if (myDescription.myAuthor.isSingle()) {
					myDescription.myAuthor = Author.MultiAuthor.create(myDescription.myAuthor);
				}
				((Author.MultiAuthor)myDescription.myAuthor).addAuthor(author);
			}
		}
		
		public void clearAuthor() {
			myDescription.myAuthor = null;
		}
		
		public Author getAuthor() {
			return myDescription.getAuthor();
		}
		
		public String getTitle() {
			return myDescription.myTitle;
		}
		
		public void setTitle(String title) {
			myDescription.myTitle = title;
		}
		
		public String getSeriesName() {
			return myDescription.mySeriesName;
		}
		
		public void setSeriesName(String sequenceName) {
			myDescription.mySeriesName = sequenceName;
		}
		
		public int getNumberInSeries() {
			return myDescription.myNumberInSeries;
		}
		
		public void setNumberInSeries(int numberInSeries) {
			myDescription.myNumberInSeries = numberInSeries;
		}
		
		public String getFileName() {
			return myDescription.FileName; 
		}
		
		public String getLanguage() {
			return myDescription.myLanguage;
		}
		
		public void setLanguage(String language) {
			this.myDescription.myLanguage = language;
		}
		
		public String getEncoding() {
			return myDescription.myEncoding;
		}
		
		public void setEncoding(String encoding) {
			this.myDescription.myEncoding = encoding;
		}
	};
}
