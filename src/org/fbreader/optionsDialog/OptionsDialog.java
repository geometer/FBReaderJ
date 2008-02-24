package org.fbreader.optionsDialog;

import org.fbreader.fbreader.FBReader;
import org.fbreader.fbreader.FBView;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.runnable.ZLRunnable;
import org.zlibrary.text.view.style.ZLTextBaseStyle;
import org.zlibrary.text.view.style.ZLTextStyleCollection;

public class OptionsDialog {
	private ZLOptionsDialog myDialog;
	
	public OptionsDialog(FBReader fbreader) {
		ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().baseStyle();
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("OptionsDialog", new OptionsApplyRunnable(fbreader), true);

		ZLDialogContent generalTab = myDialog.createTab("General");

		ZLDialogContent encodingTab = myDialog.createTab("Language");
		
		myDialog.createTab("Scrolling");
		
		ZLDialogContent selectionTab = myDialog.createTab("Selection");
		selectionTab.addOption("enableSelection", FBView.selectionOption());
		
		ZLDialogContent marginTab = myDialog.createTab("Margins");
		
		myDialog.createTab("Format");
		
		myDialog.createTab("Styles");
		
		ZLDialogContent rotationTab = myDialog.createTab("Rotation");
		
		ZLDialogContent colorsTab = myDialog.createTab("Colors");
		
		myDialog.createTab("Keys");
		
		myDialog.createTab("Config");
		
		/*
		ZLDialogContent &generalTab = myDialog->createTab(ZLResourceKey("General"));
		CollectionView &collectionView = (CollectionView&)*fbreader.myCollectionView;
		generalTab.addOption(ZLResourceKey("bookPath"), collectionView.collection().PathOption);
		generalTab.addOption(ZLResourceKey("lookInSubdirectories"), collectionView.collection().ScanSubdirsOption);
		RecentBooksView &recentBooksView = (RecentBooksView&)*fbreader.myRecentBooksView;
		generalTab.addOption(ZLResourceKey("recentListSize"), new ZLSimpleSpinOptionEntry(recentBooksView.lastBooks().MaxListSizeOption, 1));
		generalTab.addOption(ZLResourceKey("keyDelay"), new ZLSimpleSpinOptionEntry(fbreader.KeyDelayOption, 50));

		ZLDialogContent &encodingTab = myDialog->createTab(ZLResourceKey("Language"));
		encodingTab.addOption(ZLResourceKey("autoDetect"), new ZLSimpleBooleanOptionEntry(PluginCollection::instance().LanguageAutoDetectOption));
		encodingTab.addOption(ZLResourceKey("defaultLanguage"), new ZLLanguageOptionEntry(PluginCollection::instance().DefaultLanguageOption, ZLLanguageList::languageCodes()));
		EncodingEntry *encodingEntry = new EncodingEntry(PluginCollection::instance().DefaultEncodingOption);
		EncodingSetEntry *encodingSetEntry = new EncodingSetEntry(*encodingEntry);
		encodingTab.addOption(ZLResourceKey("defaultEncodingSet"), encodingSetEntry);
		encodingTab.addOption(ZLResourceKey("defaultEncoding"), encodingEntry);
		encodingTab.addOption(ZLResourceKey("useWindows1252Hack"), new ZLSimpleBooleanOptionEntry(ZLEncodingCollection::useWindows1252HackOption()));
		encodingTab.addOption(ZLResourceKey("chineseBreakAtAnyPosition"), new ZLSimpleBooleanOptionEntry(ZLChineseBreakingAlgorithm::instance().AnyPositionBreakingOption));

		myScrollingPage = new ScrollingOptionsPage(myDialog->createTab(ZLResourceKey("Scrolling")), fbreader);

		ZLDialogContent &selectionTab = myDialog->createTab(ZLResourceKey("Selection"));
		selectionTab.addOption(ZLResourceKey("enableSelection"), FBView::selectionOption());

		ZLDialogContent &marginTab = myDialog->createTab(ZLResourceKey("Margins"));
		FBMargins &margins = FBView::margins();
		marginTab.addOptions(
			ZLResourceKey("left"), new ZLSimpleSpinOptionEntry(margins.LeftMarginOption, 1),
			ZLResourceKey("right"), new ZLSimpleSpinOptionEntry(margins.RightMarginOption, 1)
		);
		marginTab.addOptions(
			ZLResourceKey("top"), new ZLSimpleSpinOptionEntry(margins.TopMarginOption, 1),
			ZLResourceKey("bottom"), new ZLSimpleSpinOptionEntry(margins.BottomMarginOption, 1)
		);

		myFormatPage = new FormatOptionsPage(myDialog->createTab(ZLResourceKey("Format")));
		myStylePage = new StyleOptionsPage(myDialog->createTab(ZLResourceKey("Styles")), *fbreader.context());

		createIndicatorTab(fbreader);

		ZLDialogContent &rotationTab = myDialog->createTab(ZLResourceKey("Rotation"));
		ZLResourceKey directionKey("direction");
		rotationTab.addOption(directionKey, new RotationTypeEntry(rotationTab.resource(directionKey), fbreader.RotationAngleOption));

		ZLDialogContent &colorsTab = myDialog->createTab(ZLResourceKey("Colors"));
		ZLResourceKey colorKey("colorFor");
		const ZLResource &resource = colorsTab.resource(colorKey);
		ZLColorOptionBuilder builder;
		const std::string BACKGROUND = resource["background"].value();
		builder.addOption(BACKGROUND, baseStyle.BackgroundColorOption);
		builder.addOption(resource["selectionBackground"].value(), baseStyle.SelectionBackgroundColorOption);
		builder.addOption(resource["text"].value(), baseStyle.RegularTextColorOption);
		builder.addOption(resource["internalLink"].value(), baseStyle.InternalHyperlinkTextColorOption);
		builder.addOption(resource["externalLink"].value(), baseStyle.ExternalHyperlinkTextColorOption);
		builder.addOption(resource["highlighted"].value(), baseStyle.SelectedTextColorOption);
		builder.addOption(resource["treeLines"].value(), baseStyle.TreeLinesColorOption);
		builder.addOption(resource["indicator"].value(), (FBView::commonIndicatorInfo().ColorOption));
		builder.setInitial(BACKGROUND);
		colorsTab.addOption(colorKey, builder.comboEntry());
		colorsTab.addOption("", "", builder.colorEntry());

		myKeyBindingsPage = new KeyBindingsPage(fbreader, myDialog->createTab(ZLResourceKey("Keys")));
		if (ZLOption::isAutoSavingSupported()) {
			myConfigPage = new ConfigPage(fbreader, myDialog->createTab(ZLResourceKey("Config")));
		}

		std::vector<std::pair<ZLResourceKey,ZLOptionEntry*> > additional;
		additional.push_back(std::pair<ZLResourceKey,ZLOptionEntry*>(
			ZLResourceKey("singleClickOpen"),
			new ZLSimpleBooleanOptionEntry(fbreader.EnableSingleClickDictionaryOption)
		));
		createIntegrationTab(fbreader.dictionaryCollection(), ZLResourceKey("Dictionary"), additional);
		additional.clear();
		createIntegrationTab(fbreader.webBrowserCollection(), ZLResourceKey("Web"), additional);

		myDialog->createPlatformDependentTabs();
		*/
	}
	
	public ZLOptionsDialog getDialog() {
		return myDialog;
	}
	
	private static class OptionsApplyRunnable implements ZLRunnable {
		private final FBReader myFBReader;
		
		public OptionsApplyRunnable(FBReader fbreader) {
			myFBReader = fbreader;
		}
		
		public void run() {
			myFBReader.grabAllKeys(myFBReader.KeyboardControlOption.getValue());
			myFBReader.clearTextCaches();
			myFBReader.getCollectionView().synchronizeModel();
			myFBReader.refreshWindow();
		}
	}
}
