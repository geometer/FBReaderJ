package org.zlibrary.ui.j2me.application;

import org.zlibrary.core.application.*;
import org.zlibrary.core.view.ZLViewWidget;

import org.zlibrary.ui.j2me.view.*;

public class ZLJ2MEApplicationWindow extends ZLApplicationWindow {
	private final ZLCanvas myCanvas;

	public ZLJ2MEApplicationWindow(ZLApplication application, ZLCanvas canvas) {
		super(application);
		myCanvas = canvas;
	}

	protected void initMenu() {
		// TODO: implement
	}

	public void setToolbarItemState(ZLApplication.Toolbar.Item item, boolean visible, boolean enabled) {
		// TODO: implement
	}
	
	protected ZLViewWidget createViewWidget() {
		return new ZLJ2MEViewWidget(myCanvas);
	}
	
	public void addToolbarItem(ZLApplication.Toolbar.Item item) {
		// TODO: implement
	}

	public void close() {
		// TODO: implement
	}

	public void setCaption(String caption) {
		// TODO: implement
	}

	public void setFullscreen(boolean fullscreen) {
		// TODO: implement
	}

	public boolean isFullscreen() {
		// TODO: implement
		return false;
	}
}
