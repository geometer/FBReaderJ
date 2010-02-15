/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.dialogs;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class ZLOptionsDialog {
	private final ZLResource myResource;
	protected final ArrayList /*ZLDialogContent*/ myTabs = new ArrayList();
	protected Runnable myExitAction;
	protected Runnable myApplyAction;
	protected ZLStringOption myTabOption;
	
	protected ZLOptionsDialog(ZLResource resource, Runnable exitAction, Runnable applyAction) {
		myResource = resource;
		myExitAction = exitAction;
		myApplyAction = applyAction;
		myTabOption = new ZLStringOption(resource.Name, "SelectedTab", "");
	}
	
	protected void acceptTab(ZLDialogContent tab) {
		tab.accept();
		if (myApplyAction != null) {
			myApplyAction.run();
		}
	}

	protected void resetTab(ZLDialogContent tab) {
		tab.reset();
	}

	protected void accept() {
		final int size = myTabs.size();
		for (int i = 0; i < size; i++) {
			((ZLDialogContent)myTabs.get(i)).accept();
		}
		if (myApplyAction != null) {
			myApplyAction.run();
		}
	}
	
	protected abstract String getSelectedTabKey();
	
	protected abstract void selectTab(String key);
	
	protected abstract void runInternal();
	
	protected final String getCaption() {
		return myResource.getResource(ZLDialogManager.DIALOG_TITLE).getValue();
	}
	
	protected final ZLResource getTabResource(String key) {
		return myResource.getResource("tab").getResource(key);
	}
	
	public abstract ZLDialogContent createTab(String key);
	
	public void run() {
		selectTab(myTabOption.getValue());
		runInternal();
		myTabOption.setValue(getSelectedTabKey());
	}
	
	/*
	 * protected:
	static void addPlatformDependentBuilder(shared_ptr<ZLDialogContentBuilder> builder);

private:
	static std::vector<shared_ptr<ZLDialogContentBuilder> > ourPlatformDependentBuilders;

public void createPlatformDependentTabs() {
		
	}

	 */
}
