package org.geometerplus.android.fbreader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;


public class YotaSelectionBSPopup extends YotaSelectionPopup {
    public final static String ID = "YotaBSSelectionPopup";

    public YotaSelectionBSPopup(FBReaderApp application, Context ctx) {
        super(application, ctx);
    }

    @Override
    public String getId() {
        return ID;
    }

    protected void fillLayout(View root) {
        root.setBackground(new ColorDrawable(Color.WHITE));
        final int[] views = {R.id.highlight, R.id.copy, R.id.share, R.id.translate, R.id.define};
        final String[] codes = {ActionCode.SELECTION_BOOKMARK, ActionCode.SELECTION_COPY_TO_CLIPBOARD,
                ActionCode.SELECTION_SHARE, ActionCode.SELECTION_TRANSLATE, ActionCode.SELECTION_DEFINE};
        for (int i = 0; i < views.length; ++i) {
            TextView view = (TextView)root.findViewById(views[i]);
            view.setBackgroundResource(R.drawable.yota_black_selection_bspopup_item_background);
            view.setTag(codes[i]);
            view.setOnClickListener(this);
        }
    }
}
