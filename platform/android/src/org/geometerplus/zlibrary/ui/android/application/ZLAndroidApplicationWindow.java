/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import android.view.Menu;
import android.view.MenuItem;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;

import org.geometerplus.zlibrary.ui.android.view.ZLAndroidViewWidget;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

public final class ZLAndroidApplicationWindow extends ZLApplicationWindow {
	private final HashMap<MenuItem,ZLApplication.Menubar.PlainItem> myMenuItemMap =
		new HashMap<MenuItem,ZLApplication.Menubar.PlainItem>();

	private class MenuBuilder extends ZLApplication.MenuVisitor {
		private int myItemCount = Menu.FIRST;
		private final Stack<Menu> myMenuStack = new Stack<Menu>();

		private MenuBuilder(Menu menu) {
			myMenuStack.push(menu);
		}
		protected void processSubmenuBeforeItems(ZLApplication.Menubar.Submenu submenu) {
			myMenuStack.push(myMenuStack.peek().addSubMenu(0, myItemCount++, Menu.NONE, submenu.getMenuName()));	
		}
		protected void processSubmenuAfterItems(ZLApplication.Menubar.Submenu submenu) {
			myMenuStack.pop();
		}
		protected void processItem(ZLApplication.Menubar.PlainItem item) {
			MenuItem menuItem = myMenuStack.peek().add(0, myItemCount++, Menu.NONE, item.Name);
			menuItem.setOnMenuItemClickListener(myMenuListener);
			myMenuItemMap.put(menuItem, item);
		}
		protected void processSepartor(ZLApplication.Menubar.Separator separator) {
			//myMenuStack.peek().addSeparator(0, myItemCount++);
		}
	}

	private final MenuItem.OnMenuItemClickListener myMenuListener =
		new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				getApplication().doAction(myMenuItemMap.get(item).ActionId);
				return true;
			}
		};

	public ZLAndroidApplicationWindow(ZLApplication application) {
		super(application);
	}

	public void buildMenu(Menu menu) {
		new MenuBuilder(menu).processMenu(getApplication());
		refresh();
	}

	protected void refresh() {
		super.refresh();
		for (Map.Entry<MenuItem,ZLApplication.Menubar.PlainItem> entry : myMenuItemMap.entrySet()) {
			final String actionId = entry.getValue().ActionId;
			final ZLApplication application = getApplication();
			entry.getKey().setVisible(application.isActionVisible(actionId) && application.isActionEnabled(actionId));
		}
	}

	public void initMenu() {
		// TODO: implement
	}

	public void setCaption(String caption) {
		// TODO: implement
		//myFrame.setTitle(caption);
	}

	protected ZLAndroidViewWidget createViewWidget() {
		// TODO: implement
		ZLAndroidViewWidget viewWidget =
			new ZLAndroidViewWidget(getApplication().AngleStateOption.getValue());
		return viewWidget;
	}

	public void addToolbarItem(ZLApplication.Toolbar.Item item) {
		// TODO: implement
	}

	public void setToolbarItemState(ZLApplication.Toolbar.Item item, boolean visible, boolean enabled) {
		// TODO: implement
	}

	public void closeInternal() {
		((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).finish();
	}

	//public void setToggleButtonState(ZLApplication.Toolbar.ButtonItem item) {
		// TODO: implement
	//}

	public void setFullscreen(boolean fullscreen) {
		// TODO: implement
	}

	public boolean isFullscreen() {
		// TODO: implement
		return false;
	}
}
