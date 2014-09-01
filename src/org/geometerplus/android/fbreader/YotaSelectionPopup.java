package org.geometerplus.android.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yotadevices.yotaphone2.fbreader.UIUtils;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

public class YotaSelectionPopup extends ZLApplication.PopupPanel implements View.OnClickListener {
    public final static String ID = "YotaFSSelectionPopup";

    private FBReaderApp mReaderApp;
    private android.widget.PopupWindow mPopup;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View mRootView;

    private int mPopupYOffset;
    private int mPopupHeight;
    private int mConstYOffset;

    public YotaSelectionPopup(FBReaderApp application, Context ctx) {
        super(application);
        mReaderApp = application;
        mContext = ctx;
        mConstYOffset = (int)UIUtils.convertDpToPixel(10, ctx);
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View root = mLayoutInflater.inflate(getLayoutId(), null);
        fillLayout(root);
        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(root);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
    }

    protected void fillLayout(View root) {
        final int[] views = {R.id.highlight, R.id.copy, R.id.share, R.id.translate, R.id.define};
        final String[] codes = {ActionCode.SELECTION_BOOKMARK, ActionCode.SELECTION_COPY_TO_CLIPBOARD,
                ActionCode.SELECTION_SHARE, ActionCode.SELECTION_TRANSLATE, ActionCode.SELECTION_DEFINE};
        for (int i = 0; i < views.length; ++i) {
            TextView view = (TextView)root.findViewById(views[i]);
            view.setTag(codes[i]);
            view.setOnClickListener(this);
        }
    }

    protected int getLayoutId() {
        return R.layout.yota_selection_popup;
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
        if (mPopupYOffset != 0) {
            mPopup.showAtLocation(mRootView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, mPopupYOffset);
        } else {
            mPopup.showAtLocation(mRootView, Gravity.CENTER, 0, 0);
        }
    }

    public void move(int selectionStart, int selectionEnd) {
        mPopup.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mPopupHeight = mPopup.getContentView().getMeasuredHeight();

        if (selectionStart - mPopupHeight - mConstYOffset >= 0) {
            mPopupYOffset = selectionStart - mPopupHeight - mConstYOffset;
        }
        else if (mRootView.getHeight() - selectionEnd >= (mPopupHeight + mConstYOffset)) {
            mPopupYOffset = selectionEnd + mConstYOffset;
        }
        else {
            mPopupYOffset = 0;
        }
    }

    @Override
    public void onClick(View v) {
        String action = (String)v.getTag();
        mReaderApp.runAction(action);
        hide_();
    }

    public void setRootView(View root) {
        mRootView = root;
    }
}
