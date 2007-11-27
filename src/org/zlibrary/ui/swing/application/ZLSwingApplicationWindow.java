package org.zlibrary.ui.swing.application;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.zlibrary.core.application.ZLAction;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.application.menu.Menu;
import org.zlibrary.core.application.menu.MenuVisitor;
import org.zlibrary.core.application.menu.Menubar;
import org.zlibrary.core.application.menu.Menubar.PlainItem;
import org.zlibrary.core.application.menu.Menubar.Submenu;
import org.zlibrary.core.application.toolbar.Toolbar;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;
import org.zlibrary.ui.swing.view.ZLSwingViewWidget;

public class ZLSwingApplicationWindow extends ZLApplicationWindow {
	private final JFrame myFrame;
	private final JPanel myMainPanel;
	private final JToolBar myToolbar;
	private final Map<Toolbar.Item, Action> myItemActionMap = new HashMap<Toolbar.Item, Action>();
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
					Dimension size = getSize();
					myWidthOption.setValue(size.width);
					myHeightOption.setValue(size.height);
				}

				public void componentMoved(ComponentEvent event) {
					Point point = getLocation();
					myXOption.setValue(point.x);
					myYOption.setValue(point.y);
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
		visitor.processMenu(application().getMenubar());
	}
		
	private class MyMenuVisitor extends MenuVisitor {
		private final Stack<JMenuItem> myMenuStack = new Stack<JMenuItem>();

		private MyMenuVisitor(JMenuItem menu) {
			myMenuStack.push(menu);
		}
		protected void processSubmenuBeforeItems(Menubar.Submenu submenu) {
			JMenu menu = new JMenu(submenu.getMenuName());
			myMenuStack.peek().add(menu);
			myMenuStack.push(menu);	
		}
		protected void processSubmenuAfterItems(Menubar.Submenu submenu) {
			myMenuStack.pop();
		}
		protected void processItem(Menubar.PlainItem item) {
			JMenuItem menu = new JMenuItem(item.getName());
			menu.addActionListener(new MyMenuItemAction(item));
			myMenuStack.peek().add(menu);
		}
		protected void processSepartor(Menubar.Separator separator) {
			((JMenu)myMenuStack.peek()).addSeparator();
		}
	}
	
	private class MyMenuItemAction extends AbstractAction {
		private PlainItem myItem;
		
		MyMenuItemAction(PlainItem item) {
			myItem = item;
			//myItem.getActionId().
			putValue(Action.SHORT_DESCRIPTION, item.getName()); 
		    //????ZLAction zlaction = application().getAction(myItem.getActionId());
		    //myActionMap.put(zlaction, this);
		}
		
		public void actionPerformed(ActionEvent event) {
			application().doAction(myItem.getActionId());
		}

	}


	public void setCaption(String caption) {
		myFrame.setTitle(caption);
	}

	protected ZLSwingViewWidget createViewWidget() {
		ZLSwingViewWidget viewWidget = new ZLSwingViewWidget(ZLSwingViewWidget.Angle.DEGREES0);
		//myFrame.getRootPane()
		myMainPanel.add(viewWidget.getPanel(), BorderLayout.CENTER);
		return viewWidget;
	}

	public void addToolbarItem(Toolbar.Item item) {
		if (item instanceof Toolbar.ButtonItem) {
			Toolbar.ButtonItem buttonItem = (Toolbar.ButtonItem)item;
			Action action = new MyButtonAction(buttonItem);
			myToolbar.add(action);
			//myActionMap.put(item, action);
			myItemActionMap.put(item, action);
		} else {
			myToolbar.addSeparator();
		}
	}
	
	private class MyButtonAction extends AbstractAction {
		private Toolbar.ButtonItem myItem;
		
		MyButtonAction(Toolbar.ButtonItem item) {
			myItem = item;
			//myItem.getActionId().
			String iconFileName = "icons/toolbar/" + myItem.getIconName() + ".png";
			java.net.URL iconURL = getClass().getClassLoader().getResource(iconFileName);
			ImageIcon icon = (iconURL != null) ? new ImageIcon(iconURL) : new ImageIcon(iconFileName);
			putValue(Action.SMALL_ICON, icon); 
			putValue(Action.SHORT_DESCRIPTION, item.getTooltip()); 
		    //ZLAction zlaction = application().getAction(myItem.getActionId());
		    //myActionMap.put(zlaction, this);
		}
		
		public void actionPerformed(ActionEvent event) {
			onButtonPress(myItem);
		}
	}

	public void setToolbarItemState(Toolbar.Item item, boolean visible, boolean enabled) {
		Action action = myItemActionMap.get(item);
		
		if (action != null) {
			//action.setEnabled(enabled);
		}
		//setVisible()???		
		// TODO: implement
	}

	public void setToggleButtonState(Toolbar.ButtonItem item) {
		
		// TODO: implement
	}

	public void setFullscreen(boolean fullscreen) {
		this.myFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
	}
	
	public boolean isFullscreen() {
		return this.myFrame.isMaximumSizeSet();		
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
		System.exit(0);
		// TODO: implement
	}
}
