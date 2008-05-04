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

package org.geometerplus.fbreader.fbreader;

import java.util.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.language.ZLLanguageList;
import org.geometerplus.zlibrary.core.optionEntries.*;

import org.geometerplus.fbreader.collection.BookCollection;
import org.geometerplus.fbreader.description.*;
import org.geometerplus.fbreader.formats.*;
import org.geometerplus.fbreader.encodingOption.*;
import org.geometerplus.fbreader.encodingOption.EncodingEntry;

public class BookInfoDialog {
	private final ZLOptionsDialog myDialog;
	private final BookCollection myCollection;
	private final BookDescription.BookInfo myBookInfo;
//	private FormatInfoPage myFormatInfoPage;
	
	private AuthorDisplayNameEntry myAuthorDisplayNameEntry;
	private AuthorSortKeyEntry myAuthorSortKeyEntry;
	private ZLComboOptionEntry myEncodingSetEntry;
	private ZLComboOptionEntry myEncodingEntry;
	private ZLComboOptionEntry myLanguageEntry;
	private SeriesTitleEntry mySeriesTitleEntry;
	private ZLSpinOptionEntry myBookNumberEntry;
	//private FormatInfoPage myFormatInfoPage;
	
	public BookInfoDialog(BookCollection collection, String fileName, Runnable actionOnAccept) {
		myCollection = collection;
		myBookInfo = new BookDescription.BookInfo(fileName);
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("InfoDialog", actionOnAccept, null, false);
		
		ZLDialogContent commonTab = myDialog.createTab("Common");
		commonTab.addOption("file", new ZLStringInfoEntry(new ZLFile(fileName).getPath()));
		commonTab.addOption("title", myBookInfo.TitleOption);
		
		myAuthorDisplayNameEntry = new AuthorDisplayNameEntry();
		myAuthorSortKeyEntry = new AuthorSortKeyEntry();
		myEncodingEntry = new EncodingEntry(myBookInfo.EncodingOption);
		myEncodingSetEntry =
			(!"auto".equals(myEncodingEntry.initialValue())) ?
			new EncodingSetEntry((EncodingEntry)myEncodingEntry) : null;
		ArrayList/*<std::string>*/ languageCodes = ZLLanguageList.languageCodes();
		languageCodes.add("de-traditional");
		myLanguageEntry = new ZLLanguageOptionEntry(myBookInfo.LanguageOption, languageCodes);
	
		mySeriesTitleEntry = new SeriesTitleEntry();
		myBookNumberEntry = new ZLSimpleSpinOptionEntry(myBookInfo.NumberInSeriesOption, 1);

		commonTab.addOption("authorDisplayName", myAuthorDisplayNameEntry);
		commonTab.addOption("authorSortKey", myAuthorSortKeyEntry);
		commonTab.addOption("language", myLanguageEntry);
		if (myEncodingSetEntry != null) {
			commonTab.addOption("encodingSet", myEncodingSetEntry);
		}
		commonTab.addOption("encoding", myEncodingEntry);

		ZLDialogContent seriesTab = myDialog.createTab("Series");
		seriesTab.addOption("seriesTitle", mySeriesTitleEntry);
		seriesTab.addOption("bookNumber", myBookNumberEntry);
		mySeriesTitleEntry.onValueEdited(mySeriesTitleEntry.initialValue());

		ZLDialogContent tagsTab = myDialog.createTab("Tags");
		tagsTab.addOption("tags", myBookInfo.TagsOption);
		
		//FormatPlugin plugin = PluginCollection.instance().getPlugin(new ZLFile(fileName), false);
		//if (plugin != null) {
			//myFormatInfoPage = plugin.createInfoPage(myDialog, fileName);
		//}
		
	}

	public ZLOptionsDialog getDialog() {
		return myDialog;
	}
	
	private class AuthorSortKeyEntry extends ZLStringOptionEntry {
		public String initialValue() {
			Author currentAuthor = myAuthorDisplayNameEntry.myCurrentAuthor;
			return currentAuthor == null ?
				myBookInfo.AuthorSortKeyOption.getValue() :
				currentAuthor.getSortKey();
		}

		public void onAccept(String value) {
			myBookInfo.AuthorSortKeyOption.setValue(value);
		}	
	}
	
	private class AuthorDisplayNameEntry extends ZLComboOptionEntry {
		private final ArrayList myValues = new ArrayList();
		private	Author myCurrentAuthor;
		
		public AuthorDisplayNameEntry() {
			super(true);
		}

		public ArrayList getValues() {
			if (myValues.size() == 0) {
				final String initial = initialValue();
				boolean addInitial = true;
				final ArrayList authors = myCollection.authors();
				for (int i = 0; i < authors.size(); i++) {
					final String name = ((Author) authors.get(i)).getDisplayName(); 
					if (addInitial && (name != null && name.equals(initial))) {
						addInitial = false;
					}
					myValues.add(name);
				}
				if (addInitial) {
					myValues.add(initial);
				}
			}
			return myValues;
		}

		public String initialValue() {
			return myBookInfo.AuthorDisplayNameOption.getValue();
		}

		public void onAccept(String value) {
			myBookInfo.AuthorDisplayNameOption.setValue(value);
		}

		public void onValueSelected(int index) {
			final ArrayList authors = myCollection.authors();
			if (index < authors.size()) {
				myCurrentAuthor = (Author)authors.get(index);
			}
			myAuthorSortKeyEntry.resetView();
			mySeriesTitleEntry.resetView();
		}	
	}
	
	private class SeriesTitleEntry extends ZLComboOptionEntry {
		private final ArrayList myValues = new ArrayList();
		private	Author myOriginalAuthor;
		
		public SeriesTitleEntry() {
			super(true);
			final ArrayList authors = myCollection.authors();
			final String authorName = myBookInfo.AuthorDisplayNameOption.getValue();
			final String authorKey = myBookInfo.AuthorSortKeyOption.getValue();
			for (int i = 0; i < authors.size(); i++) {
				final Author author = (Author) authors.get(i);
				if ((authorName != null && authorName.equals(author.getDisplayName())) &&
						(authorKey != null && authorKey.equals(author.getSortKey()))) {
					myOriginalAuthor = author;
					break;
				}
			}
		}
		
		public boolean useOnValueEdited() {
			return true;
		}
		
		public void onValueEdited(String value) {
			myBookNumberEntry.setVisible(value != null && !value.equals(""));;
		}
		
		public void onValueSelected(int index) {
			myBookNumberEntry.setVisible(index != 0);
		}

		public ArrayList getValues() {
			myValues.clear();
			HashSet valuesSet = new HashSet();
			valuesSet.add(initialValue());
			valuesSet.add("");
			if (myOriginalAuthor != null) {
				myCollection.collectSeriesNames(myOriginalAuthor, valuesSet);
			}
			Author currentAuthor = myAuthorDisplayNameEntry.myCurrentAuthor;
			if (currentAuthor != null && (currentAuthor != myOriginalAuthor)) {
				myCollection.collectSeriesNames(currentAuthor, valuesSet);
			}
			myValues.addAll(valuesSet);
			return myValues;
		}

		public String initialValue() {
			return myBookInfo.SeriesNameOption.getValue();
		}

		public void onAccept(String value) {
			myBookInfo.SeriesNameOption.setValue(value);
		}
	}
}
