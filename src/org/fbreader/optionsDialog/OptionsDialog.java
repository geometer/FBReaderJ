package org.fbreader.optionsDialog;

import org.fbreader.fbreader.CollectionView;
import org.fbreader.fbreader.FBReader;
import org.fbreader.fbreader.FBView;
import org.fbreader.fbreader.RecentBooksView;
import org.fbreader.formats.FormatPlugin.PluginCollection;
import org.zlibrary.core.dialogs.ZLChoiceOptionEntry;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.optionEntries.ZLSimpleBooleanOptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleSpinOptionEntry;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.text.view.style.ZLTextBaseStyle;
import org.zlibrary.text.view.style.ZLTextStyleCollection;

public class OptionsDialog {
	private ZLOptionsDialog myDialog;
	
	public OptionsDialog(FBReader fbreader) {
		ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().baseStyle();
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("OptionsDialog", new OptionsApplyRunnable(fbreader), true);

		ZLDialogContent libraryTab = myDialog.createTab("Library");
		CollectionView collectionView = fbreader.getCollectionView();
		libraryTab.addOption("bookPath", collectionView.getCollection().PathOption);
		libraryTab.addOption("lookInSubdirectories", collectionView.getCollection().ScanSubdirsOption);
		RecentBooksView recentBooksView = (RecentBooksView) fbreader.getRecentBooksView();
		libraryTab.addOption("recentListSize", new ZLSimpleSpinOptionEntry(recentBooksView.lastBooks().MaxListSizeOption, 1));

		ZLDialogContent encodingTab = myDialog.createTab("Language");
		encodingTab.addOption("autoDetect", new ZLSimpleBooleanOptionEntry(PluginCollection.instance().LanguageAutoDetectOption));
		
		myDialog.createTab("Scrolling");
		
		ZLDialogContent selectionTab = myDialog.createTab("Selection");
		selectionTab.addOption("enableSelection", FBView.selectionOption());
		
		ZLDialogContent marginTab = myDialog.createTab("Margins");
		marginTab.addOptions(
			"left", new ZLSimpleSpinOptionEntry(FBView.getLeftMarginOption(), 1),
			"right", new ZLSimpleSpinOptionEntry(FBView.getRightMarginOption(), 1)
		);
		marginTab.addOptions(
			"top", new ZLSimpleSpinOptionEntry(FBView.getTopMarginOption(), 1),
			"bottom", new ZLSimpleSpinOptionEntry(FBView.getBottomMarginOption(), 1)
		);
		
		myDialog.createTab("Format");
		
		myDialog.createTab("Styles");
		
		ZLDialogContent rotationTab = myDialog.createTab("Rotation");
		rotationTab.addOption("direction", new RotationTypeEntry(rotationTab.getResource("direction"), fbreader.RotationAngleOption));
		
		ZLDialogContent colorsTab = myDialog.createTab("Colors");
		
		ZLDialogContent keysTab = myDialog.createTab("Keys");
		keysTab.addOption("keyDelay", new ZLSimpleSpinOptionEntry(fbreader.KeyDelayOption, 50));
		
		myDialog.createTab("Config");
		
		/*
		
		encodingTab.addOption(ZLResourceKey("defaultLanguage"), new ZLLanguageOptionEntry(PluginCollection::instance().DefaultLanguageOption, ZLLanguageList::languageCodes()));
		EncodingEntry *encodingEntry = new EncodingEntry(PluginCollection::instance().DefaultEncodingOption);
		EncodingSetEntry *encodingSetEntry = new EncodingSetEntry(*encodingEntry);
		encodingTab.addOption(ZLResourceKey("defaultEncodingSet"), encodingSetEntry);
		encodingTab.addOption(ZLResourceKey("defaultEncoding"), encodingEntry);
		encodingTab.addOption(ZLResourceKey("useWindows1252Hack"), new ZLSimpleBooleanOptionEntry(ZLEncodingCollection::useWindows1252HackOption()));
		encodingTab.addOption(ZLResourceKey("chineseBreakAtAnyPosition"), new ZLSimpleBooleanOptionEntry(ZLChineseBreakingAlgorithm::instance().AnyPositionBreakingOption));

		myScrollingPage = new ScrollingOptionsPage(myDialog->createTab(ZLResourceKey("Scrolling")), fbreader);

		myFormatPage = new FormatOptionsPage(myDialog->createTab(ZLResourceKey("Format")));
		myStylePage = new StyleOptionsPage(myDialog->createTab(ZLResourceKey("Styles")), *fbreader.context());

		createIndicatorTab(fbreader);

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
	
	private static class OptionsApplyRunnable implements Runnable {
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
	
	private static class RotationTypeEntry extends ZLChoiceOptionEntry {
		private final ZLResource myResource;
		private ZLIntegerOption myAngleOption;
		
		public RotationTypeEntry(ZLResource resource, ZLIntegerOption angleOption) {
			myAngleOption = angleOption;
			myResource = resource;
		}
		
		public int choiceNumber() {
			return 5;
		}

		public String getText(int index) {
			final String keyName;
			switch (index) {
				case 1:
					keyName = "counterclockwise";
					break;
				case 2:
					keyName = "180";
					break;
				case 3:
					keyName = "clockwise";
					break;
				case 4:
					keyName = "cycle";
					break;
				default:
					keyName = "disabled";
					break;
			}
			return myResource.getResource(keyName).getValue();
		}

		public int initialCheckedIndex() {
			switch (myAngleOption.getValue()) {
			default:
				return 0;
			case ZLViewWidget.Angle.DEGREES90:
				return 1;
			case ZLViewWidget.Angle.DEGREES180:
				return 2;
			case ZLViewWidget.Angle.DEGREES270:
				return 3;
			case -1:
				return 4;
			}
		}

		public void onAccept(int index) {
			int angle = ZLViewWidget.Angle.DEGREES0;
			switch (index) {
				case 1:
					angle = ZLViewWidget.Angle.DEGREES90;
					break;
				case 2:
					angle = ZLViewWidget.Angle.DEGREES180;
					break;
				case 3:
					angle = ZLViewWidget.Angle.DEGREES270;
					break;
				case 4:
					angle = -1;
					break;
			}
			myAngleOption.setValue(angle);
		}	
	}
}
