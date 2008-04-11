package org.fbreader.optionsDialog;
import org.fbreader.encoding.ZLEncodingCollection;
import org.fbreader.encodingOption.*;
import org.fbreader.fbreader.*;
import org.fbreader.formats.FormatPlugin.PluginCollection;
import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.language.ZLLanguageList;
import org.zlibrary.core.optionEntries.*;
import org.zlibrary.core.options.*;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.text.view.style.*;

public class OptionsDialog {
	private ZLOptionsDialog myDialog;
	
	public OptionsDialog(FBReader fbreader) {
		ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().baseStyle();
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("OptionsDialog", null, new OptionsApplyRunnable(fbreader), true);

		ZLDialogContent libraryTab = myDialog.createTab("Library");
		CollectionView collectionView = fbreader.getCollectionView();
		libraryTab.addOption("bookPath", collectionView.getCollection().PathOption);
		libraryTab.addOption("lookInSubdirectories", collectionView.getCollection().ScanSubdirsOption);
		RecentBooksView recentBooksView = (RecentBooksView) fbreader.getRecentBooksView();
		libraryTab.addOption("recentListSize", new ZLSimpleSpinOptionEntry(recentBooksView.lastBooks().MaxListSizeOption, 1));
		//ZLToggleBooleanOptionEntry showTagsEntry = new ZLToggleBooleanOptionEntry(collectionView.ShowTagsOption);
		//ZLOptionEntry showAllBooksTagEntry = new ZLSimpleBooleanOptionEntry(collectionView.ShowAllBooksTagOption);
		//showTagsEntry.addDependentEntry(showAllBooksTagEntry);
		//libraryTab.addOption("showTags", showTagsEntry);
		//libraryTab.addOption("showAllBooksList", showAllBooksTagEntry);
		//showTagsEntry.onStateChanged(showTagsEntry.initialState());
		
		ZLDialogContent encodingTab = myDialog.createTab("Language");
		encodingTab.addOption("autoDetect", new ZLSimpleBooleanOptionEntry(PluginCollection.instance().LanguageAutoDetectOption));
		encodingTab.addOption("defaultLanguage", new ZLLanguageOptionEntry(PluginCollection.instance().DefaultLanguageOption, ZLLanguageList.languageCodes()));
//		EncodingEntry encodingEntry = new EncodingEntry(PluginCollection.instance().DefaultEncodingOption);
//		EncodingSetEntry encodingSetEntry = new EncodingSetEntry(encodingEntry);
//		encodingTab.addOption("defaultEncodingSet", encodingSetEntry);
//		encodingTab.addOption("defaultEncoding", encodingEntry);
		encodingTab.addOption("useWindows1252Hack", new ZLSimpleBooleanOptionEntry(ZLEncodingCollection.useWindows1252HackOption()));

		new ScrollingOptionsPage(myDialog.createTab("Scrolling"), fbreader);
				
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
		
		new FormatOptionsPage(myDialog.createTab("Format"));
			
		new StyleOptionsPage(myDialog.createTab("Styles"), fbreader.getContext());
		
		createIndicatorTab(fbreader);
		
		ZLDialogContent rotationTab = myDialog.createTab("Rotation");
		rotationTab.addOption("direction", new RotationTypeEntry(rotationTab.getResource("direction"), fbreader.RotationAngleOption));
		
		ZLDialogContent colorsTab = myDialog.createTab("Colors");
		final String colorKey = "colorFor";
		final ZLResource resource = colorsTab.getResource(colorKey);
		ZLColorOptionBuilder builder = new ZLColorOptionBuilder();
		final String BACKGROUND = resource.getResource("background").getValue();
		builder.addOption(BACKGROUND, baseStyle.BackgroundColorOption);
		builder.addOption(resource.getResource("selectionBackground").getValue(), baseStyle.SelectionBackgroundColorOption);
		builder.addOption(resource.getResource("text").getValue(), baseStyle.RegularTextColorOption);
		builder.addOption(resource.getResource("internalLink").getValue(), baseStyle.InternalHyperlinkTextColorOption);
		builder.addOption(resource.getResource("externalLink").getValue(), baseStyle.ExternalHyperlinkTextColorOption);
		builder.addOption(resource.getResource("highlighted").getValue(), baseStyle.SelectedTextColorOption);
		builder.addOption(resource.getResource("treeLines").getValue(), baseStyle.TreeLinesColorOption);
		builder.addOption(resource.getResource("indicator").getValue(), ((FBIndicatorInfo) fbreader.getCollectionView().getIndicatorInfo()).ColorOption);
		builder.setInitial(BACKGROUND);
		colorsTab.addOption(colorKey, builder.comboEntry());
		colorsTab.addOption("", builder.colorEntry());
		
		
		
		new KeyBindingsPage(fbreader, myDialog.createTab("Keys"));
		
		ZLDialogContent webTab = myDialog.createTab("Web");
		webTab.addOption("defaultText",
				new ZLToggleBooleanOptionEntry(fbreader.getBookTextView().OpenInBrowserOption));	
	}
	
