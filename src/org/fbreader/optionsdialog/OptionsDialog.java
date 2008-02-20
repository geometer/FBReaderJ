package org.fbreader.optionsdialog;

import org.fbreader.fbreader.FBReader;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.runnable.ZLRunnable;

public class OptionsDialog {
	private ZLOptionsDialog myDialog;
	
	public OptionsDialog(FBReader fbreader) {
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("OptionsDialog", new OptionsApplyRunnable(fbreader), true);

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
	//		myFBReader.grabAllKeys(myFBReader.KeyboardControlOption.getValue());
	//		myFBReader.clearTextCaches();
	//		((CollectionView) myFBReader.CollectionView).synchronizeModel();
			myFBReader.refreshWindow();
		}
	}
}
