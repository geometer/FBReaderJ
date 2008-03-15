package org.fbreader.fbreader;

import org.fbreader.description.BookDescription;

class ShowHelpAction extends FBAction {
	ShowHelpAction(FBReader fbreader) {
		super(fbreader);
	}
		
	public void run() {
		//fbreader().openBook(fbreader().getHelpFileName());
		fbreader().openBook(BookDescription.getDescription(fbreader().getHelpFileName()));
		
		fbreader().refreshWindow();
	}
}
