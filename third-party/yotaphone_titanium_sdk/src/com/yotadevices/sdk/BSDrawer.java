package com.yotadevices.sdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import com.yotadevices.sdk.Constants.SystemBSFlags;
import com.yotadevices.sdk.utils.EinkUtils;

import java.lang.ref.WeakReference;
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
    public static int TYPE_LAYOUT_EPD = getLayoutTypeEpd("TYPE_EPD");
    public static int TYPE_LAYOUT_NAVIGATION_BAR = getLayoutTypeEpd("TYPE_NAVIGATION_BAR");

    private WeakReference<BSActivity> mActivity;

    private Context mContext;
    private Context mEpdContext;
    private ViewGroup mParentView;
    private ViewGroup mBlankView;

    private LayoutInflater mEpdInflater;

    private int mNavigationBarHeight;
    private boolean isEpdInit;
    private int mLayoutType = TYPE_LAYOUT_EPD;

    public BSDrawer(BSActivity activity) {
        mActivity = new WeakReference<BSActivity>(activity);
        mContext = activity.getContext();
        final Resources res = mContext.getResources();

        mNavigationBarHeight = res.getDimensionPixelSize(R.dimen.navigation_panel_height);
        initDisplay();
    }

    public BSDrawer(BSActivity activity, int layoutType) {
        this(activity);
        mLayoutType = layoutType;
    }

    public void setLayoutType(int layoutType) {
        mLayoutType = layoutType;
    }

    private void initDisplay() {
        Display display = getEpdDisplay(mContext);
        if (display != null)
            initEpdDisplay(display);
    }

    private static Display getEpdDisplay(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

        for (Display d : dm.getDisplays()) {
            if (getTypeDisplay(d) == TYPE_DISPLAY_EPD) {
                return d;
            }
        }

        return null;
    }

    private static int getTypeDisplay(Display d) {
        try {
            Method m = android.view.Display.class.getDeclaredMethod("getType");
            return (Integer) m.invoke(d, new Object[]{});
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

    private static int getLayoutTypeEpd(String type) {
        try {
            Field field = android.view.WindowManager.LayoutParams.class.getDeclaredField(type);
            return (Integer) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Return EPD display context. May be null, when EDP display is not added.
     *
     * @param context Main application context
     * @return A Context for the EPD display.
     */
    public static Context createEpdContext(Context context) {
        Display d = getEpdDisplay(context);
        return d != null ? context.createDisplayContext(d) : null;
    }

    private void initEpdDisplay(Display d) {
        mEpdContext = mContext.createDisplayContext(d);
        mEpdInflater = (LayoutInflater) mEpdContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mParentView = createParentEpdView(mEpdContext);
        mBlankView = createParentEpdView(mEpdContext);
        isEpdInit = true;
    }

    /**
     * Check the possibility of using the back screen
     *
     * @return <b>true</b> if EPD display is attached and all methods for EDP
     * display are available, otherwise <b>false</b>.
     */
    public boolean isInit() {
        return isEpdInit;
    }

    public void showBlankView() {
        if (isInit() && !isShowBSParentView() && !isShowBlankView()) {
            getWindowManager().addView(mBlankView, getDefaultLayoutParams());
        }
    }

    private void hideBlankView() {
        if (isInit() && isShowBlankView()) {
            getWindowManager().removeView(mBlankView);
        }
    }

    private WindowManager getWindowManager() {
        return isInit() ? (WindowManager) mEpdContext.getSystemService(Context.WINDOW_SERVICE) : null;
    }

    private LayoutParams getDefaultLayoutParams() {
        return new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, mLayoutType,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, -1);
    }

    private LayoutParams applySystemIUVisibility(LayoutParams lp, int visibility) {
        if (lp != null) {
            boolean hideNavigation = (visibility & SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION) != 0;
            boolean translucentNavigation = (visibility & SystemBSFlags.SYSTEM_BS_UI_FLAG_TRANSLUCENT_NAVIGATION) != 0;

            int y = 0;
            // int height = BS_SCREEN_HEIGHT - y - ((hideNavigation ||
            // translucentNavigation) ? 0 : mNavigationBarHeight);

            lp.y = y;
            // lp.height = WindowManager.LayoutParams.MATCH_PARENT;// height;
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
    private synchronized boolean isShowBSParentView() {
        return mParentView != null && mParentView.isShown();
    }

    private synchronized boolean isShowBlankView() {
        return mBlankView != null && mBlankView.isShown();
    }

    /**
     * @param initialWaveform
     * @param initialDithering
     * @hide only for inner usage
     */
    @Override
    public synchronized void addBSParentView(final Waveform initialWaveform, final Dithering initialDithering) {
        if (isInit() && !isShowBSParentView()) {
            WindowManager wm = getWindowManager();
            LayoutParams lp = getDefaultLayoutParams();

            final BSActivity activity = mActivity.get();
            if (activity != null) {
                activity.onPrepareLayoutParams(lp);
                applySystemIUVisibility(lp, activity.getSytemBSUiVisibility());
            }
            final Waveform prevWaveform = EinkUtils.getViewWaveform(mParentView);
            final Dithering prevDithering = EinkUtils.getViewDithering(mParentView);
            EinkUtils.setViewDithering(mParentView, initialDithering);
            EinkUtils.setViewWaveform(mParentView, initialWaveform);

            mParentView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.get().sendRequest(InnerConstants.RequestFramework.UNFREEZE_EPD);
                            EinkUtils.setViewDithering(mParentView, prevDithering);
                            EinkUtils.setViewWaveform(mParentView, prevWaveform);
                        }
                    },100);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });

            wm.addView(mParentView, lp);

            // When BS layout is added we perform FULL update to remove all
            // ghosting
            // from previous BSActivity
            // EinkUtils.performSingleUpdate(mParentView, initialWaveform,
            // initialDithering, 0);

            hideBlankView();
        }
    }

    /**
     * @hide only for inner usage
     */
    @Override
    public synchronized void removeBSParentView() {
        if (isInit() && isShowBSParentView()) {
            WindowManager wm = (WindowManager) mEpdContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mParentView);
        }
    }

    private Context getContext() {
        return mContext;
    }

    /**
     * Adds a child view with the specified layout parameters.
     *
     * @param child  the child view to add
     * @param params the layout parameters to set on the child
     */
    @Override
    public void addViewToBS(View child, ViewGroup.LayoutParams params) {
        if (isInit()) {
            mParentView.addView(child, params);
        }
    }

    /**
     * <p>
     * Adds a child view. If no layout parameters are already set on the child,
     * the default parameters for this ViewGroup are set on the child.
     * </p>
     *
     * @param child the child view to add
     */
    public void addViewToBS(View child) {
        if (isInit()) {
            mParentView.addView(child);
        }
    }

    /**
     * Look for a child view with the given id. If this view has the given id,
     * return this view.
     *
     * @param id The id to search for.
     * @return The view that has the given id in the hierarchy or null
     */
    public View findViewById(int id) {
        return isInit() ? mParentView.findViewById(id) : null;
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
     * @param view the view to remove from back screen
     */
    @Override
    public void removeViewFromBS(View view) {
        if (isInit() && isShowBSParentView()) {
            mParentView.removeView(view);
        }
    }

    @Override
    public void updateViewLayout(int visibility) {
        if (isInit() && isShowBSParentView()) {
            WindowManager wm = getWindowManager();
            LayoutParams lp = getDefaultLayoutParams();
            BSActivity activity = mActivity.get();
            if (activity != null) {
                activity.onPrepareLayoutParams(lp);
            }
            applySystemIUVisibility(lp, visibility);

            wm.updateViewLayout(mParentView, lp);
        }
    }

    public ViewGroup getParentView() {
        return mParentView;
    }

}
