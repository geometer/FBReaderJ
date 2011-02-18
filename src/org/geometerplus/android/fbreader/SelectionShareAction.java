package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.content.Intent;

public class SelectionShareAction extends SelectionProcessAction {
	SelectionShareAction(FBReader activity, FBReaderApp fbreader) {
		super(activity, fbreader);
	}

	public void run() {
		String text = Reader.getTextView().getSelectedText();
		Reader.getTextView().deactivateSelectionMode();
		String title = Reader.Model.Book.getTitle();
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
				ZLResource.resource("menu").getResource("selection").getResource("quoteFrom").getValue() + " " + title);
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		myActivity.startActivity(Intent.createChooser(shareIntent, null));
	}

}
