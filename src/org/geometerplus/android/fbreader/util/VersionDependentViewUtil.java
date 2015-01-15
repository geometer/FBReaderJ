package org.geometerplus.android.fbreader.util;

import java.util.HashMap;
import java.util.Map;

import org.geometerplus.android.util.DeviceType;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

public abstract class VersionDependentViewUtil {

	public static final int ACTION_BAR_COLOR = Color.DKGRAY;
	private static final String SHOW_BOOK_INFO = "bookInfo";

	public static volatile boolean myShowStatusBarFlag;
	public static volatile boolean myShowActionBarFlag;
	public static volatile boolean myActionBarIsVisible;

	private static final HashMap<MenuItem,String> myMenuItemMap = new HashMap<MenuItem,String>();
	private static MenuItem.OnMenuItemClickListener myMenuListener;
	
	public static interface ActionProvider {
		void runAction(String id);
		boolean isActionVisible(String id);
		boolean isActionEnabled(String id);
		ZLBoolean3 isActionChecked(String id);
	}
	
	public static interface WidgetPreserver {
		void setPreserveSize(boolean visible);
	}


	public static void doAtOnCreate(Activity a, View titleView, final ActionProvider p, ZLBooleanOption actionBarOption, ZLBooleanOption statusBarOption, String title) {
		myShowActionBarFlag = actionBarOption.getValue();
		myShowStatusBarFlag = statusBarOption.getValue();	
		myActionBarIsVisible = myShowActionBarFlag;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			a.requestWindowFeature(Window.FEATURE_NO_TITLE);
		} else {
			doAtOnCreateNew(a, titleView, p);
		}
		a.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
				);
		a.setTitle(title);
		myMenuListener = new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				p.runAction(myMenuItemMap.get(item));
				return true;
			}
		};
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void doAtOnCreateNew(Activity a, View titleView, final ActionProvider p) {
		if (!myShowActionBarFlag) {
			a.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}
		final ActionBar bar = a.getActionBar();
		bar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
				);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			bar.setDisplayUseLogoEnabled(false);
		}
		titleView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				p.runAction(SHOW_BOOK_INFO);
			}
		});
		bar.setCustomView(titleView);
		bar.setBackgroundDrawable(new ColorDrawable(ACTION_BAR_COLOR));
	}

	public static void doAtOnStartRunnable(Activity a, final Intent i, ZLBooleanOption actionBarOption, ZLBooleanOption statusBarOption) {
		final boolean showStatusBar = statusBarOption.getValue();
		final boolean showActionBar =
				Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? 
						myShowActionBarFlag : 
							actionBarOption.getValue();
		if (showStatusBar != myShowStatusBarFlag || showActionBar != myShowActionBarFlag) {
			a.finish();
			a.startActivity(i);
		}

		statusBarOption.saveSpecialValue();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			actionBarOption.saveSpecialValue();
		}
	}

	public static void addMenuItemInternal(Menu menu, String actionId, Integer iconId, String name) {
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				setupMenuInActionBar(menuItem, myActionBarIsVisible);
			}
		}
		menuItem.setOnMenuItemClickListener(myMenuListener);
		myMenuItemMap.put(menuItem, actionId);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void setupMenuInActionBar(MenuItem menuItem, boolean showInActionBar) {
		if (showInActionBar) {
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}
	}

	public static void setStatusBarVisibility(Activity a, boolean visible, WidgetPreserver p) {
		if (DeviceType.Instance() != DeviceType.KINDLE_FIRE_1ST_GENERATION && !myShowStatusBarFlag) {
			p.setPreserveSize(visible);
			if (visible) {
				a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
		}
	}
	
	public static void ensureFullscreen(View view, ZLBooleanOption fullscreenModeOption, ZLBooleanOption buttonLightsOption) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
				&& fullscreenModeOption.getValue()) {
			view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE |
				View.SYSTEM_UI_FLAG_IMMERSIVE |
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
				View.SYSTEM_UI_FLAG_FULLSCREEN |
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			);
		} else if (buttonLightsOption.getValue()) {
			view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE
			);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void hideBarsInternal(Activity a, View rootView, ZLBooleanOption fullscreenModeOption, ZLBooleanOption buttonLightsOption, WidgetPreserver p) {
		if (!myShowActionBarFlag) {
			a.getActionBar().hide();
			myActionBarIsVisible = false;
			a.invalidateOptionsMenu();
		}

		ensureFullscreen(rootView, fullscreenModeOption, buttonLightsOption);

		setStatusBarVisibility(a, false, p);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void showBarsInternal(Activity a, View rootView, WidgetPreserver p) {
		setStatusBarVisibility(a, true, p);
		a.getActionBar().show();
		myActionBarIsVisible = true;
		a.invalidateOptionsMenu();
		rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	public static void setTitle(Activity a, CharSequence title) {
		a.setTitle(title);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setTitleNew(a, title);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void setTitleNew(Activity a, CharSequence title) {
		final TextView view = (TextView)(a.getActionBar().getCustomView());
		if (view != null) {
			view.setText(title);
			view.postInvalidate();
		}
	}

	public static void refreshMenu(ActionProvider p) {
		for (Map.Entry<MenuItem,String> entry : myMenuItemMap.entrySet()) {
			final String actionId = entry.getValue();
			final MenuItem menuItem = entry.getKey();
			menuItem.setVisible(p.isActionVisible(actionId) && p.isActionEnabled(actionId));
			switch (p.isActionChecked(actionId)) {
			case B3_TRUE:
				menuItem.setCheckable(true);
				menuItem.setChecked(true);
				break;
			case B3_FALSE:
				menuItem.setCheckable(true);
				menuItem.setChecked(false);
				break;
			case B3_UNDEFINED:
				menuItem.setCheckable(false);
				break;
			}
		}
	}

}


