package org.fbreader.fbreader;

import org.fbreader.optionsDialog.OptionsDialog;

class ShowOptionsDialogAction extends FBAction {

	ShowOptionsDialogAction(FBReader fbreader) {
		super(fbreader);
	}

	protected void run() {
		// TODO Auto-generated method stub
		FBReader f = fbreader();
		new OptionsDialog(f).getDialog().run();
	}

}
