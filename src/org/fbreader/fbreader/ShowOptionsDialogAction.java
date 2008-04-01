package org.fbreader.fbreader;

import org.fbreader.optionsDialog.OptionsDialog;

class ShowOptionsDialogAction extends FBAction {
	ShowOptionsDialogAction(FBReader fbreader) {
		super(fbreader);
	}

	protected void run() {
		new OptionsDialog(fbreader()).getDialog().run();
	}
}
