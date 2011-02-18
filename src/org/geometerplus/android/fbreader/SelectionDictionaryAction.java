package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class SelectionDictionaryAction extends SelectionProcessAction {
    SelectionDictionaryAction(FBReader activity, FBReaderApp fbreader) {
        super(activity, fbreader);
    }

    public void run() {
        final FBView fbview = Reader.getTextView();
        final int selectionStartY = fbview.getSelectionStartY(), selectionEndY = fbview.getSelectionEndY();
        String text = fbview.getSelectedText();
        Reader.getTextView().deactivateSelectionMode();
        DictionaryUtil.openTextInDictionary(myActivity, text, selectionStartY, selectionEndY);
    }

}
