/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.application;

import java.util.*;

import android.app.Activity;

import android.view.Menu;
import android.view.MenuItem;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import org.geometerplus.android.util.UIUtil;

public final class ZLAndroidApplicationWindow extends ZLApplicationWindow {
	private final HashMap<MenuItem,String> myMenuItemMap = new HashMap<MenuItem,String>();

	private final MenuItem.OnMenuItemClickListener myMenuListener =
		new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				getApplication().doAction(myMenuItemMap.get(item));
				return true;
			}
		};

	public ZLAndroidApplicationWindow(ZLApplication application) {
		super(application);
	}

	public void addMenuItem(Menu menu, String actionId, Integer iconId, String name) {
		if (name == null) {
			name = ZLResource.resource("menu").getResource(actionId).getValue();
		}
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
		}
		menuItem.setOnMenuItemClickListener(myMenuListener);
		myMenuItemMap.put(menuItem, actionId);
	}

	@Override
	public void refreshMenu() {
		for (Map.Entry<MenuItem,String> entry : myMenuItemMap.entrySet()) {
			final String actionId = entry.getValue();
			final ZLApplication application = getApplication();
			entry.getKey().setVisible(application.isActionVisible(actionId) && application.isActionEnabled(actionId));
		}
	}
	
	@Override
	public void wait(String key, Runnable action) {
		final Activity activity = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getActivity();
		if (activity != null) {
			UIUtil.wait(key, action, activity);
		} else {
			action.run();
		}
	}

	@Override
	public void setTitle(final String title) {
		final Activity activity = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getActivity();
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					activity.setTitle(title);
				}
			});
		}
	}

	protected ZLViewWidget getViewWidget() {
		return ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
	}

	@Override
	public void rotate() {
		((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).rotateScreen();
	}

	public boolean canRotate() {
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		return zlibrary.AutoOrientationOption.getValue();
	}

	public void close() {
		((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).finish();
	}

	private int myBatteryLevel;
	protected int getBatteryLevel() {
		return myBatteryLevel;
	}
	public void setBatteryLevel(int percent) {
		myBatteryLevel = percent;
	}
}
