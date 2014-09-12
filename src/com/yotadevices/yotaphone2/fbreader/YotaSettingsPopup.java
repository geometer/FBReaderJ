package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.TreeMap;

public class YotaSettingsPopup {
	private final android.widget.PopupWindow mPopup;
	private final Context mContext;
	private final View mRootView;
	private final View mPopupView;

	private final OnSortingModeChangeListener mListener;
	private final static int[] FONT_RADIO_BUTTONS_IDS =
			{R.id.font1, R.id.font2, R.id.font3, R.id.font4, R.id.font5};

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

	private View.OnClickListener mOnFontSelectListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//enableSortMethod(v.getId());
		}
	};

	public YotaSettingsPopup(int layoutId, Context ctx, View root, OnSortingModeChangeListener listener) {
		mContext = ctx;
		mRootView = root;
		mListener = listener;

		mPopupView = View.inflate(ctx, layoutId, null);
		//initRadioButtons(mPopupView);

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

	private void initRadioButtons(View root, int[] buttons, TreeMap<Integer, RadioButton> buttonsGroup) {
		for (int id : buttons) {
			RadioButton rb = (RadioButton)root.findViewById(id);
			rb.setOnClickListener(mOnFontSelectListener);
		}
	}

	private void selectRadioButton(int id, TreeMap<Integer, RadioButton> buttonsGroup) {
		for (RadioButton rb : buttonsGroup.values()) {
			rb.setChecked(false);
		}
		final RadioButton rb = (RadioButton)buttonsGroup.get(id);
		rb.setChecked(true);
		hide();
	}

	public void show(FBReaderApp readerApp) {
		final ZLStringOption option =
				readerApp.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;

		mPopup.showAsDropDown(mRootView, 0, 0);
		EinkUtils.performSingleUpdate(mPopupView, Drawer.Waveform.WAVEFORM_GC_FULL);
	}

	public void hide() {
		mPopup.dismiss();
	}

	public boolean isShowing() {
		return  mPopup.isShowing();
	}
}
