package org.fbreader.fbreader;

class ShowOptionsDialogAction extends FBAction {

	ShowOptionsDialogAction(FBReader fbreader) {
		super(fbreader);
	}

	@Override
	protected void run() {
		// TODO Auto-generated method stub
		FBReader f = fbreader();
	//	new OptionsDialog(f).dialog().run();
	}

}
