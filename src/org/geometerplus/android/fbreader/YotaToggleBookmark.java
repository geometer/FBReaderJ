package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;

/**
 * Created by ybereza on 05.10.14.
 */
public class YotaToggleBookmark extends FBAndroidAction {
	YotaToggleBookmark(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		final ZLTextView textView = Reader.getTextView();
		if (textView.hasBookmarks()) {
			final Bookmark bookmark = textView.getCurrentBookmarkHighlighting().getBookmark();
			Reader.Collection.deleteBookmark(bookmark);
		}
		else {
			final Bookmark bookmark = Reader.createBookmark(20, true);
			Reader.Collection.saveBookmark(bookmark);
		}
		BaseActivity.hideBars();
	}
}
