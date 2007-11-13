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

public class ZLSwingApplicationWindow extends ZLApplicationWindow {
	public ZLSwingApplicationWindow(ZLApplication application) {
		super(application);
		myFrame = new JFrame();
		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		myToolbar = new JToolBar();
		myToolbar.setFloatable(false);
		myFrame.getRootPane().setLayout(new BorderLayout());
		myFrame.getRootPane().add(myToolbar, BorderLayout.NORTH);
	}

	public void run() {
		myFrame.setSize(800, 600);
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
			Action action = new AbstractAction("tooltip text", new ImageIcon(iconFileName)) {
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
}
