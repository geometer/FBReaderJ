package org.geometerplus.android.fbreader;

import android.content.ContentResolver;
import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class YotaDefineBSPopup extends YotaDefinePopup {
    public static String ID = "YotaDefineBSPopup";
    public YotaDefineBSPopup(FBReaderApp application, Context ctx, ContentResolver resolver) {
        super(application, ctx, resolver);
    }

    @Override
    public String getId() {
        return ID;
    }
}
