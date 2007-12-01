package org.fbreader.fbreader;

class QuitAction extends FBAction {
	QuitAction(FBReader fbreader) {
		super(fbreader);
	}
		
	public void run() {
		fbreader().closeView();
	}		
}
