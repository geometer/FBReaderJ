package org.zlibrary.ui.swing.application;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.application.toolbar.ButtonItem;
import org.zlibrary.core.application.toolbar.Item;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;
import org.zlibrary.ui.swing.view.ZLSwingViewWidget;

public class ZLSwingApplicationWindow extends ZLApplicationWindow {
	private final JFrame myFrame;
	private final JToolBar myToolbar;
	private final Map<Item, Action> myItemActionMap = new HashMap<Item, Action>();

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
		myFrame.getRootPane().setLayout(new BorderLayout());
		myFrame.getRootPane().add(myToolbar, BorderLayout.NORTH);
	}

	public void run() {
		myToolbar.setVisible(true);
		myFrame.setVisible(true);
	}

	public void init() {
		super.init();
	}

	public void initMenu() {
	}

	public void setCaption(String caption) {
		myFrame.setTitle(caption);
	}

	protected ZLSwingViewWidget createViewWidget() {
		ZLSwingViewWidget viewWidget = new ZLSwingViewWidget(ZLSwingViewWidget.Angle.DEGREES0);
		myFrame.getRootPane().add(viewWidget.getPanel(), BorderLayout.CENTER);
		return viewWidget;
	}

	public void addToolbarItem(Item item) {
		if (item.getType() == Item.Type.BUTTON) {
			ButtonItem buttonItem = (ButtonItem)item;
			Action action = new MyButtonAction(buttonItem);
			myToolbar.add(action);
			myItemActionMap.put(item, action);
		} else {
			myToolbar.addSeparator();
		}
	}
	
	private class MyButtonAction extends AbstractAction {
		private ButtonItem myItem;
		
		MyButtonAction(ButtonItem item) {
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

	public void setToolbarItemState(Item item, boolean visible, boolean enabled) {
		Action action = myItemActionMap.get(item);
		if (action != null) {
			action.setEnabled(enabled);
		}
		//setVisible()???		
		// TODO: implement
	}

	public void setToggleButtonState(ButtonItem item) {
		
		// TODO: implement
	}

	public void setFullscreen(boolean fullscreen) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.myFrame.setSize(screenSize);
	}
	
	public boolean isFullscreen() {
		return (myFrame.getSize() == Toolkit.getDefaultToolkit().getScreenSize());		
	}

}
