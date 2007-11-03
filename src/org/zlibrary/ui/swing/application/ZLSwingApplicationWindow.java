package org.zlibrary.ui.swing.application;

import javax.swing.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.view.ZLViewWidget;

import org.zlibrary.ui.swing.view.ZLSwingViewWidget;
import org.zlibrary.ui.swing.view.ZLSwingPaintContext;

public class ZLSwingApplicationWindow extends ZLApplicationWindow {
	public ZLSwingApplicationWindow(ZLApplication application) {
		super(application);
		myFrame = new JFrame();
		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void run() {
		myFrame.setSize(300, 300);
		myFrame.setVisible(true);
	}

	public void init() {
	}

	public void initMenu() {
	}

	public void setCaption(String caption) {
		myFrame.setTitle(caption);
	}

	protected ZLViewWidget createViewWidget() {
		ZLSwingViewWidget viewWidget = new ZLSwingViewWidget(ZLSwingViewWidget.Angle.DEGREES0);
		myFrame.add(viewWidget.getPanel());
		return viewWidget;
	}

	private JFrame myFrame;
}
