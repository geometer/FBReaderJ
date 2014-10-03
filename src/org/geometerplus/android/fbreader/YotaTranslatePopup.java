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
import android.widget.TextView;

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
	protected TextView mErrorView;
	protected String mTextToTranslate;

	protected final String mHTMLOpen = "<html><head><style type=\"text/css\">" +
			"   #h1 {" +
			"font-size: 150%;" +
			"   }" +
			"   #layer1 {" +
			"padding-top: 10px;" +
			"   }" +
			"   #layer2 {" +
			"padding-top: 5px;" +
			"padding-left: 30px;" +
			"   }" +
			"   #layer3 {" +
			"padding-left: 30px;" +
			"   }" +
			"   #sugest {" +
			"padding-top: 10px;" +
			"padding-bottom: 20px;" +
			"   }" +
			"</style></head><body>";
	protected final String mHTMLClose = "</body></html>";
	protected final String mHTMLHeader = "<div id=\"h1\"><b>%s</b></div>";
	protected final String mHTMLText = "<div id=\"layer1\"><i>%s \u2794 %s</i></div>";
	protected final String mHTMLSubText = "<div id=\"layer2\">%s</div>";
	private final String mLink = "<div id=\"layer3\"><a href=%s>%s</a></div>";
	private final String mSuggest = "<div id=\"sugest\"><i>%s</i> <a href=%s>%s</a>?</div>";
    protected boolean mOnBackScreen = false;
	protected int mWordsToDefine = 0;

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
		mErrorView = (TextView)mContentView.findViewById(R.id.error_view);
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
		mErrorView.setVisibility(View.GONE);
		if (mWordsToDefine == 1) {
			AbbyyTranslator translator = new AbbyyTranslator(mResolver, this);
			translator.execute(mTextToTranslate);
		}
		else {
			onTranslationError(Error.SELECT_ONE_WORD);
		}
		mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
	}

	public void setTextToTranlate(String text) {
		mTextToTranslate = text;
	}

	@Override
	public void onTranslationComplete(List<AbbyyTranslator.Translate> results) {
		if (results.size() > 0) {
			String source = String.format(mHTMLHeader, results.get(0).Heading);
			String data = "";
			String suggestion = null;
			for (AbbyyTranslator.Translate t : results) {
				if (t.Translation != null) {
					data += String.format(mHTMLText, t.LanguageFrom, t.LanguageTo);
					data += String.format(mHTMLSubText, t.Translation);
                    if (!mOnBackScreen) {
                        data += String.format(mLink, t.ArticleURI, mContext.getString(R.string.open_in_lingvo));
                    }
				} else if (t.Heading != null && t.ArticleURI != null && !mOnBackScreen && suggestion == null) {
					suggestion = String.format(mSuggest, mContext.getString(R.string.did_you_mean), t.ArticleURI, t.Heading);
				}
			}
			if (suggestion == null) { suggestion = ""; }
			String html = mHTMLOpen + source + suggestion + data + mHTMLClose;
			String base64 = Base64.encodeToString(html.getBytes(), Base64.DEFAULT);
			mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
		}
	}

	@Override
	public void onTranslationError(Error error) {
		String errorText = "";
		switch (error) {
			case NOTHING_TO_TRANSLATE:
				errorText = mContext.getString(R.string.nothing_to_translate);
				break;
			case TRANSLATION_NOT_FOUND:
				errorText = mContext.getString(R.string.nothing_was_found);
				break;
			case UNKNOWN_ERROR:
				errorText = mContext.getString(R.string.unknown_error);
				break;
			case LINGVO_INTERNAL_ERROR:
				errorText = mContext.getString(R.string.lingvo_error);
				break;
			case NO_DICTIONARIES:
				errorText = mContext.getString(R.string.no_dictionaries);
				break;
			case SELECT_ONE_WORD:
				errorText = mContext.getString(R.string.select_only_one_word);
				break;
		}
		mErrorView.setVisibility(View.VISIBLE);
		mErrorView.setText(errorText);
	}

	public void setNumWordsToDefine(int words) {
		mWordsToDefine = words;
	}

	public void setRootView(View root) {
		mRootView = root;
	}

}
