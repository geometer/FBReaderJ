/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.application;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

public abstract class ZLApplication {
	public static ZLApplication Instance() {
		return ourInstance;
	}

	private static ZLApplication ourInstance;

	private static final String MouseScrollUpKey = "<MouseScrollDown>";
	private static final String MouseScrollDownKey = "<MouseScrollUp>";
	public static final String NoAction = "none";
	
	public final ZLIntegerRangeOption KeyDelayOption =
		new ZLIntegerRangeOption("Options", "KeyDelay", 0, 5000, 250);
	
	private ZLApplicationWindow myWindow;
	private ZLView myView;

	private final HashMap<String,ZLAction> myIdToActionMap = new HashMap<String,ZLAction>();
	private Menubar myMenubar;
	//private ZLTime myLastKeyActionTime;

	protected ZLApplication() {
		ourInstance = this;
		
		new MenubarCreator().read(ZLResourceFile.createResourceFile("data/default/menubar.xml"));
	}

	final Menubar getMenubar() {
		return myMenubar;
	}

	protected final void setView(ZLView view) {
		if (view != null) {
			myView = view;
			repaintView();
			onViewChanged();
		}
	}

	public final ZLView getCurrentView() {
		return myView;
	}

	final void setWindow(ZLApplicationWindow window) {
		myWindow = window;
	}

	public void initWindow() {
		myWindow.init();
		setView(myView);
	}

	public final void repaintView() {
		if (myWindow != null) {
			myWindow.repaintView();
		}
	}

	public final void scrollViewTo(int viewPage, int shift) {
		if (myWindow != null) {
			myWindow.scrollViewTo(viewPage, shift);
		}
	}

	public final void startViewAutoScrolling(int viewPage) {
		if (myWindow != null) {
			myWindow.startViewAutoScrolling(viewPage);
		}
	}

	public final void onRepaintFinished() {
		if (myWindow != null) {
			myWindow.refreshMenu();
		}
		for (ButtonPanel panel : myPanels) {
			panel.updateStates();
		}
	}

	public final void onViewChanged() {
		for (ButtonPanel panel : myPanels) {
			panel.hide();
		}
	}

	protected final void addAction(String actionId, ZLAction action) {
		myIdToActionMap.put(actionId, action);
	}

	public final boolean isActionVisible(String actionId) {
		final ZLAction action = myIdToActionMap.get(actionId);
		return (action != null) && action.isVisible();
	}
	
	public final boolean isActionEnabled(String actionId) {
		final ZLAction action = myIdToActionMap.get(actionId);
		return (action != null) && action.isEnabled();
	}
	
	public final void doAction(String actionId) {
		final ZLAction action = myIdToActionMap.get(actionId);
		if (action != null) {
			action.checkAndRun();
		}
	}

	//may be protected
	abstract public ZLKeyBindings keyBindings();
	
	public final boolean doActionByKey(String key) {		
		final String actionId = keyBindings().getBinding(key);
		if (actionId != null) {
			final ZLAction action = myIdToActionMap.get(keyBindings().getBinding(key));
			return (action != null) && action.checkAndRun();
		}
		return false;
	}

	public void rotateScreen() {
		if (myWindow != null) {
			myWindow.rotate();
		}
	}

	public boolean canRotateScreen() {
		if (myWindow != null) {
			return myWindow.canRotate();
		}
		return false;
	}

	public boolean closeWindow() {
		onWindowClosing();
		if (myWindow != null) {
			myWindow.close();
		}
		return true;
	}

	public void onWindowClosing() {
	}

	public abstract void openFile(ZLFile file);

	//Action
	static abstract public class ZLAction {
		public boolean isVisible() {
			return true;
		}

		public boolean isEnabled() {
			return isVisible();
		}
		
		public final boolean checkAndRun() {
			if (isEnabled()) {
				run();
				return true;
			}
			return false;
		}
		
		public boolean useKeyDelay() {
			return true;
		}
		
		abstract protected void run();
	}

