package org.zlibrary.core.dialogs;

import java.util.ArrayList;

import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;
import org.zlibrary.core.util.*;

public abstract class ZLOptionsDialog {
	private final ZLResource myResource;
	protected final ArrayList /*ZLDialogContent*/ myTabs = new ArrayList();
	protected ZLRunnable myApplyAction;
	protected ZLStringOption myTabOption;
	
	protected ZLOptionsDialog(ZLResource resource, ZLRunnable applyAction) {
		myResource = resource;
		myApplyAction = applyAction;
		myTabOption = new ZLStringOption(ZLOption.LOOK_AND_FEEL_CATEGORY, resource.Name, "SelectedTab", "");
	}
	
	protected void accept() {
		final int size = myTabs.size();
		for (int i = 0; i < size; i++) {
			((ZLDialogContent) myTabs.get(i)).accept();
		}
		if (myApplyAction != null) {
			myApplyAction.run();
		}
	}
	
	protected abstract String getSelectedTabKey();
	
	protected abstract void selectTab(String key);
	
	protected abstract boolean runInternal();
	
	protected final String getCaption() {
		return myResource.getResource(ZLDialogManager.DIALOG_TITLE).getValue();
	}
	
	protected final ZLResource getTabResource(String key) {
		return myResource.getResource("tab").getResource(key);
	}
	
	public abstract ZLDialogContent createTab(String key);
	
	public boolean run() {
		selectTab(myTabOption.getValue());
		boolean code = runInternal();
		if (code) {
			accept();
		}
		myTabOption.setValue(getSelectedTabKey());
		return code;
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
