package org.geometerplus.fbreader.fbreader;

import org.geometerplus.fbreader.optionsDialog.OptionsDialog;

class ShowOptionsDialogAction extends FBAction {
	ShowOptionsDialogAction(FBReader fbreader) {
		super(fbreader);
	}

	protected void run() {
		new OptionsDialog(fbreader()).getDialog().run();
	}
}