	static public interface ButtonPanel {
		void updateStates();
		void hide();
	}
	private final HashSet<ButtonPanel> myPanels = new HashSet<ButtonPanel>();
	public final void registerButtonPanel(ButtonPanel panel) {
		myPanels.add(panel);
	}
	public final void unregisterButtonPanel(ButtonPanel panel) {
		myPanels.remove(panel);
	}
	
	//Menu
	static class Menu {
		public interface Item {
		}

		private final ArrayList myItems = new ArrayList();
		private final ZLResource myResource;

		Menu(ZLResource resource) {
			myResource = resource;
		}

		ZLResource getResource() {
			return myResource;
		}

		void addItem(String actionId) {
			myItems.add(new Menubar.PlainItem(myResource.getResource(actionId).getValue(), actionId));
		}
		
		void addSeparator() {
			myItems.add(new Menubar.Separator());
		}
		
		Menubar.Submenu addSubmenu(String key) {
			Menubar.Submenu submenu = new Menubar.Submenu(myResource.getResource(key));
			myItems.add(submenu);
			return submenu;
		}

		int size() {
			return myItems.size();
		}

		Item getItem(int index) {
			return (Item)myItems.get(index);
		}
	}
	
	//MenuBar
	public static final class Menubar extends Menu {
		public static final class PlainItem implements Item {
			public final String Name;
			public final String ActionId;

			public PlainItem(String name, String actionId) {
				Name = name;
				ActionId = actionId;
			}
		};

		public static final class Submenu extends Menu implements Item {
			public Submenu(ZLResource resource) {
				super(resource);
			}

			public String getMenuName() {
				return getResource().getValue();
			}
		};
		
		public static final class Separator implements Item {
		};
			
		public Menubar() {
			super(ZLResource.resource("menu"));
		}
	}

	//MenuVisitor
	static public abstract class MenuVisitor {
		public final void processMenu(ZLApplication application) {
			if (application.myMenubar != null) {
				processMenu(application.myMenubar);
			}
		}

		private final void processMenu(Menu menu) {
			final int size = menu.size();
			for (int i = 0; i < size; ++i) {
				final Menu.Item item = menu.getItem(i);
				if (item instanceof Menubar.PlainItem) {
					processItem((Menubar.PlainItem)item);
				} else if (item instanceof Menubar.Submenu) {
					Menubar.Submenu submenu = (Menubar.Submenu)item;
					processSubmenuBeforeItems(submenu);
					processMenu(submenu);
					processSubmenuAfterItems(submenu);
				} else if (item instanceof Menubar.Separator) {
					processSepartor((Menubar.Separator)item);
				}
			}
		}

		protected abstract void processSubmenuBeforeItems(Menubar.Submenu submenu);
		protected abstract void processSubmenuAfterItems(Menubar.Submenu submenu);
		protected abstract void processItem(Menubar.PlainItem item);
		protected abstract void processSepartor(Menubar.Separator separator);
	}
	
	private class MenubarCreator extends ZLXMLReaderAdapter {
		private static final String ITEM = "item";
		private static final String SUBMENU = "submenu";

		private final ArrayList mySubmenuStack = new ArrayList();

		public boolean dontCacheAttributeValues() {
			return true;
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			if (myMenubar == null) {
				myMenubar = new Menubar();
			}
			final ArrayList stack = mySubmenuStack;
			final Menu menu = stack.isEmpty() ? myMenubar : (Menu)stack.get(stack.size() - 1);
			if (ITEM == tag) {
				final String id = attributes.getValue("id");
				if (id != null) {
					menu.addItem(id);
				}
			} else if (SUBMENU == tag) {
				final String id = attributes.getValue("id");
				if (id != null) {
					stack.add(menu.addSubmenu(id));
				}
			}
			return false;
		}

		public boolean endElementHandler(String tag) {
			if (SUBMENU == tag) {
				final ArrayList stack = mySubmenuStack;
				if (!stack.isEmpty()) {
					stack.remove(stack.size() - 1);
				}
			}
			return false;
		}
	}
}
