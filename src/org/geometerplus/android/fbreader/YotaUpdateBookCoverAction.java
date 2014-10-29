package org.geometerplus.android.fbreader;

import android.content.Context;

import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaUpdateBookCoverAction extends FBAndroidContextAction {
	private final boolean mOnBackScreen;
	public YotaUpdateBookCoverAction(Context ctx, FBReaderApp fbreader, boolean backscreen) {
		super(ctx, fbreader);
		mOnBackScreen = backscreen;
	}

	@Override
	protected void run(Object... params) {
		if (params != null && params.length > 0) {
			Book b = (Book)params[0];
			if (!mOnBackScreen) {
				((FBReader) mContext).updateCoverOnYotaWidget(b);
			}
			else {
				((FBReaderYotaService)mContext).updateCoverOnYotaWidget(b);
			}
		}
	}
}
