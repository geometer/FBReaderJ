package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Base64;
import android.view.Gravity;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.OxfordDefinition;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaDefinePopup extends YotaTranslatePopup implements OxfordDefinition.DefinitionResult{
	public final static String ID = "YotaDefinePopup";

	public YotaDefinePopup(FBReaderApp application, Context ctx, ContentResolver resolver) {
		super(application, ctx, resolver);
	}

	@Override
	protected void show_() {
        if (mOnBackScreen) {
            mWebView.setInitialScale(150);
        }
		mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
        mWebView.loadData("<h3>Loading, please wait...</h3>", "text/html; charset=utf-8", "");
		OxfordDefinition.getDefinition(mTextToTranslate, this);
	}

	@Override
	public void onObtainDefinition(OxfordDefinition.Definition result) {
		String base64 = Base64.encodeToString(result.Xml.getBytes(), Base64.DEFAULT);
        if (!mOnBackScreen) {
            mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
            //mWebView.loadUrl(result.ArticleURI);
        }
        else {
            //mWebView.clearCache(false);
            //mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
            mWebView.loadUrl(result.ArticleURI);
        }
        if (mOnBackScreen) {
            EinkUtils.performSingleUpdate(mWebView, Drawer.Waveform.WAVEFORM_GC_FULL);
        }
	}

	@Override
	public void onDefinitionError(OxfordDefinition.DefinitionResult.Error error) {

	}

	@Override
	public String getId() {
		return ID;
	}
}
