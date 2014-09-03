package org.geometerplus.android.fbreader;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaBSSelectionCopyAction extends FBAndroidContextAction {
    public YotaBSSelectionCopyAction(Context ctx, FBReaderApp fbreader) {
        super(ctx, fbreader);
    }

    @Override
    protected void run(Object... params) {
        final String text = Reader.getTextView().getSelectedText();
        Reader.getTextView().clearSelection();

        final ClipboardManager clipboard =
                (ClipboardManager)mContext.getSystemService(Application.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("FBReader", text));
    }
}
