/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.swing.application;

import java.util.HashMap;
import java.util.Stack;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;
import org.geometerplus.zlibrary.ui.swing.view.ZLSwingViewWidget;
import org.geometerplus.zlibrary.ui.swing.util.ZLSwingIconUtil;

@SuppressWarnings("serial")
public class ZLSwingApplicationWindow extends ZLApplicationWindow {
	private final JFrame myFrame;
	private final JPanel myMainPanel;
	private final JToolBar myToolbar;
	private final HashMap<ZLApplication.Toolbar.Item,JComponent> myToolbarMap =
		new HashMap<ZLApplication.Toolbar.Item,JComponent>();
	//private final Map<Integer, Action> myIntegerActionMap = new HashMap<Integer, Action>();
	//private final Map<Menu.Item, Action> myMenuActionMap = new HashMap<Menu.Item, Action>();
	//private final Map<ZLAction, Action> myActionMap = new HashMap<ZLAction, Action>();

	//private final JMenuBar myMenuBar;
	//private final JMenu myMenu;
	
	private final ZLIntegerRangeOption myXOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "XPosition", 0, 2000, 10);
	private final ZLIntegerRangeOption myYOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "YPosition", 0, 2000, 10);
	private final ZLIntegerRangeOption myWidthOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "Width", 10, 2000, 800);
	private final ZLIntegerRangeOption myHeightOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "Height", 10, 2000, 600);

	
	private class ZLFrame extends JFrame {
		ZLFrame() {
			setSize(myWidthOption.getValue(), myHeightOption.getValue());
			setLocation(myXOption.getValue(), myYOption.getValue());
			addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent event) {
					if (!isFullscreen()) {
						Dimension size = getSize();
						myWidthOption.setValue(size.width);
						myHeightOption.setValue(size.height);
					}
				}

				public void componentMoved(ComponentEvent event) {
					if (!isFullscreen()) {
						Point point = getLocation();
						myXOption.setValue(point.x);
						myYOption.setValue(point.y);
					}
				}
			});
		}

		protected void processWindowEvent(WindowEvent event) {
			if (event.getID() == WindowEvent.WINDOW_CLOSING) {
				close();
			}
			super.processWindowEvent(event);
		}
	}

	public ZLSwingApplicationWindow(ZLApplication application) {
		super(application);
		myFrame = new ZLFrame();
		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		myToolbar = new JToolBar();
		myToolbar.setFloatable(false);
		//myMenuBar = new JMenuBar();
		//myMenu = new JMenu("File");
		//myMenuBar.add(myMenu);
		myMainPanel = new JPanel();
		myMainPanel.setLayout(new BorderLayout());
		myMainPanel.add(myToolbar, BorderLayout.NORTH);
		myFrame.add(myMainPanel);
		
		//myFrame.setJMenuBar(myMenuBar);
		myFrame.addKeyListener(new MyKeyListener());
		myFrame.setFocusable(true);
	}

	public JFrame getFrame() {
		return myFrame;
	}

	public void run() {
		myToolbar.setVisible(true);
		//myMenuBar.setVisible(true);
		myFrame.setVisible(true);
	}

	public void init() {
		super.init();
	}

	public void initMenu() {
		//MyMenuVisitor visitor = new MyMenuVisitor(myMenu);
		//visitor.processMenu(getApplication());
	}
		
	/*
	private class MyMenuVisitor extends ZLApplication.MenuVisitor {
		private final Stack<JMenuItem> myMenuStack = new Stack<JMenuItem>();

		private MyMenuVisitor(JMenuItem menu) {
			myMenuStack.push(menu);
		}
		
		protected void processSubmenuBeforeItems(ZLApplication.Menubar.Submenu submenu) {
			JMenu menu = new JMenu(submenu.getMenuName());
			myMenuStack.peek().add(menu);
			myMenuStack.push(menu);	
		}
		
		protected void processSubmenuAfterItems(ZLApplication.Menubar.Submenu submenu) {
			myMenuStack.pop();
		}
		
		protected void processItem(ZLApplication.Menubar.PlainItem item) {
			JMenuItem menu = new JMenuItem(item.getName());
			menu.addActionListener(new MyMenuItemAction(item));
			myMenuStack.peek().add(menu);
		}
		protected void processSepartor(ZLApplication.Menubar.Separator separator) {
			((JMenu)myMenuStack.peek()).addSeparator();
		}
	}
	
	private class MyMenuItemAction extends AbstractAction {
		private ZLApplication.Menubar.PlainItem myItem;
		
		MyMenuItemAction(ZLApplication.Menubar.PlainItem item) {
			myItem = item;
			//myItem.getActionId().
			putValue(Action.SHORT_DESCRIPTION, item.getName()); 
			//????ZLAction zlaction = application().getAction(myItem.getActionId());
			//myActionMap.put(zlaction, this);
		}
		
		public void actionPerformed(ActionEvent event) {
			getApplication().doAction(myItem.getActionId());
		}

	}
	*/

	public void setCaption(String caption) {
		myFrame.setTitle(caption);
	}

	protected ZLSwingViewWidget createViewWidget() {
		ZLSwingViewWidget viewWidget =
			new ZLSwingViewWidget(getApplication().AngleStateOption.getValue());
		myMainPanel.add(viewWidget.getPanel(), BorderLayout.CENTER);
		return viewWidget;
	}

	public void addToolbarItem(ZLApplication.Toolbar.Item item) {
		if (item instanceof ZLApplication.Toolbar.ButtonItem) {
			MyButtonAction action = new MyButtonAction((ZLApplication.Toolbar.ButtonItem)item);
			AbstractButton button = myToolbar.add(action);
			button.setFocusable(false);
			myToolbarMap.put(item, button);
		} else if (item instanceof ZLApplication.Toolbar.SeparatorItem) {
			JToolBar.Separator separator = new JToolBar.Separator(null);
			myToolbar.add(separator);
			myToolbarMap.put(item, separator);
		} else {
			// TODO: implement
		}
	}
	
	private class MyButtonAction extends AbstractAction {
		private ZLApplication.Toolbar.ButtonItem myItem;
		
		MyButtonAction(ZLApplication.Toolbar.ButtonItem item) {
			myItem = item;
			final String iconFileName = "icons/toolbar/" + myItem.getIconName() + ".png";
			putValue(Action.SMALL_ICON, ZLSwingIconUtil.getIcon(iconFileName)); 
			putValue(Action.SHORT_DESCRIPTION, item.getTooltip()); 
		}
		
		public void actionPerformed(ActionEvent event) {
			onButtonPress(myItem);
		}
	}

	public void setToolbarItemState(ZLApplication.Toolbar.Item item, boolean visible, boolean enabled) {
		JComponent component = myToolbarMap.get(item);
		if (component != null) {
			component.setEnabled(enabled);
			component.setVisible(visible);
		}
	}

	//public void setToggleButtonState(ZLApplication.Toolbar.ButtonItem item) {
		// TODO: implement
	//}

	public void setFullscreen(boolean fullscreen) {
		if (fullscreen) {
			myFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
			//myFrame.setResizable(false);
		} else {
			//myFrame.setResizable(true);
			myFrame.setExtendedState(Frame.NORMAL);
		}
	}
	
	
	public boolean isFullscreen() {
		return myFrame.getExtendedState() == Frame.MAXIMIZED_BOTH;		
	}
	
	protected void closeInternal() {
		ZLSwingLibrary.shutdown();
		// TODO: implement
	}
	
	private class MyKeyListener extends KeyAdapter {
		 
		public void keyPressed(KeyEvent e) {			
			final String modifiers = e.getModifiersExText(e.getModifiersEx());
			final int code = e.getKeyCode();
			String main;
			switch (code) {
				case 27:
					main = "Esc";
					break;
				case 33:
					main = "Page Up";
					break;
				case 34:
					main = "Page Down";
					break;
				case 35:
					main = "End";
					break;
				case 36:
					main = "Home";
					break;
				case 37:
					main = "LeftArrow";
					break;
				case 38:
					main = "UpArrow";
					break;
				case 39:
					main = "RightArrow";
					break;
				case 40:
					main = "DownArrow";
					break;
				default:
					main = e.getKeyText(code);
					break;
			}
			final String keyCode = keyTextModifiersParse(modifiers) + keyTextParse(main);
			getApplication().doActionByKey(keyCode);
		}
		
		private String keyTextParse(String str) {
			if (str.equals("Left") || str.equals("Down") || 
					str.equals("Right") || str.equals("Up")) {
				str = str + "Arrow";
			} else if (str.equals("Escape")) {
				str = "Esc";
			} else if (str.equals("Equals")) {
				str = "=";
			} else if (str.equals("Minus")) {
				str = "-";
			} else if (str.startsWith("Page")) {
				str = "Page" + str.substring("Page".length() + 1, str.length());
			} else if (str.equals("Enter")) {
				str = "Return";
			}
			
			return "<" + str + ">";
		}
		
		private String keyTextModifiersParse(String str) {
			if (str.equals("")) {
				return "";
			}
			return "<" + str + ">+";
		}
	}
}
