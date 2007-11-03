package org.zlibrary.core.application;

import org.zlibrary.core.view.ZLViewWidget;

abstract public class ZLApplicationWindow {
	protected ZLApplicationWindow(ZLApplication application) {
		myApplication = application;
		myApplication.setWindow(this);
	}

	public ZLApplication application() {
		return myApplication;
	}

	abstract public void init();
	abstract public void initMenu();

	/*
	void onButtonPress(ZLApplication::Toolbar::ButtonItem &button);
	// TODO: change to pure virtual
	virtual void setToggleButtonState(const ZLApplication::Toolbar::ButtonItem&) {}
	// TODO: change to pure virtual
	virtual void setToolbarItemState(ZLApplication::Toolbar::ItemPtr item, bool visible, bool enabled) {}
	*/

	abstract protected ZLViewWidget createViewWidget();

	/*
	virtual void addToolbarItem(ZLApplication::Toolbar::ItemPtr item) = 0;

	// TODO: change to non-virtual (?)
	virtual void refresh();
	// TODO: change to pure virtual
	virtual void present() {}

	virtual void close() = 0;
*/

	abstract public void setCaption(String caption);

/*
	virtual bool isFullKeyboardControlSupported() const = 0;
	virtual void grabAllKeys(bool grab) = 0;

	virtual bool isFingerTapEventSupported() const = 0;
	virtual bool isMousePresented() const = 0;
	virtual bool isKeyboardPresented() const = 0;

	virtual void setFullscreen(bool fullscreen) = 0;
	virtual bool isFullscreen() const = 0;

	// TODO: change to pure virtual (?)
	virtual void setHyperlinkCursor(bool) {}

public:
	virtual ~ZLApplicationWindow();

private:
	bool myToggleButtonLock;

friend class ZLApplication;
*/
	private ZLApplication myApplication;
}
