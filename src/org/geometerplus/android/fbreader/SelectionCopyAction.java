package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import android.app.Application;
import android.text.ClipboardManager;
import android.widget.Toast;

public class SelectionCopyAction extends SelectionProcessAction {

	SelectionCopyAction(FBReader activity, FBReaderApp fbreader) {
		super(activity, fbreader);
	}

	public void run() {
		String text = Reader.getTextView().getSelectedText();
		Reader.getTextView().deactivateSelectionMode();
		ClipboardManager clipboard =
			(ClipboardManager)ZLAndroidApplication.Instance().getSystemService(Application.CLIPBOARD_SERVICE);
		clipboard.setText(text);
		Toast.makeText(myActivity,
				ZLResource.resource("menu").getResource("selection").getResource("textInBuffer").getValue() + "\n" + clipboard.getText(),
				Toast.LENGTH_SHORT).show();
	}
}

