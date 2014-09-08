package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yotadevices.yotaphone2.fbreader.AbbyyTranslator;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import java.nio.charset.Charset;
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
	protected WebView mWebView;
	protected String mTextToTranslate;

	protected final String mHTMLOpen = "<html><body>";
	protected final String mHTMLClose = "</body></html>";
	protected final String mHTMLHeader = "<h3>%s</h3>";
	protected final String mHTMLText = "<p>%s</p>";

	private final String mLink = "<p><a href=%s>%s</a></p>";
	private final String mSuggest = "<p>%s <a href=%s>%s</a>?</p>";
    protected boolean mOnBackScreen = false;

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
		mWebView = (WebView)mContentView.findViewById(R.id.webview);
		WebSettings settings = mWebView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
        mWebView.setVerticalScrollBarEnabled(false);

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

    public void setOnBackScreen(boolean val) {
        mOnBackScreen = val;
    }

	@Override
	protected void hide_() {
		mWebView.loadData("", "text/html", "");
		mPopup.dismiss();
	}

	@Override
	protected void show_() {
        if (mOnBackScreen) {
            mWebView.setInitialScale(150);
        }
		mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
		AbbyyTranslator translator = new AbbyyTranslator(mResolver, this);
		translator.execute(mTextToTranslate);
	}

	public void setTextToTranlate(String text) {
		mTextToTranslate = text;
	}

	@Override
	public void onTranslationComplete(List<AbbyyTranslator.Translate> results) {
		if (results.size() > 0) {
			String source = String.format(mHTMLHeader, results.get(0).Heading);
			String data = "";
			for (AbbyyTranslator.Translate t : results) {
				if (t.Translation != null) {
					data += String.format(mHTMLText, t.Translation);
                    if (!mOnBackScreen) {
                        data += String.format(mLink, t.ArticleURI, mContext.getString(R.string.open_in_lingvo));
                    }
				} else if (t.Heading != null && t.ArticleURI != null && !mOnBackScreen) {
					data += String.format(mSuggest, mContext.getString(R.string.did_you_mean), t.ArticleURI, t.Heading);
				}
			}
			String html = mHTMLOpen + source + data + mHTMLClose;
			String base64 = Base64.encodeToString(html.getBytes(), Base64.DEFAULT);
			mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
		}
	}

	@Override
	public void onTranslationError(Error error) {
		String errorText = "";
		switch (error) {
			case NOTHING_TO_TRANSLATE:
				errorText = String.format(mHTMLHeader, mContext.getString(R.string.nothing_to_translate));
				break;
			case TRANSLATION_NOT_FOUND:
				errorText = String.format(mHTMLHeader, mContext.getString(R.string.nothing_was_found));
				break;
			case UNKNOWN_ERROR:
				errorText = String.format(mHTMLHeader, mContext.getString(R.string.unknown_error));
				break;
			case LINGVO_INTERNAL_ERROR:
				errorText = String.format(mHTMLHeader, mContext.getString(R.string.lingvo_error));
				break;
		}
		String html = mHTMLOpen + errorText + mHTMLClose;
		String base64 = Base64.encodeToString(html.getBytes(), Base64.DEFAULT);
		mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
	}

	public void setRootView(View root) {
		mRootView = root;
	}

}
