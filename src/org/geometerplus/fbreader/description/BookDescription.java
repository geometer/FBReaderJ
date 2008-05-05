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
	private static final String EMPTY = "";
	private static final String UNKNOWN = "unknown";

	public final String FileName;

	private Author myAuthor;
	private	String myTitle = "";
	private	String mySeriesName = "";
	private	int myNumberInSeries = 0;
	private	String myLanguage = "";
	private	String myEncoding = "";
	private final static HashMap ourDescriptions = new HashMap();
	private final ArrayList myTags = new ArrayList();

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
			description.myTags.clear();
			final String tagList = info.TagsOption.getValue();
			if (tagList.length() > 0) {
			int index = 0;
			do {
				final int newIndex = tagList.indexOf(',', index);
				final String tagName = (newIndex == -1) ? tagList.substring(index) : tagList.substring(index, newIndex);
				description.addTag(tagName, true);
				index = newIndex + 1;
			} while (index != 0);
		}

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

	public ArrayList getTags() {
		return myTags;
	}

  private boolean addTag(String tag, boolean check) {
		if (check) {
			tag = BookDescriptionUtil.removeWhiteSpacesFromTag(tag);
		}

		if ((tag.length() > 0) && !myTags.contains(tag)) {
			myTags.add(tag);
			return true;
		}
		return false;
	}

  private void saveTags() {
		saveTags(new ZLStringOption(FBOptions.BOOKS_CATEGORY, FileName, "TagList", EMPTY));
	}

  private void saveTags(ZLStringOption tagsOption) {
		final ArrayList tags = myTags;
		if (tags.isEmpty()) {
			tagsOption.setValue("");
		} else {
			final StringBuilder tagString = new StringBuilder();
			tagString.append(tags.get(0));
			final	int len = tags.size();
			for (int i = 1; i < len; ++i) {
				tagString.append(",");
				tagString.append(tags.get(i));
			}
  		tagsOption.setValue(tagString.toString());
    }
	}

	public static class BookInfo {
		public final ZLStringOption AuthorDisplayNameOption;
		public final ZLStringOption AuthorSortKeyOption;
		public final ZLStringOption TitleOption;
		public final ZLStringOption SeriesNameOption;
		public final ZLIntegerRangeOption NumberInSeriesOption;
		public final ZLStringOption LanguageOption;
		public final ZLStringOption EncodingOption;
		public final ZLStringOption TagsOption;

		public BookInfo(String fileName) {
			final String category = FBOptions.BOOKS_CATEGORY;
			AuthorDisplayNameOption = new ZLStringOption(category, fileName, "AuthorDisplayName", EMPTY);
			AuthorSortKeyOption = new ZLStringOption(category, fileName, "AuthorSortKey", EMPTY);
			TitleOption = new ZLStringOption(category, fileName, "Title", EMPTY);
			SeriesNameOption = new ZLStringOption(category, fileName, "Sequence", EMPTY);
			NumberInSeriesOption = new ZLIntegerRangeOption(category, fileName, "Number in seq", 0, 100, 0);
			LanguageOption = new ZLStringOption(category, fileName, "Language", UNKNOWN);
			EncodingOption = new ZLStringOption(category, fileName, "Encoding", EMPTY);
			TagsOption = new ZLStringOption(category, fileName, "TagList", EMPTY);
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
			TagsOption.setValue(EMPTY);
		}
	}

	public int compareTo(Object o) {
		final BookDescription d = (BookDescription)o;

		{
			final int result = getAuthor().compareTo(d.getAuthor());
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
			myDescription.myLanguage = language;
		}

		public String getEncoding() {
			return myDescription.myEncoding;
		}

		public void setEncoding(String encoding) {
			myDescription.myEncoding = encoding;
		}

		public void addTag(String tag, boolean check) {
			if (myDescription.addTag(tag, check)) {
				myDescription.saveTags();
			}
		}

		public void removeTag(String tag, boolean includeSubTags) {
			final ArrayList tags = myDescription.myTags;
			if (includeSubTags) {
				final String prefix = tag + '/';
				boolean changed = false;
				final int len = tags.size();
				ArrayList toRemove = null;
				for (int i = 0; i < len; ++i) {
					String current = (String)tags.get(i);
					if (current.equals(tag) || current.startsWith(prefix)) {
						if (toRemove == null) {
							toRemove = new ArrayList();
						}
						toRemove.add(i);
					}
				}
				if (toRemove != null) {
					for (int i = toRemove.size() - 1; i >= 0; --i) {
						tags.remove(((Integer)toRemove.get(i)).intValue());
					}
					myDescription.saveTags();
				}
			} else {
				if (tags.remove(tag)) {
					myDescription.saveTags();
				}
			}
		}

		public void renameTag(String from, String to, boolean includeSubTags) {
			final ArrayList tags = myDescription.myTags;
			if (includeSubTags) {
				final String prefix = from + '/';
				final HashSet tagSet = new HashSet();
				boolean changed = false;
				final int len = tags.size();
				for (int i = 0; i < len; ++i) {
					final String value = (String)tags.get(i);
					if (from.equals(value)) {
						tagSet.add(to);
						changed = true;
					} else if (value.startsWith(prefix)) {
						tagSet.add(to + '/' + value.substring(prefix.length()));
						changed = true;
					} else {
						tagSet.add(value);
					}
				}
				if (changed) {
					tags.clear();
					tags.addAll(tagSet);
					myDescription.saveTags();
				}
			} else {
				if (tags.remove(from) && !tags.contains(to)) {
					tags.add(to);
					myDescription.saveTags();
				}
			}
		}

		public void cloneTag(String from, String to, boolean includeSubTags) {
			final ArrayList tags = myDescription.myTags;
			if (includeSubTags) {
				final String prefix = from + '/';
				final HashSet tagSet = new HashSet();
				final int len = tags.size();
				for (int i = 0; i < len; ++i) {
					final String value = (String)tags.get(i);
					if (value.equals(from)) {
						tagSet.add(to);
					} else if (value.startsWith(prefix)) {
						tagSet.add(to + '/' + value.substring(prefix.length()));
					}
				}
				if (!tagSet.isEmpty()) {
					tagSet.addAll(tags);
					tags.clear();
					tags.addAll(tagSet);
					myDescription.saveTags();
				}
			} else {
				if (tags.contains(from) && !tags.contains(to)) {
					tags.add(to);
					myDescription.saveTags();
				}
			}
		}

		public void removeAllTags() {
			myDescription.myTags.clear();
		}
	};
}
