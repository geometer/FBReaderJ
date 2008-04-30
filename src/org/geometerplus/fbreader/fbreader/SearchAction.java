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

import org.geometerplus.fbreader.option.FBOptions;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;

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
		final ZLTextView textView = Reader.getTextView();	
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
