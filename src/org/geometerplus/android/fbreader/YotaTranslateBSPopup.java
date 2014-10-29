package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaTranslateBSPopup extends YotaTranslatePopup {
	public final static String ID = "YotaTranslateBSPopup";

	public YotaTranslateBSPopup(FBReaderApp application, Context ctx, ContentResolver resolver) {
		super(application, ctx, resolver);
	}

	@Override
	public String getId() {
		return ID;
	}
}
