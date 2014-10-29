package org.geometerplus.android.fbreader;


import android.content.Context;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaBSSelectionBookmarkAction extends FBAndroidContextAction {
    public YotaBSSelectionBookmarkAction(Context ctx, FBReaderApp fbreader) {
        super(ctx, fbreader);
    }

    @Override
    protected void run(Object... params) {
        final boolean existingBookmark;
        final Bookmark bookmark;

        if (params.length != 0) {
            existingBookmark = true;
            bookmark = (Bookmark)params[0];
        } else {
            existingBookmark = false;
            bookmark = Reader.addSelectionBookmark();
        }
    }
}
