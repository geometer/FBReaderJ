package org.geometerplus.android.fbreader;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;

import com.yotadevices.yotaphone2.fbreader.UIUtils;
import com.yotadevices.yotaphone2.yotareader.R;


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
        final boolean whiteTheme = mReaderApp.ViewOptions.getColorProfile().Name.equals(ColorProfile.YOTA_BS_WHITE);
        root.setBackground(new ColorDrawable(whiteTheme ? Color.WHITE : Color.BLACK));
        final int[] views = {R.id.highlight, R.id.copy, R.id.share, R.id.translate, R.id.define};
        final String[] codes = {ActionCode.SELECTION_BOOKMARK, ActionCode.SELECTION_COPY_TO_CLIPBOARD,
                ActionCode.SELECTION_SHARE, ActionCode.SELECTION_TRANSLATE, ActionCode.SELECTION_DEFINE};
        for (int i = 0; i < views.length; ++i) {
            TextView view = (TextView)root.findViewById(views[i]);
            view.setBackgroundResource(whiteTheme ?
                    R.drawable.yota_black_selection_bspopup_item_background :
                    R.drawable.yota_white_selection_bspopup_item_background);
            if (whiteTheme) {
                view.setTextColor(mContext.getResources().getColorStateList(R.color.yota_black_selection_bspopup_item_text));
            }
            else {
                view.setTextColor(mContext.getResources().getColorStateList(R.color.yota_white_selection_bspopup_item_text));
            }
	        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            view.setTag(codes[i]);
            view.setOnClickListener(this);
	        view.getPaint().setAntiAlias(false);
        }
	    if (UIUtils.Region.getRegion() == UIUtils.Region.CHINA) {
		    root.findViewById(R.id.translate).setVisibility(View.GONE);
	    }
    }

    @Override
    protected void show_() {
        fillLayout(mContentView);
        super.show_();
    }
}
