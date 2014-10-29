package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.OxfordDefinition;
import com.yotadevices.yotaphone2.fbreader.util.ConnectionManager;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;

public class YotaDefinePopup extends YotaTranslatePopup implements OxfordDefinition.DefinitionResult{
	public final static String ID = "YotaDefinePopup";

	public YotaDefinePopup(FBReaderApp application, Context ctx, ContentResolver resolver) {
		super(application, ctx, resolver);
	}

	@Override
	protected void show_() {
		mErrorView.setVisibility(View.GONE);
		if (mWordsToDefine == 1) {
			ConnectionManager.getInstance().startNetworkMonitoring(mContext);
			if (ConnectionManager.getInstance().connected()) {
				if (mOnBackScreen) {
					mWebView.setInitialScale(150);
				}
				mWebView.loadData("<h3>Loading, please wait...</h3>", "text/html; charset=utf-8", "");
				OxfordDefinition.getDefinition(mTextToTranslate, this);
			} else {
				onDefinitionError(OxfordDefinition.DefinitionResult.Error.NO_CONNECTION);
			}
		}
		else {
			onDefinitionError(OxfordDefinition.DefinitionResult.Error.SELECT_ONE_WORD);
		}
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    protected void hide_() {
        ConnectionManager.getInstance().stopNetworkMonitoring(mContext);
        super.hide_();
    }

    @Override
	public void onObtainDefinition(OxfordDefinition.Definition result) {
		String base64 = Base64.encodeToString(result.Xml.getBytes(), Base64.DEFAULT);
        if (!mOnBackScreen) {
            mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
        }
        else {
            mWebView.loadUrl(result.ArticleURI);
        }
        if (mOnBackScreen) {
            EinkUtils.performSingleUpdate(mWebView, Drawer.Waveform.WAVEFORM_GC_FULL);
        }
	}

	@Override
	public void onDefinitionError(OxfordDefinition.DefinitionResult.Error error) {
        String errorText = "";
        switch (error) {
            case NOTHING_TO_DEFINE:
                errorText = mContext.getString(R.string.nothing_to_define);
                break;
            case INCORRECT_REQUEST:
                errorText = mContext.getString(R.string.incorrect_request);
                break;
            case NO_CONNECTION:
                errorText = mContext.getString(R.string.no_connection);
                break;
            case NOT_FOUND:
	            errorText = mContext.getString(R.string.only_english);
	            break;
            case SELECT_ONE_WORD:
	            errorText = mContext.getString(R.string.select_only_one_word);
	            break;
        }
		mWebView.loadData("", "text/html; charset=utf-8", "");
		mErrorView.setVisibility(View.VISIBLE);
		mErrorView.setText(errorText);
    }

	@Override
	public String getId() {
		return ID;
	}
}