	private void createIndicatorTab(FBReader fbreader) {
		ZLDialogContent indicatorTab = myDialog.createTab("Indicator");
		//TODO
		FBIndicatorInfo indicatorInfo = (FBIndicatorInfo) fbreader.getCollectionView().getIndicatorInfo();
		ZLToggleBooleanOptionEntry showIndicatorEntry =
			new ZLToggleBooleanOptionEntry(indicatorInfo.ShowOption);
		indicatorTab.addOption("show", showIndicatorEntry);

		ZLOptionEntry heightEntry =
			new ZLSimpleSpinOptionEntry(indicatorInfo.HeightOption, 1);
		ZLOptionEntry offsetEntry =
			new ZLSimpleSpinOptionEntry(indicatorInfo.OffsetOption, 1);
		indicatorTab.addOptions("height", heightEntry, "offset", offsetEntry);
		showIndicatorEntry.addDependentEntry(heightEntry);
		showIndicatorEntry.addDependentEntry(offsetEntry);

		//StateOptionEntry showTextPositionEntry =
		//	new StateOptionEntry(indicatorInfo.ShowTextPositionOption);
		//indicatorTab.addOption("pageNumber", showTextPositionEntry);
		//showIndicatorEntry.addDependentEntry(showTextPositionEntry);

		//StateOptionEntry showTimeEntry =
		//	new StateOptionEntry(indicatorInfo.ShowTimeOption);
		//indicatorTab.addOption("time", showTimeEntry);
		//showIndicatorEntry.addDependentEntry(showTimeEntry);

		//SpecialFontSizeEntry fontSizeEntry =
		//	new SpecialFontSizeEntry(indicatorInfo.FontSizeOption, 2, showTextPositionEntry, showTimeEntry);
		//indicatorTab.addOption("fontSize", fontSizeEntry);
		//showIndicatorEntry.addDependentEntry(fontSizeEntry);
		//showTextPositionEntry.addDependentEntry(fontSizeEntry);
		//showTimeEntry.addDependentEntry(fontSizeEntry);

		//ZLOptionEntry tocMarksEntry =
		//	new ZLSimpleBooleanOptionEntry(fbreader.getBookTextView().ShowTOCMarksOption);
		//indicatorTab.addOption("tocMarks", tocMarksEntry);
		//showIndicatorEntry.addDependentEntry(tocMarksEntry);

		ZLOptionEntry navigationEntry =
			new ZLSimpleBooleanOptionEntry(indicatorInfo.IsSensitiveOption);
		indicatorTab.addOption("navigation", navigationEntry);
		showIndicatorEntry.addDependentEntry(navigationEntry);

		showIndicatorEntry.onStateChanged(showIndicatorEntry.initialState());
		//showTextPositionEntry.onStateChanged(showTextPositionEntry.initialState());
		//showTimeEntry.onStateChanged(showTimeEntry.initialState());
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
	
	private static class StateOptionEntry extends ZLToggleBooleanOptionEntry {
		private boolean myState;
		
		public StateOptionEntry(ZLBooleanOption option) {
			super(option);
			myState = option.getValue();
		}
	
		public void onStateChanged(boolean state) {
			myState = state;
			super.onStateChanged(state);
		}
	}

	private static class SpecialFontSizeEntry extends ZLSimpleSpinOptionEntry {
		private StateOptionEntry myFirst;
		private StateOptionEntry mySecond;
		
		public SpecialFontSizeEntry(ZLIntegerRangeOption option, int step, StateOptionEntry first, StateOptionEntry second) {
			super(option, step);
			myFirst = first;
			mySecond = second;
		}

		public void setVisible(boolean state) {
			super.setVisible(
					(myFirst.isVisible() && myFirst.myState) ||
					(mySecond.isVisible() && mySecond.myState)
			);
		}	
	}
	
}
