package org.geometerplus.android.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

public class YotaSelectionPopup extends ZLApplication.PopupPanel implements View.OnClickListener {
    final static String ID = "YotaSelectionPopup";

    private FBReaderApp mReaderApp;
    private android.widget.PopupWindow mPopup;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View mRootView;

    private int mSelectionStart;
    private int mSelectionEnd;

    protected YotaSelectionPopup(FBReaderApp application, Context ctx) {
        super(application);
        mReaderApp = application;
        mContext = ctx;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View root = mLayoutInflater.inflate(R.layout.yota_selection_popup, null);

        final int[] views = {R.id.highlight, R.id.copy, R.id.share, R.id.translate, R.id.define};
        final String[] codes = {ActionCode.SELECTION_BOOKMARK, ActionCode.SELECTION_COPY_TO_CLIPBOARD,
                ActionCode.SELECTION_SHARE, ActionCode.SELECTION_TRANSLATE, ActionCode.SELECTION_DEFINE};
        for (int i = 0; i < views.length; ++i) {
            TextView view = (TextView)root.findViewById(views[i]);
            view.setTag(codes[i]);
            view.setOnClickListener(this);
        }

        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(root);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
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
        mPopup.showAtLocation(mRootView, Gravity.HORIZONTAL_GRAVITY_MASK, 0, mSelectionEnd);
    }

    public void move(int selectionStart, int selectionEnd) {
        mSelectionStart = selectionStart;
        mSelectionEnd = selectionEnd;
    }

    @Override
    public void onClick(View v) {
        String action = (String)v.getTag();
        mReaderApp.runAction(action);
    }

    public void setRootView(View root) {
        mRootView = root;
    }
}
