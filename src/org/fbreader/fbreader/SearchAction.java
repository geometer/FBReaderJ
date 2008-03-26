package org.fbreader.fbreader;

import org.fbreader.option.FBOptions;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.dialogs.ZLDialog;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.text.view.ZLTextView;

class SearchAction extends FBAction {
	
	public static final String SEARCH = "Search";
	public static final String PATTERN = "Pattern";

	private ZLBooleanOption SearchBackwardOption;
	private ZLBooleanOption SearchIgnoreCaseOption;
	private ZLBooleanOption SearchInWholeTextOption;
	private ZLBooleanOption SearchThisSectionOnlyOption;
	private ZLStringOption SearchPatternOption;

	SearchAction(FBReader fbreader) {
		super(fbreader);
		SearchBackwardOption = new ZLBooleanOption(FBOptions.SEARCH_CATEGORY, SEARCH, "Backward", false);
		SearchIgnoreCaseOption = new ZLBooleanOption(FBOptions.SEARCH_CATEGORY, SEARCH, "IgnoreCase", true);
		SearchInWholeTextOption = new ZLBooleanOption(FBOptions.SEARCH_CATEGORY, SEARCH, "WholeText", false);
		SearchThisSectionOnlyOption = new ZLBooleanOption(FBOptions.SEARCH_CATEGORY, SEARCH, "ThisSectionOnly", false);
		SearchPatternOption = new ZLStringOption(FBOptions.SEARCH_CATEGORY, SEARCH, PATTERN, "");	
	}

	public boolean isVisible() {
		return true; 
	}

	public boolean isEnabled() {
		return true;
	}

	public void run() {
//		System.err.println("SearchAction running...");
		
		ZLTextView textView = fbreader().getTextView();	
	
		ZLDialog searchDialog = ZLDialogManager.getInstance().createDialog("textSearchDialog");
	
//		searchDialog.addOption("text", new SearchPatternEntry(this));
		searchDialog.addOption("ignoreCase", SearchIgnoreCaseOption);
		searchDialog.addOption("wholeText", SearchInWholeTextOption);
		searchDialog.addOption("backward", SearchBackwardOption);
/*		if (textView.hasMultiSectionModel()) {
			searchDialog.addOption("currentSection", SearchThisSectionOnlyOption);
		}
*/
		searchDialog.addButton("go", true);
		searchDialog.addButton(ZLDialogManager.CANCEL_BUTTON, false);
		
		if (searchDialog.run()) {
			searchDialog.acceptValues();
			SearchPatternOption.setValue("FBReader");
			textView.search(
				SearchPatternOption.getValue(),
				SearchIgnoreCaseOption.getValue(),
				SearchInWholeTextOption.getValue(),
				SearchBackwardOption.getValue(),
				SearchThisSectionOnlyOption.getValue()
			);
		}
	}
}
