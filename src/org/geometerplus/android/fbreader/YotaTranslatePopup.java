package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.yotadevices.yotaphone2.fbreader.AbbyyTranslator;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.List;


public class YotaTranslatePopup extends ZLApplication.PopupPanel implements AbbyyTranslator.TranslateCompletitionResult {
	public final static String ID = "YotaTranslatePopup";
	protected FBReaderApp mReaderApp;
	protected android.widget.PopupWindow mPopup;
	protected Context mContext;
	protected ContentResolver mResolver;
	protected LayoutInflater mLayoutInflater;
	protected View mRootView;
	protected View mContentView;
	protected View mCloseButton;
	protected String mTextToTranslate;

	public YotaTranslatePopup(FBReaderApp application, Context ctx, ContentResolver resolver) {
		super(application);
		mReaderApp = application;
		mContext = ctx;
		mResolver = resolver;
		mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mContentView = mLayoutInflater.inflate(R.layout.yota_translate_popup, null);
		mCloseButton = mContentView.findViewById(R.id.close_button);
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hide_();
			}
		});

		mPopup = new android.widget.PopupWindow(ctx);
		mPopup.setBackgroundDrawable(new ColorDrawable(0));
		mPopup.setContentView(mContentView);
		mPopup.setWindowLayoutMode(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void update() {

	}

	@Override
	protected void hide_() {
		mPopup.dismiss();
	}

	@Override
	protected void show_() {
		mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
		AbbyyTranslator translator = new AbbyyTranslator(mResolver, this);
		translator.execute(mTextToTranslate);

	}

	public void setTextToTranlate(String text) {
		mTextToTranslate = text;
	}

	@Override
	public void onTranslationComplete(List<AbbyyTranslator.Translate> results) {

	}

	@Override
	public void onTranslationError(Error error) {

	}

	public void setRootView(View root) {
		mRootView = root;
	}

}
