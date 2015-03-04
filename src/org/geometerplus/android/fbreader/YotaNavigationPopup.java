package org.geometerplus.android.fbreader;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import com.yotadevices.yotaphone2.fbreader.UIUtils;
import com.yotadevices.yotaphone2.yotareader.R;

public class YotaNavigationPopup extends NavigationPopup {
	private TextView mPage;
	private TextView mPagesLeft;
	private TextView mBackToPage;
	private ImageView mBackArrow;
	private TextView mBackButton;
	private TOCPopup mTOCPopup;
	private RelativeLayout mRootView;
	private int mStartPage;

	private SeekBar mSlider;
	private boolean mNavigated = false;

	YotaNavigationPopup(FBReaderApp fbReader) {
		super(fbReader);
	}

	public void runNavigation(FBReader activity, RelativeLayout root) {
		createControlPanel(activity, root);
		myStartPosition = new ZLTextWordCursor(myFBReader.getTextView().getStartCursor());
		mStartPage = myFBReader.getTextView().pagePosition().Current;
		myWindow.show();
		setupNavigation();
	}

	public void stopNavigation() {
		if (myWindow == null) {
			return;
		}

		if (myStartPosition != null &&
				!myStartPosition.equals(myFBReader.getTextView().getStartCursor())) {
			myFBReader.addInvisibleBookmark(myStartPosition);
			myFBReader.storePosition();
		}
		myWindow.hide();
		myWindow = null;
	}

	private void hideBackButton() {
		mBackButton.setVisibility(View.INVISIBLE);
		mBackArrow.setVisibility(View.INVISIBLE);
	}

	private void showBackButton() {
		mBackButton.setVisibility(View.VISIBLE);
		mBackArrow.setVisibility(View.VISIBLE);
	}

	public void createControlPanel(FBReader activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getActivity()) {
			return;
		}

		myWindow = new PopupWindow(activity, root, PopupWindow.Location.BottomFlat);
		myWindow.setBackgroundColor(Color.WHITE);
		mTOCPopup = new TOCPopup(activity);
		mRootView = root;
		final View layout = activity.getLayoutInflater().inflate(R.layout.yota_navigate, myWindow, false);
		mSlider = (SeekBar)layout.findViewById(R.id.navigation_slider);

		final TextView page = (TextView)layout.findViewById(R.id.page);
		final TextView pages_left = (TextView)layout.findViewById(R.id.pages_left);
		mBackArrow = (ImageView)layout.findViewById(R.id.back_to_page_icon);
		mBackButton = (TextView)layout.findViewById(R.id.back_to_page);
		mBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNavigated) {
					mNavigated = false;
					if (myStartPosition != null) {
						myFBReader.getTextView().gotoPosition(myStartPosition);
					}
					myFBReader.getViewWidget().reset();
					myFBReader.getViewWidget().repaint();
					update();
					hideBackButton();;
				}
				else {
					myFBReader.jumpBack();
					myFBReader.runAction(ActionCode.TOGGLE_BARS);
				}
			}
		});
		if (!myFBReader.canJumpBack()) {
			hideBackButton();
		}

		final String pages_left_resource = myWindow.getActivity().getString(R.string.pages_left);
		final String of = myWindow.getActivity().getString(R.string.of);

		mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLTextView view = myFBReader.getTextView();
				if (page == 1) {
					view.gotoHome();
				} else {
					view.gotoPage(page);
				}
				myFBReader.getViewWidget().reset();
				myFBReader.getViewWidget().repaint();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				mNavigated = true;
				showBackButton();
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				hideChpaterPopup();
				myFBReader.storePosition();
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int curpage = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1;
					gotoPage(curpage);
					page.setText(makeProgressText(curpage, pagesNumber, of));
					int percents = (int)(((float)curpage / (float)pagesNumber) * 100);
					String percentsText = percents+"%";
					pages_left.setText(UIUtils.isArabic() ? UIUtils.convertStringWithNumbersToArabic(percentsText) :percentsText);

					final TOCTree tocElement = myFBReader.getCurrentTOCElement();
					if (tocElement != null) {
						final String chapter = tocElement.getText();
						final String page = String.format("%d", curpage);
						showChapterPopup(chapter, page);
					}
				}
			}
		});

		myWindow.addView(layout);
	}

	@Override
	protected void setupNavigation() {
		final SeekBar slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		final TextView page = (TextView)myWindow.findViewById(R.id.page);
		final TextView pagesLeft = (TextView)myWindow.findViewById(R.id.pages_left);
		final TextView back_to_page = (TextView)myWindow.findViewById(R.id.back_to_page);

		final ZLTextView textView = myFBReader.getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();
		//back_to_page.setText(myWindow.getActivity().getString(R.string.back_to_page) + " " + mStartPage);

		if (slider.getMax() != pagePosition.Total - 1 || slider.getProgress() != pagePosition.Current - 1) {
			slider.setMax(pagePosition.Total - 1);
			slider.setProgress(pagePosition.Current - 1);
			page.setText(makeProgressText(pagePosition.Current, pagePosition.Total, myWindow.getActivity().getString(R.string.of)));
			int percents = (int)(((float)pagePosition.Current / (float)pagePosition.Total) * 100);
			String percentsText = percents+"%";
			pagesLeft.setText(UIUtils.isArabic() ? UIUtils.convertStringWithNumbersToArabic(percentsText) :percentsText);
		}
	}

	private void showChapterPopup(String chapter, String page) {
		mTOCPopup.show(mRootView, chapter, page);
	}

	private void hideChpaterPopup() {
		mTOCPopup.hide();
	}

	protected String makeProgressText(int page, int pagesNumber, String of) {
		final StringBuilder builder = new StringBuilder();
		builder.append(page);
		builder.append("/");
		builder.append(pagesNumber);
//		final TOCTree tocElement = myFBReader.getCurrentTOCElement();
//		if (tocElement != null) {
//			builder.append("  ");
//			builder.append(tocElement.getText());
//		}
		return UIUtils.isArabic() ? UIUtils.convertStringWithNumbersToArabic(builder.toString()) : builder.toString();
	}
}
