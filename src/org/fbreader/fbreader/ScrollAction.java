package org.fbreader.fbreader;

import org.zlibrary.text.view.ZLTextView;

class ScrollAction extends FBAction {
	private final int myNumberOfParagraphs;

	ScrollAction(FBReader fbreader, int numberOfParagraphs) {
		super(fbreader);
		myNumberOfParagraphs = numberOfParagraphs;
	}
		
	public void run() {
		fbreader().getTextView().scroll(myNumberOfParagraphs);
		fbreader().refreshWindow();
	}		
}
