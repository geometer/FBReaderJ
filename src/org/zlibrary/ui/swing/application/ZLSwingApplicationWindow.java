package org.zlibrary.ui.swing.application;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.application.toolbar.Item;
import org.zlibrary.core.application.toolbar.ButtonItem;
import org.zlibrary.core.view.ZLViewWidget;

import org.zlibrary.ui.swing.view.ZLSwingViewWidget;
import org.zlibrary.ui.swing.view.ZLSwingPaintContext;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;

import org.zlibrary.options.ZLOption;
import org.zlibrary.options.ZLIntegerRangeOption;

public class ZLSwingApplicationWindow extends ZLApplicationWindow {
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

	protected ZLViewWidget createViewWidget() {
		ZLSwingViewWidget viewWidget = new ZLSwingViewWidget(ZLSwingViewWidget.Angle.DEGREES0);
		myFrame.getRootPane().add(viewWidget.getPanel(), BorderLayout.CENTER);
		return viewWidget;
	}

	public void addToolbarItem(Item item) {
		// TODO: implement
		if (item.getType() == Item.Type.BUTTON) {
			ButtonItem buttonItem = (ButtonItem)item;
			String iconFileName = "icons/toolbar/" + buttonItem.getIconName() + ".png";
			java.net.URL iconURL = getClass().getClassLoader().getResource(iconFileName);
			ImageIcon icon = (iconURL != null) ? new ImageIcon(iconURL) : new ImageIcon(iconFileName);
			Action action = new AbstractAction("tooltip text", icon) {
				public void actionPerformed(ActionEvent event) {
				}
			};
			myToolbar.add(action);
		} else {
			myToolbar.addSeparator();
		}
	}

	public void setToolbarItemState(Item item, boolean visible, boolean enabled) {
		// TODO: implement
	}

	public void setToggleButtonState(ButtonItem item) {
		// TODO: implement
	}

	private JFrame myFrame;
	private JToolBar myToolbar;

	final private ZLIntegerRangeOption myXOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "XPosition", 0, 2000, 10);
	final private ZLIntegerRangeOption myYOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "YPosition", 0, 2000, 10);
	final private ZLIntegerRangeOption myWidthOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "Width", 10, 2000, 800);
	final private ZLIntegerRangeOption myHeightOption =
		new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "Height", 10, 2000, 600);
}
