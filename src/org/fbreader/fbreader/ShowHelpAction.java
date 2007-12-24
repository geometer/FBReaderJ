package org.fbreader.fbreader;

class ShowHelpAction extends FBAction {
	ShowHelpAction(FBReader fbreader) {
		super(fbreader);
	}
		
	public void run() {
		fbreader().openBook(FBReader.HELP_FILE_NAME);
		fbreader().refreshWindow();
	}
}
