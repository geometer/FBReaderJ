package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class SelectionDictionaryAction extends FBAndroidAction {
    SelectionDictionaryAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    public void run() {
        final FBView fbview = Reader.getTextView();
        final int selectionStartY = fbview.getSelectionStartY(), selectionEndY = fbview.getSelectionEndY();
        String text = fbview.getSelectedText();
        Reader.getTextView().deactivateSelectionMode();
        DictionaryUtil.openTextInDictionary(BaseActivity, text, selectionStartY, selectionEndY);
    }

}
