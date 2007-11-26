package org.zlibrary.ui.android.application;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLApplicationWindow;
import org.zlibrary.core.application.toolbar.Item;
import org.zlibrary.core.application.toolbar.ButtonItem;

import org.zlibrary.ui.android.view.ZLAndroidViewWidget;

public class ZLAndroidApplicationWindow extends ZLApplicationWindow {
	public ZLAndroidApplicationWindow(ZLApplication application) {
		super(application);
	}

	public void init() {
		// TODO: implement
		super.init();
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
		//ZLSwingViewWidget viewWidget = new ZLSwingViewWidget(ZLSwingViewWidget.Angle.DEGREES0);
		//myFrame.getRootPane().add(viewWidget.getPanel(), BorderLayout.CENTER);
		//return viewWidget;
		return ZLAndroidViewWidget.Instance;
	}

	public void addToolbarItem(Item item) {
		// TODO: implement
	}

	public void setToolbarItemState(Item item, boolean visible, boolean enabled) {
		// TODO: implement
	}

	public void close() {
		// TODO: implement
	}

	public void setToggleButtonState(ButtonItem item) {
		// TODO: implement
	}

	public void setFullscreen(boolean fullscreen) {
		// TODO: implement
	}

	public boolean isFullscreen() {
		// TODO: implement
		return false;
	}

	public boolean isFingerTapEventSupported() {
		// TODO: implement
		return false;
	}

	public boolean isMousePresented() {
		// TODO: implement
		return true;
	}

	public boolean isKeyboardPresented() {
		// TODO: implement
		return true;
	}

	public boolean isFullKeyboardControlSupported() {
		// TODO: implement
		return true;
	}
}
