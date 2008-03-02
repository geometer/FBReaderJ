package org.fbreader.fbreader;

import org.fbreader.collection.BookCollection;
import org.fbreader.description.BookDescription.BookInfo;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.dialogs.ZLOptionsDialog;

public class BookInfoDialog {
	private final ZLOptionsDialog myDialog;
	private final BookCollection myCollection;
	private BookInfo myBookInfo;
	
	public BookInfoDialog(BookCollection collection, String fileName) {
		myCollection = collection;
		myBookInfo = new BookInfo(fileName);
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("InfoDialog");
		
		ZLDialogContent commonTab = myDialog.createTab("Common");
		
		ZLDialogContent seriesTab = myDialog.createTab("Series");
		
	/*	ZLDialogContent &commonTab = myDialog->createTab(ZLResourceKey("Common"));
		commonTab.addOption(ZLResourceKey("file"), 
			new ZLStringInfoEntry(ZLFile::fileNameToUtf8(ZLFile(fileName).path()))
		);
		commonTab.addOption(ZLResourceKey("title"), myBookInfo.TitleOption);

		myAuthorDisplayNameEntry = new AuthorDisplayNameEntry(*this);
		myAuthorSortKeyEntry = new AuthorSortKeyEntry(*this);
		myEncodingEntry = new EncodingEntry(myBookInfo.EncodingOption);
		myEncodingSetEntry =
			(myEncodingEntry->initialValue() != "auto") ?
			new EncodingSetEntry(*(EncodingEntry*)myEncodingEntry) : 0;
		std::vector<std::string> languageCodes = ZLLanguageList::languageCodes();
		languageCodes.push_back("de-traditional");
		myLanguageEntry = new ZLLanguageOptionEntry(myBookInfo.LanguageOption, languageCodes);
		mySeriesTitleEntry = new SeriesTitleEntry(*this);
		myBookNumberEntry = new ZLSimpleSpinOptionEntry(myBookInfo.NumberInSequenceOption, 1);

		commonTab.addOption(ZLResourceKey("authorDisplayName"), myAuthorDisplayNameEntry);
		commonTab.addOption(ZLResourceKey("authorSortKey"), myAuthorSortKeyEntry);
		commonTab.addOption(ZLResourceKey("language"), myLanguageEntry);
		if (myEncodingSetEntry != 0) {
			commonTab.addOption(ZLResourceKey("encodingSet"), myEncodingSetEntry);
		}
		commonTab.addOption(ZLResourceKey("encoding"), myEncodingEntry);

		ZLDialogContent &seriesTab = myDialog->createTab(ZLResourceKey("Series"));
		seriesTab.addOption(ZLResourceKey("seriesTitle"), mySeriesTitleEntry);
		seriesTab.addOption(ZLResourceKey("bookNumber"), myBookNumberEntry);

		mySeriesTitleEntry->onValueEdited(mySeriesTitleEntry->initialValue());
		
		FormatPlugin *plugin = PluginCollection::instance().plugin(ZLFile(fileName), false);
		if (plugin != 0) {
			myFormatInfoPage = plugin->createInfoPage(*myDialog, fileName);
		}
		*/
	}

	public ZLOptionsDialog getDialog() {
		return myDialog;
	}
}
