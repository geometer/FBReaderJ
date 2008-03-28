package org.fbreader.fbreader;

import org.fbreader.option.FBOptions;
import org.zlibrary.core.options.*;
import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.text.view.ZLTextView;

class SearchAction extends FBAction {
	public static final String SEARCH = "Search";
	public static final String PATTERN = "Pattern";

	public ZLBooleanOption SearchBackwardOption;
	public ZLBooleanOption SearchIgnoreCaseOption;
	public ZLBooleanOption SearchInWholeTextOption;
	public ZLBooleanOption SearchThisSectionOnlyOption;
	public ZLStringOption SearchPatternOption;

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
		final ZLTextView textView = fbreader().getTextView();	
		final ZLDialog searchDialog = ZLDialogManager.getInstance().createDialog("textSearchDialog");
	
		searchDialog.addOption("text", new SearchPatternEntry(this));
		searchDialog.addOption("ignoreCase", SearchIgnoreCaseOption);
		searchDialog.addOption("wholeText", SearchInWholeTextOption);
		searchDialog.addOption("backward", SearchBackwardOption);
/*		if (textView.hasMultiSectionModel()) {
			searchDialog.addOption("currentSection", SearchThisSectionOnlyOption);
		}
*/
		searchDialog.addButton("go", new Runnable() {
			public void run() {
				searchDialog.acceptValues();
				final String pattern = SearchPatternOption.getValue();
				if (pattern.length() != 0) {
					textView.search(
						pattern,
						SearchIgnoreCaseOption.getValue(),
						SearchInWholeTextOption.getValue(),
						SearchBackwardOption.getValue(),
						SearchThisSectionOnlyOption.getValue()
					);
				}
			}
		});
		searchDialog.addButton(ZLDialogManager.CANCEL_BUTTON, null);
		
		searchDialog.run();
	}
}
