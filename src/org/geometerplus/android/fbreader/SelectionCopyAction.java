package org.geometerplus.android.fbreader;

import android.app.Application;
import android.text.ClipboardManager;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.android.util.UIUtil;

public class SelectionCopyAction extends SelectionProcessAction {

	SelectionCopyAction(FBReader activity, FBReaderApp fbreader) {
		super(activity, fbreader);
	}

	public void run() {
		String text = Reader.getTextView().getSelectedText();
		ClipboardManager clipboard =
			(ClipboardManager)ZLAndroidApplication.Instance().getSystemService(Application.CLIPBOARD_SERVICE);
		clipboard.setText(text);
		UIUtil.showMessageText(
			myActivity,
			ZLResource.resource("dialog").getResource("selection").getResource("textInBuffer").getValue() + "\n" + clipboard.getText()
		);
	}
}

