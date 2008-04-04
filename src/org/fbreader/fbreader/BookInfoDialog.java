package org.fbreader.fbreader;

import java.util.*;

import org.fbreader.collection.BookCollection;
import org.fbreader.description.*;
import org.fbreader.formats.FormatPlugin;
import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.language.ZLLanguageList;
import org.zlibrary.core.optionEntries.*;

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
		commonTab.addOption("title", myBookInfo.getTitleOption());
		
		myAuthorDisplayNameEntry = new AuthorDisplayNameEntry(this);
		myAuthorSortKeyEntry = new AuthorSortKeyEntry(this);
		//myEncodingEntry = new EncodingEntry(myBookInfo.EncodingOption);
		//myEncodingSetEntry =
		//	(myEncodingEntry.initialValue() != "auto") ?
		//	new EncodingSetEntry((EncodingEntry)myEncodingEntry) : null;
		ArrayList/*<std::string>*/ languageCodes = ZLLanguageList.languageCodes();
		languageCodes.add("de-traditional");
		myLanguageEntry = new ZLLanguageOptionEntry(myBookInfo.LanguageOption, languageCodes);
	
		mySeriesTitleEntry = new SeriesTitleEntry(this);
		myBookNumberEntry = new ZLSimpleSpinOptionEntry(myBookInfo.getNumberInSequenceOption(), 1);

		commonTab.addOption("authorDisplayName", myAuthorDisplayNameEntry);
		commonTab.addOption("authorSortKey", myAuthorSortKeyEntry);
		commonTab.addOption("language", myLanguageEntry);
		if (myEncodingSetEntry != null) {
			commonTab.addOption("encodingSet", myEncodingSetEntry);
		}
//		commonTab.addOption("encoding", myEncodingEntry);

		ZLDialogContent seriesTab = myDialog.createTab("Series");
		
		seriesTab.addOption("seriesTitle", mySeriesTitleEntry);
		seriesTab.addOption("bookNumber", myBookNumberEntry);

		mySeriesTitleEntry.onValueEdited(mySeriesTitleEntry.initialValue());
		
		
		FormatPlugin plugin = FormatPlugin.PluginCollection.instance().getPlugin(new ZLFile(fileName), false);
		//if (plugin != null) {
			//myFormatInfoPage = plugin.createInfoPage(myDialog, fileName);
		//}
		
	}

	public ZLOptionsDialog getDialog() {
		return myDialog;
	}
	
	private static class AuthorSortKeyEntry extends ZLStringOptionEntry {
		private final BookInfoDialog myInfoDialog;
		
		public AuthorSortKeyEntry(BookInfoDialog dialog) {
			myInfoDialog = dialog;
		}

		public String initialValue() {
			Author currentAuthor = myInfoDialog.myAuthorDisplayNameEntry.myCurrentAuthor;
			return currentAuthor == null ?
				myInfoDialog.myBookInfo.getAuthorSortKeyOption().getValue() :
				currentAuthor.getSortKey();
		}

		public void onAccept(String value) {
			myInfoDialog.myBookInfo.getAuthorSortKeyOption().setValue(value);
		}	
	}
	
	private static class AuthorDisplayNameEntry extends ZLComboOptionEntry {
		private final BookInfoDialog myInfoDialog;
		private final ArrayList/*String*/ myValues = new ArrayList();
		private	Author myCurrentAuthor;
		
		public AuthorDisplayNameEntry(BookInfoDialog dialog) {
			super(true);
			myInfoDialog = dialog;
		}

		public ArrayList getValues() {
			if (myValues.size() == 0) {
				final String initial = initialValue();
				boolean addInitial = true;
				final ArrayList authors = myInfoDialog.myCollection.authors();
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
			return myInfoDialog.myBookInfo.getAuthorDisplayNameOption().getValue();
		}

		public void onAccept(String value) {
			myInfoDialog.myBookInfo.getAuthorDisplayNameOption().setValue(value);
		}

		public void onValueSelected(int index) {
			final ArrayList authors = myInfoDialog.myCollection.authors();
			myCurrentAuthor = (Author) authors.get(index);
			myInfoDialog.myAuthorSortKeyEntry.resetView();
			myInfoDialog.mySeriesTitleEntry.resetView();
		}	
	}
	
	private static class SeriesTitleEntry extends ZLComboOptionEntry {
		private final BookInfoDialog myInfoDialog;
		private final ArrayList/*String*/ myValues = new ArrayList();
		private	Author myOriginalAuthor;
		
		public SeriesTitleEntry(BookInfoDialog dialog) {
			super(true);
			myInfoDialog = dialog;
			final ArrayList authors = myInfoDialog.myCollection.authors();
			final String authorName = myInfoDialog.myBookInfo.getAuthorDisplayNameOption().getValue();
			final String authorKey = myInfoDialog.myBookInfo.getAuthorSortKeyOption().getValue();
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
			myInfoDialog.myBookNumberEntry.setVisible(value != null && !value.equals(""));;
		}
		
		public void onValueSelected(int index) {
			myInfoDialog.myBookNumberEntry.setVisible(index != 0);
		}

		public ArrayList getValues() {
			myValues.clear();
			HashSet valuesSet = new HashSet();
			valuesSet.add(initialValue());
			valuesSet.add("");
			if (myOriginalAuthor != null) {
				final ArrayList books = myInfoDialog.myCollection.books(myOriginalAuthor);
				for (int i = 0; i < books.size(); i++) {
					valuesSet.add(((BookDescription) books.get(i)).getSequenceName());
				}
			}
			Author currentAuthor = myInfoDialog.myAuthorDisplayNameEntry.myCurrentAuthor;
			if (currentAuthor != null && (currentAuthor != myOriginalAuthor)) {
				final ArrayList books = myInfoDialog.myCollection.books(currentAuthor);
				for (int i = 0; i < books.size(); i++) {
					valuesSet.add(((BookDescription) books.get(i)).getSequenceName());
				}
			}
			myValues.addAll(valuesSet);
			return myValues;
		}

		public String initialValue() {
			return myInfoDialog.myBookInfo.getSequenceNameOption().getValue();
		}

		public void onAccept(String value) {
			myInfoDialog.myBookInfo.getSequenceNameOption().setValue(value);
		}
	}
}
