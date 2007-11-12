package org.zlibrary.core.application;

import java.util.Map;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.application.menu.Menubar;
import org.zlibrary.core.application.toolbar.Toolbar;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.options.ZLBooleanOption;
import org.zlibrary.options.ZLIntegerOption;
import org.zlibrary.options.ZLIntegerRangeOption;
import org.zlibrary.options.ZLOption;

public abstract class ZLApplication extends ZLApplicationBase {
	static final String MouseScrollUpKey = "<MouseScrollDown>";
	static final String MouseScrollDownKey = "<MouseScrollUp>";
	
	
	static final String ROTATION = "Rotation";
	static final String ANGLE = "Angle";
	static final String STATE = "State";
	static final String KEYBOARD = "Keyboard";
	static final String FULL_CONTROL = "FullControl";
	static final String CONFIG = "Config";
	static final String AUTO_SAVE = "AutoSave";
	static final String TIMEOUT = "Timeout";


	private ZLIntegerOption RotationAngleOption;
	private ZLIntegerOption AngleStateOption;

	private ZLBooleanOption KeyboardControlOption;

	private ZLBooleanOption ConfigAutoSavingOption;
	private ZLIntegerRangeOption ConfigAutoSaveTimeoutOption;

	private ZLIntegerRangeOption KeyDelayOption;
	
	private String myName;
	private ZLViewWidget myViewWidget;
	private ZLApplicationWindow myWindow;
	private ZLPaintContext myContext;
	private ZLView myInitialView;

	private Map<Integer,Action> myActionMap;
	private Toolbar myToolbar;
	private Menubar myMenubar;
	//private ZLTime myLastKeyActionTime;
	//private ZLMessageHandler myPresentWindowHandler;

	protected ZLApplication(String name) {
		super(name);
		myName = name;
		myContext = ZLibrary.getContext();
		
		RotationAngleOption = new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, ROTATION, ANGLE, ZLViewWidget.Angle.DEGREES90.getAngle());
		
		AngleStateOption = new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, STATE, ANGLE, ZLViewWidget.Angle.DEGREES0.getAngle());
		
		KeyboardControlOption = new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, KEYBOARD, FULL_CONTROL, false);
		ConfigAutoSavingOption = new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, CONFIG, AUTO_SAVE, true);
		ConfigAutoSaveTimeoutOption = new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, CONFIG, TIMEOUT, 1, 6000, 30);
		KeyDelayOption = new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, "Options", "KeyDelay", 0, 5000, 250);
		setMyViewWidget(null);
		myWindow = null;
		if (ConfigAutoSavingOption.getValue()) {
			//ZLOption.startAutoSave((int)(ConfigAutoSaveTimeoutOption.getValue()));
		}

		//myPresentWindowHandler = new PresentWindowHandler(this);
		//ZLCommunicationManager.instance().registerHandler("present", myPresentWindowHandler);
	}
	
	public Toolbar getToolbar() {
		return this.myToolbar;
	}

	public Menubar getMenubar() {
		return this.myMenubar;
	}


	protected void setView(ZLView view) {
		if (view == null) {
			return;
		}

		if (getMyViewWidget() != null) {
			getMyViewWidget().setView(view);
			resetWindowCaption();
			refreshWindow();
		} else {
			myInitialView = view;
		}
	}

	protected ZLView getCurrentView() {
		return (getMyViewWidget() != null) ? getMyViewWidget().getView() : null;
	}

	protected void quit() {
		if (myWindow != null) {
			//myWindow.close();
		}
	}

	void setWindow(ZLApplicationWindow window) {
		myWindow = window;
	}

	public void initWindow() {
		setMyViewWidget(myWindow.createViewWidget());
		myWindow.init();
		setView(myInitialView);
		
		
		/*if (KeyboardControlOption.getValue()) {
			grabAllKeys(true);
		}
		myWindow.init();
		setView(myInitialView);*/

	}

	public ZLPaintContext getContext() {
		return myContext;
	}

	public void refreshWindow() {
		if (getMyViewWidget() != null) {
			//myViewWidget.repaint();
		}
		if (myWindow != null) {
			//myWindow.refresh();
		}

	}

	public void resetWindowCaption() {
		if (myWindow != null) {
			ZLView view = getCurrentView();
			if (view != null) {
				myWindow.setCaption(view.caption());
			}
		}
	}
	
	protected void setFullscreen(boolean fullscreen) {
		if (myWindow != null) {
			//myWindow.setFullscreen(fullscreen);
		}
	}
	
	protected boolean isFullscreen() {
		return true;//(myWindow != null) && myWindow.isFullscreen();
	}
	
	protected void addAction(int actionId, Action action) {
		myActionMap.put(actionId, action);
	}

	public boolean isFullKeyboardControlSupported() {
		return true;//(myWindow != null) && myWindow.isFullKeyboardControlSupported();
	}
	
	public void grabAllKeys(boolean grab) {
		if (myWindow != null) {
			//myWindow.grabAllKeys(grab);
		}
	}

	public boolean isFingerTapEventSupported() {
		return true;//(myWindow != null) && myWindow.isFingerTapEventSupported();
	}
	
	public boolean isMousePresented() {
		return true;//(myWindow != null) && myWindow.isMousePresented();
	}
	
	public boolean isKeyboardPresented() {
		return true;//(myWindow != null) && myWindow.isKeyboardPresented();
	}
	
	public void trackStylus(boolean track) {
		if (getMyViewWidget() != null) {
			getMyViewWidget().trackStylus(track);
		}
	}

	public void setHyperlinkCursor(boolean hyperlink) {
		if (myWindow != null) {
			//myWindow.setHyperlinkCursor(hyperlink);
		}
	}

	public Action getAction(int actionId) {
		return myActionMap.get(actionId);
	}
	
	public boolean isActionVisible(int actionId) {
		Action a = getAction(actionId);
		return ((a != null) && a.isVisible());

	}
	
	public boolean isActionEnabled(int actionId) {
		Action action = getAction(actionId);
		return (action != null) && action.isEnabled();
	}
	
	public void doAction(int actionId) {
		Action action = getAction(actionId);
		if (action != null) {
			action.checkAndRun();
		}
	}

	abstract public ZLKeyBindings keyBindings();
	
	public void doActionByKey(String key) {
		Action a = getAction(keyBindings().getBinding(key));
		if ((a != null) &&
				(!a.useKeyDelay() /*||
				 (myLastKeyActionTime.millisecondsTo(ZLTime()) >= KeyDelayOption.getValue())*/)) {
			a.checkAndRun();
			//myLastKeyActionTime = ZLTime();
		}

	}

	public boolean closeView() {
		quit();
		return true;
	}
	
	public void openFile(String fileName) {}

	public void presentWindow() {
		if (myWindow != null) {
			//myWindow.present();
		}
	}

	public String lastCaller() {
		return null;//((PresentWindowHandler)myPresentWindowHandler).lastCaller();
	}
	
	public void resetLastCaller() {
		//((PresentWindowHandler)myPresentWindowHandler).resetLastCaller();
	}

	public void setMyViewWidget(ZLViewWidget myViewWidget) {
		this.myViewWidget = myViewWidget;
	}

	public ZLViewWidget getMyViewWidget() {
		return myViewWidget;
	}
}

