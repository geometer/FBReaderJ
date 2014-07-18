package com.yotadevices.sdk;

import com.yotadevices.platinum.R;
import com.yotadevices.sdk.Constants.SystemBSFlags;
import com.yotadevices.sdk.utils.EinkUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class is used to draw on back screen
 */
public class BSDrawer extends Drawer {

    private static final int DPI = 240;
    private static final int BS_SCREEN_WIDTH = 540;
    private static final int BS_SCREEN_HEIGHT = 960;

    /**
     * Back Screen width
     */
    public static final int SCREEN_WIDTH = BS_SCREEN_WIDTH;
    /**
     * Back Screen height
     */
    public static final int SCREEN_HEIGHT = BS_SCREEN_HEIGHT;

    private static final int TYPE_DISPLAY_EPD = getDisplayTypeEPD();
    private static int TYPE_LAYOUT_EPD = getLayoutTypeEpd();

    private BSActivity mActivity;

    private Context mContext;
    private Context mEpdContext;
    private ViewGroup mParentView;
    private ViewGroup mBlankView;

    private boolean isShowEpdView;
    private boolean isShowBlankView;
    private LayoutInflater mEpdInflater;

    private int mStatusBarHeight;
    private int mNavigationBarHeight;

    public BSDrawer(BSActivity activity) {
        mActivity = activity;
        mContext = activity.getContext();
        final Resources res = mContext.getResources();

        mNavigationBarHeight = res.getDimensionPixelSize(R.dimen.navigation_panel_height);
        mStatusBarHeight = res.getDimensionPixelSize(R.dimen.status_bar_height);

        initDisplay();
    }

    private void initDisplay() {
        DisplayManager dm = (DisplayManager) getContext().getSystemService(Context.DISPLAY_SERVICE);

        for (Display d : dm.getDisplays()) {
            if (getTypeDisplay(d) == TYPE_DISPLAY_EPD) {
                initEpdDisplay(d);
            }
        }
    }

    private int getTypeDisplay(Display d) {
        try {
            Method m = android.view.Display.class.getDeclaredMethod("getType");
            return (Integer) m.invoke(d, new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int getDisplayTypeEPD() {
        try {
            Field field = android.view.Display.class.getDeclaredField("TYPE_EPD");
            return (Integer) field.get(null);
        } catch (Exception unused) {
            return 6;// magic type EPD
        }
    }

    private static int getLayoutTypeEpd() {
        try {
            Field field = android.view.WindowManager.LayoutParams.class.getDeclaredField("TYPE_EPD");
            return (Integer) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void initEpdDisplay(Display d) {
        mEpdContext = mContext.createDisplayContext(d);
        mEpdInflater = (LayoutInflater) mEpdContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentView = createParentEpdView(mEpdContext);
        mBlankView = createParentEpdView(mEpdContext);
        showBlankView();
    }

    private void showBlankView() {
        getWindowManager().addView(mBlankView, getDefaultLayoutParams());
        isShowBlankView = true;
    }

    private void hideBlankView() {
        getWindowManager().removeView(mBlankView);
        isShowBlankView = false;
    }

    private WindowManager getWindowManager() {
        return mEpdContext != null ? (WindowManager) mEpdContext.getSystemService(Context.WINDOW_SERVICE) : null;
    }

    private LayoutParams getDefaultLayoutParams() {
        return new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, TYPE_LAYOUT_EPD, 0, -1);
    }

    private LayoutParams applySystemIUVisibility(LayoutParams lp, int visibility) {
        if (lp != null) {
            boolean hideNavigation = (visibility & SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION) != 0;
            boolean hideStatusBar = (visibility & SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_STATUS_BAR) != 0;

            int y = hideStatusBar ? 0 : mStatusBarHeight;
            int height = BS_SCREEN_HEIGHT - y - (hideNavigation ? 0 : mNavigationBarHeight);

            lp.y = y;
            lp.height = height;
            lp.gravity = Gravity.TOP;
        }
        return lp;
    }

    private ViewGroup createParentEpdView(Context epdContext) {
        ViewGroup group = new FrameLayout(epdContext);
        group.setBackgroundColor(Color.BLACK);
        return group;
    }

    /**
     * @hide
     */
    private boolean isShowBSParentView() {
        return isShowEpdView;
    }

    /**
     * @hide only for inner usage
     */
    @Override
    public void addBSParentView() {
        WindowManager wm = getWindowManager();
        LayoutParams lp = getDefaultLayoutParams();
        applySystemIUVisibility(lp, mActivity.getSsytemBSUiVisibility());

        wm.addView(mParentView, lp);
        //When BS layout is added we perform FULL update to remove all ghosting from previous BSActivity
        //EinkUtils.performSingleUpdate(mParentView, Waveform.WAVEFORM_GC_FULL);
        //EinkUtils.disableViewDithering(mParentView);
        isShowEpdView = true;

        if (isShowBlankView) {
            hideBlankView();
        }
    }

    /**
     * @hide only for inner usage
     */
    @Override
    public void removeBSParentView() {
        if (isShowBSParentView()) {
            WindowManager wm = (WindowManager) mEpdContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mParentView);
            isShowEpdView = false;
        }
    }

    private Context getContext() {
        return mContext;
    }

    /**
     * Adds a child view with the specified layout parameters.
     * 
     * @param child
     *            the child view to add
     * @param params
     *            the layout parameters to set on the child
     */
    @Override
    public void addViewToBS(View child, ViewGroup.LayoutParams params) {
        mParentView.addView(child, params);
    }

    /**
     * <p>
     * Adds a child view. If no layout parameters are already set on the child,
     * the default parameters for this ViewGroup are set on the child.
     * </p>
     * 
     * @param child
     *            the child view to add
     */
    public void addViewToBS(View child) {
        mParentView.addView(child);
    }

    /**
     * Look for a child view with the given id. If this view has the given id,
     * return this view.
     * 
     * @param id
     *            The id to search for.
     * @return The view that has the given id in the hierarchy or null
     */
    public View findViewById(int id) {
        return mParentView != null ? mParentView.findViewById(id) : null;
    }

    /**
     * @return Back screen context for creating a view.
     */
    @Override
    public Context getBSContext() {
        return mEpdContext;
    }

    /**
     * Quick access to the {@link LayoutInflater} instance that this Window
     * retrieved from its back screen Context.
     * 
     * @return LayoutInflater The LayoutInflater for the back screen.
     */
    @Override
    public LayoutInflater getBSLayoutInflater() {
        return mEpdInflater;
    }

    /**
     * Removes a view from the back screen.
     * 
     * @param view
     *            the view to remove from back screen
     */
    @Override
    public void removeViewFromBS(View view) {
        if (isShowBSParentView()) {
            mParentView.removeView(view);
        }
    }

    @Override
    public void updateViewLayout(int visibility) {
        if (isShowBSParentView()) {
            WindowManager wm = getWindowManager();
            LayoutParams lp = getDefaultLayoutParams();
            applySystemIUVisibility(lp, visibility);

            wm.updateViewLayout(mParentView, lp);
        }
    }

    ViewGroup getParentView() {
        return mParentView;
    }

}
