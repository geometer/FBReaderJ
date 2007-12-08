package org.zlibrary.ui.swing.application;

import java.util.HashMap;
import java.util.Stack;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;
import org.zlibrary.ui.swing.view.ZLSwingViewWidget;

public class ZLSwingApplicationWindow extends ZLApplicationWindow {
	private final JFrame myFrame;
	private final JPanel myMainPanel;
	private final JToolBar myToolbar;
	private final HashMap<ZLApplication.Toolbar.ButtonItem, AbstractButton> myButtonItemToActionMap = new HashMap<ZLApplication.Toolbar.ButtonItem, AbstractButton>();
	//private final Map<Integer, Action> myIntegerActionMap = new HashMap<Integer, Action>();
	//private final Map<Menu.Item, Action> myMenuActionMap = new HashMap<Menu.Item, Action>();
	//private final Map<ZLAction, Action> myActionMap = new HashMap<ZLAction, Action>();

	private final JMenuBar myMenuBar;
	private final JMenu myMenu;
	
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
			setSize((int)myWidthOption.getValue(), (int)myHeightOption.getValue());
			setLocation((int)myXOption.getValue(), (int)myYOption.getValue());
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
				ZLSwingLibrary.shutdown();
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
		myMenuBar = new JMenuBar();
		myMenu = new JMenu("File");
		myMenuBar.add(myMenu);
		myMainPanel = new JPanel();
		myMainPanel.setLayout(new BorderLayout());
		myMainPanel.add(myToolbar, BorderLayout.NORTH);
		myFrame.add(myMainPanel);
		
		myFrame.setJMenuBar(myMenuBar);
		myFrame.addKeyListener(new MyKeyListener());
		myFrame.setFocusable(true);

	}

	public void run() {
		myToolbar.setVisible(true);
		myMenuBar.setVisible(true);
		myFrame.setVisible(true);
	}

	public void init() {
		super.init();
	}

	public void initMenu() {
		MyMenuVisitor visitor = new MyMenuVisitor(myMenu);
		visitor.processMenu(getApplication().getMenubar());
	}
		
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


	public void setCaption(String caption) {
		myFrame.setTitle(caption);
	}

	protected ZLSwingViewWidget createViewWidget() {
		ZLSwingViewWidget viewWidget = new ZLSwingViewWidget(
			ZLSwingViewWidget.Angle.getByDegrees(getApplication().AngleStateOption.getValue()));
		myMainPanel.add(viewWidget.getPanel(), BorderLayout.CENTER);
		return viewWidget;
	}

	public void addToolbarItem(ZLApplication.Toolbar.Item item) {
		if (item instanceof ZLApplication.Toolbar.ButtonItem) {
			ZLApplication.Toolbar.ButtonItem buttonItem = (ZLApplication.Toolbar.ButtonItem)item;
			MyButtonAction action = new MyButtonAction(buttonItem);
			AbstractButton button = myToolbar.add(action);
			button.setFocusable(false);
			myButtonItemToActionMap.put(buttonItem, button);
			
			//myItemActionMap.put(item, action);
		} else {
			myToolbar.addSeparator();
		}
	}
	
	private class MyButtonAction extends AbstractAction {
		private ZLApplication.Toolbar.ButtonItem myItem;
		
		MyButtonAction(ZLApplication.Toolbar.ButtonItem item) {
			myItem = item;
			String iconFileName = "icons/toolbar/" + myItem.getIconName() + ".png";
			java.net.URL iconURL = getClass().getClassLoader().getResource(iconFileName);
			ImageIcon icon = (iconURL != null) ? new ImageIcon(iconURL) : new ImageIcon(iconFileName);
			putValue(Action.SMALL_ICON, icon); 
			putValue(Action.SHORT_DESCRIPTION, item.getTooltip()); 
		}
		
		public void actionPerformed(ActionEvent event) {
			onButtonPress(myItem);
		}
	}

	public void setToolbarItemState(ZLApplication.Toolbar.Item item, boolean visible, boolean enabled) {
		// TODO: implement
		if (item instanceof ZLApplication.Toolbar.ButtonItem) {
			AbstractButton button = myButtonItemToActionMap.get((ZLApplication.Toolbar.ButtonItem)item);
			button.setEnabled(enabled);
			button.setVisible(visible);
		}
	}

	public void setToggleButtonState(ZLApplication.Toolbar.ButtonItem item) {
		// TODO: implement
	}

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
	
	public boolean isFingerTapEventSupported() {
		return false;
	}
	
	public boolean isMousePresented() {
		return true;
	}
	
	public boolean isKeyboardPresented() {
		return true;
	}
	
	public boolean isFullKeyboardControlSupported() {
		return true;
	}
	
	public void close() {
		ZLSwingLibrary.shutdown();
		System.exit(0);
		// TODO: implement
	}
	
	private class MyKeyListener extends KeyAdapter {
		 
		public void keyPressed(KeyEvent e) {			
			String keyCode = keyTextModifiersParse(e.getModifiersExText(e.getModifiersEx()))
										 + keyTextParse(e.getKeyText(e.getKeyCode()));
			//System.out.println(keyCode);
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
