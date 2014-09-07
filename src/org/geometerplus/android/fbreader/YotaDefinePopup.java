package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Base64;
import android.view.Gravity;

import com.yotadevices.yotaphone2.fbreader.OxfordDefinition;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaDefinePopup extends YotaTranslatePopup implements OxfordDefinition.DefinitionResult{
	public final static String ID = "YotaDefinePopup";

	public YotaDefinePopup(FBReaderApp application, Context ctx, ContentResolver resolver) {
		super(application, ctx, resolver);
	}

	@Override
	protected void show_() {
		mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
		OxfordDefinition.getDefinition(mTextToTranslate, this);
	}

	@Override
	public void onObtainDefinition(OxfordDefinition.Definition result) {
		String base64 = Base64.encodeToString(result.Xml.getBytes(), Base64.DEFAULT);
		mWebView.loadData(base64, "text/html; charset=utf-8", "base64");
	}

	@Override
	public void onDefinitionError(OxfordDefinition.DefinitionResult.Error error) {

	}

	@Override
	public String getId() {
		return ID;
	}
}
