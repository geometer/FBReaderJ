package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.SeekBar;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.TreeMap;

public class YotaSettingsPopup {
	private final android.widget.PopupWindow mPopup;
	private final FBReader mFBReader;
	private FBReaderApp mReaderApp;
	private final View mRootView;
	private final View mPopupView;
	private final SeekBar mBrightnessSlide;

	private final static int[] FONT_RADIO_BUTTONS_IDS =
			{R.id.font1, R.id.font2, R.id.font3, R.id.font4, R.id.font5};
	private final static float[] FONT_SIZES = {16, 18, 20, 22.6f, 25.3f};

	private final static int[] FONT_TYPE_RADIO_BUTTONS_IDS =
			{R.id.font_sans, R.id.font_serif};

	private final static int[] THEME_RADIO_BUTTONS_IDS =
			{R.id.theme_white, R.id.theme_sepia, R.id.theme_black};

	private TreeMap<Integer, RadioButton> mFontSizeGroup = new TreeMap<Integer, RadioButton>();
	private TreeMap<Integer, RadioButton> mFontTypeGroup = new TreeMap<Integer, RadioButton>();
	private TreeMap<Integer, RadioButton> mThemeGroup    = new TreeMap<Integer, RadioButton>();

	public interface OnSortingModeChangeListener {
		public void onSortMethodChanged(int sortMethod);
	}

	private float getFontSizeByID(int fontID) {
		for (int i = 0; i < FONT_RADIO_BUTTONS_IDS.length; ++i) {
			if (FONT_RADIO_BUTTONS_IDS[i] == fontID) {
				return FONT_SIZES[i];
			}
		}
		return FONT_SIZES[0];
	}

	private View.OnClickListener mOnFontSizeSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			selectRadioButton(v.getId(), mFontSizeGroup);
			float size = getFontSizeByID(v.getId());
			float px = UIUtils.convertDpToPixel(size, mFBReader);

			final ZLIntegerRangeOption option =
					mReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
			option.setValue((int)px);
			mReaderApp.clearTextCaches();
			mReaderApp.getViewWidget().repaint();
		}
	};

	private View.OnClickListener mOnFontTypeSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			final int viewId = v.getId();
			final ZLStringOption option =
					mReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;
			selectRadioButton(viewId, mFontTypeGroup);
			switch (viewId) {
				case R.id.font_serif:
					option.setValue("Droid Serif");
					break;
				case R.id.font_sans:
					option.setValue("yota-sans");
					break;
			}
			mReaderApp.clearTextCaches();
			mReaderApp.getViewWidget().repaint();
		}
	};

	private View.OnClickListener mOnThemeSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			final int viewId = v.getId();
			selectRadioButton(v.getId(), mThemeGroup);
			switch (viewId) {
				case R.id.theme_black:
					mReaderApp.ViewOptions.YotaFSColorProfileName.setValue(ColorProfile.YOTA_FS_BLACK);
					break;
				case R.id.theme_white:
					mReaderApp.ViewOptions.YotaFSColorProfileName.setValue(ColorProfile.YOTA_FS_WHITE);
					break;
				case R.id.theme_sepia:
					mReaderApp.ViewOptions.YotaFSColorProfileName.setValue(ColorProfile.YOTA_FS_SEPIA);
					break;
			}
			mReaderApp.getViewWidget().reset();
			mReaderApp.getViewWidget().repaint();
		}
	};

	public YotaSettingsPopup(int layoutId, FBReader ctx, View root) {
		mFBReader = ctx;
		mRootView = root;

		mPopupView = View.inflate(ctx, layoutId, null);

		initRadioButtons(mPopupView, FONT_RADIO_BUTTONS_IDS, mOnFontSizeSelectListener, mFontSizeGroup);
		initRadioButtons(mPopupView, FONT_TYPE_RADIO_BUTTONS_IDS, mOnFontTypeSelectListener, mFontTypeGroup);
		initRadioButtons(mPopupView, THEME_RADIO_BUTTONS_IDS, mOnThemeSelectListener, mThemeGroup);

		mBrightnessSlide = (SeekBar)mPopupView.findViewById(R.id.brighness_seekbar);
		mPopup = new android.widget.PopupWindow(ctx);
		mPopup.setBackgroundDrawable(new ColorDrawable(0));
		mPopup.setContentView(mPopupView);
		mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

		final View clickableArea = mPopupView.findViewById(R.id.clickable_area);
		if (clickableArea != null ) {
			//there is no clickable area on the back screen, only on frontscreen
			clickableArea.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					hide();
				}
			});
		}
		else {
			//this helps closing popup on the backscreen if user clicked outside popup
			mPopup.setFocusable(true);
		}

	}

	private void initRadioButtons(View root, int[] buttons, View.OnClickListener listener, TreeMap<Integer, RadioButton> buttonsGroup) {
		for (int id : buttons) {
			RadioButton rb = (RadioButton)root.findViewById(id);
			buttonsGroup.put(id, rb);
			rb.setOnClickListener(listener);
		}
	}

	private void selectRadioButton(int id, TreeMap<Integer, RadioButton> buttonsGroup) {
		for (RadioButton rb : buttonsGroup.values()) {
			rb.setChecked(false);
		}
		final RadioButton rb = (RadioButton)buttonsGroup.get(id);
		rb.setChecked(true);
	}

	private void setupFontTypes() {
		final ZLStringOption option =
				mReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;
		if (option.getValue().toLowerCase().contains("serif")) {
			selectRadioButton(R.id.font_serif, mFontTypeGroup);
		}
		else {
			selectRadioButton(R.id.font_sans, mFontTypeGroup);
		}
	}

	private void setupFontSizes() {
		final ZLIntegerRangeOption option =
				mReaderApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
		float fontSize = UIUtils.convertPixelsToDp(option.getValue(), mFBReader);
		int matched = -1;
		for (int i = 0; i < FONT_SIZES.length - 1; ++i) {
			float curValue = FONT_SIZES[i];
			float nextValue = FONT_SIZES[i+1];
			float diff = (nextValue - curValue) / 2;
			if (fontSize <= curValue + diff) {
				matched = i;
				break;
			}
			else if (fontSize <= nextValue) {
				matched = i+1;
				break;
			}
		}
		if (matched == -1) {
			matched = FONT_SIZES.length - 1;
		}
		selectRadioButton(FONT_RADIO_BUTTONS_IDS[matched], mFontSizeGroup);
	}

	private void setupTheme() {
		final String theme = mReaderApp.ViewOptions.YotaFSColorProfileName.getValue();
		if (theme.equals(ColorProfile.YOTA_FS_BLACK)) {
			selectRadioButton(R.id.theme_black, mThemeGroup);
		}
		else if (theme.equals(ColorProfile.YOTA_FS_SEPIA)) {
			selectRadioButton(R.id.theme_sepia, mThemeGroup);
		}
		else {
			selectRadioButton(R.id.theme_white, mThemeGroup);
		}
	}

	private void setupBrightness() {
		final int cur = mFBReader.getScreenBrightness();
		mBrightnessSlide.setMax(100);
		mBrightnessSlide.setProgress(cur);
		mBrightnessSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mFBReader.setScreenBrightness(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	public void show(FBReaderApp readerApp) {
		mReaderApp = readerApp;
		setupFontSizes();
		setupFontTypes();
		setupTheme();
		setupBrightness();
		mPopup.showAsDropDown(mRootView, 0, 0);
	}

	public void hide() {
		mPopup.dismiss();
		mFBReader.hideBars();
	}

	public boolean isShowing() {
		return  mPopup.isShowing();
	}
}
