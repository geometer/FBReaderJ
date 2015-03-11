package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.view.GaugeView;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import com.yotadevices.yotaphone2.yotareader.R;

public class BSReadingStatusBar {
    private final android.widget.PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;
    private final View mPopupView;
    private final FBReaderApp mReader;
	private boolean mFirstStart = true;
    private final static int STATUS_BAR_HEIGHT = 117;
    private final static int SYSTEM_NAVIGATION_BAR = 80;
    //private final static int SYSTEM_NAVIGATION_BAR = 0;

	private TextView mBackToPage;
    private TextView mTotalPages;
    private TextView mPagesLeft;
	private SeekBar mSlider;
	private boolean mNavigated = false;
	protected ZLTextWordCursor myStartPosition;

    public BSReadingStatusBar(Context ctx, View root, FBReaderApp readerApp) {
        mContext = ctx;
        mRootView = root;
        mReader = readerApp;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(R.layout.bs_status_bar_reading_mode, null);
	    mSlider = (SeekBar)mPopupView.findViewById(R.id.navigation_slider);

        mBackToPage = (TextView)mPopupView.findViewById(R.id.back_to_page);
        mTotalPages = (TextView)mPopupView.findViewById(R.id.page);
        mPagesLeft  = (TextView)mPopupView.findViewById(R.id.pages_left);

	    //final TextView page = (TextView)mPopupView.findViewById(R.id.page);
	    //final TextView pages_left = (TextView)mPopupView.findViewById(R.id.pages_left);
	    //final TextView back_to_page = (TextView)mPopupView.findViewById(R.id.back_to_page);
	    //back_to_page.setText(myWindow.getActivity().getString(R.string.back_to_page) + " " + mStartPage);
	    mBackToPage.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    if (mNavigated) {
				    mNavigated = false;
				    if (myStartPosition != null) {
					    mReader.getTextView().gotoPosition(myStartPosition);
				    }
				    mReader.getViewWidget().reset();
				    mReader.getViewWidget().repaint();
				    updateData();
			    }
			    else {
				    mReader.jumpBack();
			    }
		    }
	    });

        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(mPopupView);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

	    mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		    private void gotoPage(int page) {
			    final ZLTextView view = mReader.getTextView();
			    if (page == 1) {
				    view.gotoHome();
			    } else {
				    view.gotoPage(page);
			    }
			    mReader.getViewWidget().reset();
			    mReader.getViewWidget().repaint();
		    }

		    public void onStartTrackingTouch(SeekBar seekBar) {
			    mNavigated = true;
		    }

		    public void onStopTrackingTouch(SeekBar seekBar) {
			    mReader.storePosition();
		    }

		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			    if (fromUser) {
				    final int curpage = progress + 1;
				    final int pagesNumber = seekBar.getMax() + 1;
				    gotoPage(curpage);
				    mTotalPages.setText(makeProgressText(curpage, pagesNumber));
				    int percents = (int)(((float)curpage / (float)pagesNumber) * 100);
				    String percentsText = percents+"%";
				    mPagesLeft.setText(UIUtils.isArabic() ? UIUtils.convertStringWithNumbersToArabic(percentsText) :percentsText);
			    }
		    }
	    });
    }

    public void updateData() {
        final ZLTextView textView = mReader.getTextView();
        final ZLTextView.PagePosition pagePosition = textView.pagePosition();

	    mTotalPages.setText(makeProgressText(pagePosition.Current, pagePosition.Total));

	    int percents = (int)(((float)pagePosition.Current / (float)pagePosition.Total) * 100);
	    String percentsText = percents+"%";
	    mPagesLeft.setText(UIUtils.isArabic() ? UIUtils.convertStringWithNumbersToArabic(percentsText) :percentsText);

	    if (mSlider.getMax() != pagePosition.Total - 1 || mSlider.getProgress() != pagePosition.Current - 1) {
		    mSlider.setMax(pagePosition.Total - 1);
		    mSlider.setProgress(pagePosition.Current - 1);
	    }

    }

    public void show() {
        updateData();
	    myStartPosition = new ZLTextWordCursor(mReader.getTextView().getStartCursor());

		int yOffset = mRootView.getHeight() - (STATUS_BAR_HEIGHT + (mFirstStart ? 0 : SYSTEM_NAVIGATION_BAR));
	    //int yOffset = mRootView.getHeight() - (STATUS_BAR_HEIGHT + SYSTEM_NAVIGATION_BAR);
	    mFirstStart = false;
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, yOffset);
    }

    public void hide() {
        mPopup.dismiss();
	    if (myStartPosition != null &&
			    !myStartPosition.equals(mReader.getTextView().getStartCursor())) {
		    mReader.addInvisibleBookmark(myStartPosition);
		    mReader.storePosition();
	    }
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

	protected String makeProgressText(int page, int pagesNumber) {
		final StringBuilder builder = new StringBuilder();
		builder.append(page);
		builder.append("/");
		builder.append(pagesNumber);
		return UIUtils.isArabic() ? UIUtils.convertStringWithNumbersToArabic(builder.toString()) : builder.toString();
	}

}
