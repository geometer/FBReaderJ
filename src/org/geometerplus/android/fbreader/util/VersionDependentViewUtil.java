package org.geometerplus.android.fbreader.util;

import java.util.HashMap;
import java.util.Map;

import org.geometerplus.android.util.DeviceType;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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

	public static interface FBReaderAppInterface {
		String getTitle();
		void runAction(String id, Object ... params);
		boolean isActionEnabled(String actionId);
		boolean isActionVisible(String actionId);
		ZLBoolean3 isActionChecked(String actionId);
	}

	public static interface FBReaderInterface {
		Activity getActivity();
		void setPreserveSize(boolean b);
		ZLBooleanOption getStatusBarOption();
		ZLBooleanOption getActionBarOption();
		ZLBooleanOption getFullscreenModeOption();
		ZLBooleanOption getButtonLightsOption();
		TextView getTitleView();
		View getRootView();
	}

	public static void doAtOnCreate(final FBReaderInterface fbreader, final FBReaderAppInterface fbreaderapp) {
		myShowActionBarFlag = fbreader.getActionBarOption().getValue();
		myShowStatusBarFlag = fbreader.getStatusBarOption().getValue();	
		myActionBarIsVisible = myShowActionBarFlag;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			fbreader.getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
		} else {
			doAtOnCreateNew(fbreader, fbreaderapp);
		}
		fbreader.getActivity().getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
				);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void doAtOnCreateNew(final FBReaderInterface fbreader, final FBReaderAppInterface fbreaderapp) {
		if (!myShowActionBarFlag) {
			fbreader.getActivity().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}
		final ActionBar bar = fbreader.getActivity().getActionBar();
		bar.setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
				);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			bar.setDisplayUseLogoEnabled(false);
		}
		fbreader.getTitleView().setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				fbreaderapp.runAction(SHOW_BOOK_INFO);
			}
		});
		bar.setCustomView(fbreader.getTitleView());
		bar.setBackgroundDrawable(new ColorDrawable(ACTION_BAR_COLOR));
		fbreader.getActivity().setTitle(fbreaderapp.getTitle());
		myMenuListener = new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				fbreaderapp.runAction(myMenuItemMap.get(item));
				return true;
			}
		};
	}

	public static void doAtOnStartRunnable(final FBReaderInterface fbreader, final Intent i) {
		final boolean showStatusBar = fbreader.getStatusBarOption().getValue();
		final boolean showActionBar =
				Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? 
						myShowActionBarFlag : 
							fbreader.getActionBarOption().getValue();
		if (showStatusBar != myShowStatusBarFlag || showActionBar != myShowActionBarFlag) {
			fbreader.getActivity().finish();
			fbreader.getActivity().startActivity(i);
		}

		fbreader.getStatusBarOption().saveSpecialValue();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			fbreader.getActionBarOption().saveSpecialValue();
		}
	}

	public static void addMenuItemInternal(Menu menu, String actionId, Integer iconId, String name, boolean showInActionBar) {
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				setupMenuInActionBar(menuItem, showInActionBar);
			}
			menuItem.setOnMenuItemClickListener(myMenuListener);
			myMenuItemMap.put(menuItem, actionId);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void setupMenuInActionBar(MenuItem menuItem, boolean showInActionBar) {
		if (showInActionBar) {
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}
	}

	public static void setStatusBarVisibility(FBReaderInterface i, boolean visible) {
		if (DeviceType.Instance() != DeviceType.KINDLE_FIRE_1ST_GENERATION && !myShowStatusBarFlag) {
			i.setPreserveSize(visible);
			if (visible) {
				i.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					i.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					i.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
				i.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void hideBarsInternal(FBReaderInterface i) {
		if (!myShowActionBarFlag) {
			i.getActivity().getActionBar().hide();
			myActionBarIsVisible = false;
			i.getActivity().invalidateOptionsMenu();
		}

		if (Build.VERSION.SDK_INT >= 19/*Build.VERSION_CODES.KITKAT*/
				&& i.getFullscreenModeOption().getValue()) {
			i.getRootView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE |
					2048 /*View.SYSTEM_UI_FLAG_IMMERSIVE*/ |
					4096 /*View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY*/ |
					4 /*View.SYSTEM_UI_FLAG_FULLSCREEN*/ |
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					);
		} else if (i.getButtonLightsOption().getValue()) {
			i.getRootView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE
					);
		}

		setStatusBarVisibility(i, false);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void showBarsInternal(FBReaderInterface i) {
		setStatusBarVisibility(i, true);
		i.getActivity().getActionBar().show();
		myActionBarIsVisible = true;
		i.getActivity().invalidateOptionsMenu();
		i.getRootView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	public static void setTitle(FBReaderInterface i, CharSequence title) {
		i.getActivity().setTitle(title);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setTitleNew(i.getActivity(), title);
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

	public static void refreshMenu(FBReaderAppInterface f) {
		for (Map.Entry<MenuItem,String> entry : myMenuItemMap.entrySet()) {
			final String actionId = entry.getValue();
			final MenuItem menuItem = entry.getKey();
			menuItem.setVisible(f.isActionVisible(actionId) && f.isActionEnabled(actionId));
			switch (f.isActionChecked(actionId)) {
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


