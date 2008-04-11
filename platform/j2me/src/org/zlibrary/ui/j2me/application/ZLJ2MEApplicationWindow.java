package org.geometerplus.zlibrary.ui.j2me.application;

import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.ui.j2me.view.*;

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
