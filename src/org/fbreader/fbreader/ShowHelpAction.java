package org.fbreader.fbreader;

class ShowHelpAction extends FBAction {
	ShowHelpAction(FBReader fbreader) {
		super(fbreader);
	}
		
	public void run() {
		fbreader().openBook(fbreader().getHelpFileName());
		fbreader().refreshWindow();
	}
}
