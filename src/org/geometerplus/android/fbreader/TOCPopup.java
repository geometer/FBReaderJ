package org.geometerplus.android.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yotadevices.yotaphone2.fbreader.UIUtils;

import org.geometerplus.zlibrary.ui.android.R;

public class TOCPopup {
	private final android.widget.PopupWindow mPopup;
	private TextView mChapter;
	private TextView mPage;

	private String mPageText;
	private final int mYOffset;

	public TOCPopup(Context ctx) {
		View popupView = View.inflate(ctx, R.layout.yota_toc_popup, null);
		mYOffset = (int)UIUtils.convertDpToPixel(400, ctx);

		mChapter = (TextView)popupView.findViewById(R.id.chapter);
		mPage = (TextView)popupView.findViewById(R.id.page);
		mPageText = ctx.getString(R.string.page);
		mPopup = new android.widget.PopupWindow(ctx);
		mPopup.setBackgroundDrawable(new ColorDrawable(0));
		mPopup.setContentView(popupView);

		mPopup.setWindowLayoutMode(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setFocusable(false);
	}

	public void show(View anchor, String chapter, String page) {
		mChapter.setText(chapter);
		mPage.setText(mPageText+" "+page);

		if (mPopup.isShowing()) {
			mPopup.update();
		} else {
			mPopup.showAtLocation(anchor, Gravity.CENTER_HORIZONTAL| Gravity.TOP, 0, mYOffset);
		}
	}

	public void hide() {
		mPopup.dismiss();
	}
}
