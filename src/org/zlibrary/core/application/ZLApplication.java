package org.zlibrary.core.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLViewWidget;

public abstract class ZLApplication {
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


	public final ZLIntegerOption RotationAngleOption;
	public final ZLIntegerOption AngleStateOption;

	public final ZLBooleanOption KeyboardControlOption;

	public final ZLBooleanOption ConfigAutoSavingOption;
	public final ZLIntegerRangeOption ConfigAutoSaveTimeoutOption;

	public final ZLIntegerRangeOption KeyDelayOption;
	
	private String myName;
	private ZLViewWidget myViewWidget;
	private ZLApplicationWindow myWindow;
	private ZLPaintContext myContext;
	private ZLView myInitialView;

	private Map<Integer,ZLAction> myActionMap = new HashMap<Integer,ZLAction>();
	private Toolbar myToolbar;
	private Menubar myMenubar;
	//private ZLTime myLastKeyActionTime;
	//private ZLMessageHandler myPresentWindowHandler;

	
	//from ZLBaseAplication
	private static String ourDefaultFilesPathPrefix;
	
	{
		RotationAngleOption = new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, ROTATION, ANGLE, ZLViewWidget.Angle.DEGREES90.getDegrees());
		AngleStateOption = new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, STATE, ANGLE, ZLViewWidget.Angle.DEGREES0.getDegrees());	
		KeyboardControlOption = new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, KEYBOARD, FULL_CONTROL, false);
		ConfigAutoSavingOption = new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, CONFIG, AUTO_SAVE, true);
		ConfigAutoSaveTimeoutOption = new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, CONFIG, TIMEOUT, 1, 6000, 30);
		KeyDelayOption = new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, "Options", "KeyDelay", 0, 5000, 250);
		
	}
	
	protected ZLApplication() {
		myName = ZLibrary.getInstance().getApplicationName();
		myContext = ZLibrary.getInstance().createPaintContext();
		
		setViewWidget(null);
		myWindow = null;
		if (ConfigAutoSavingOption.getValue()) {
			//ZLOption.startAutoSave((int)(ConfigAutoSaveTimeoutOption.getValue()));
		}

		//myPresentWindowHandler = new PresentWindowHandler(this);
		//ZLCommunicationManager.instance().registerHandler("present", myPresentWindowHandler);
	}
	
	public Toolbar getToolbar() {
		if (myToolbar == null) {
			myToolbar = new Toolbar();
		}
		return myToolbar;
	}

	public Menubar getMenubar() {
		if (myMenubar == null) {
			myMenubar = new Menubar();
		}
		return myMenubar;
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

	private void quit() {
		if (myWindow != null) {
			myWindow.close();
		}
	}

	void setWindow(ZLApplicationWindow window) {
		myWindow = window;
	}

	public void initWindow() {
		setViewWidget(myWindow.createViewWidget());
		if (KeyboardControlOption.getValue()) {
			grabAllKeys(true);
		}
		myWindow.init();
		setView(myInitialView);
	}

	public ZLPaintContext getContext() {
		return myContext;
	}

	public void refreshWindow() {
		if (getMyViewWidget() != null) {
			myViewWidget.repaint();
		}
		if (myWindow != null) {
			myWindow.refresh();
		}

	}

	private void resetWindowCaption() {
		if (myWindow != null) {
			ZLView view = getCurrentView();
			if (view != null) {
				myWindow.setCaption(view.caption());
			}
		}
	}
	
	private void setFullscreen(boolean fullscreen) {
		if (myWindow != null) {
		    myWindow.setFullscreen(fullscreen);
		}
	}
	
	protected boolean isFullscreen() {
		return (myWindow != null) && myWindow.isFullscreen();
	}
	
	protected void addAction(int actionId, ZLAction action) {
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
		return (myWindow != null) && myWindow.isFingerTapEventSupported();
	}
	
	public boolean isMousePresented() {
		return (myWindow != null) && myWindow.isMousePresented();
	}
	
	public boolean isKeyboardPresented() {
		return (myWindow != null) && myWindow.isKeyboardPresented();
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

	private ZLAction getAction(int actionId) {
		return myActionMap.get(actionId);
	}
	
	public boolean isActionVisible(int actionId) {
		ZLAction a = getAction(actionId);
		return ((a != null) && a.isVisible());

	}
	
	public boolean isActionEnabled(int actionId) {
		ZLAction action = getAction(actionId);
		return (action != null) && action.isEnabled();
	}
	
	public void doAction(int actionId) {
		ZLAction action = getAction(actionId);
		if (action != null) {
			action.checkAndRun();
		}
	}

	abstract public ZLKeyBindings keyBindings();
	
	public void doActionByKey(String key) {
		ZLAction a = getAction(keyBindings().getBinding(key));
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

	void setViewWidget(ZLViewWidget myViewWidget) {
		this.myViewWidget = myViewWidget;
	}

	public ZLViewWidget getMyViewWidget() {
		return myViewWidget;
	}


	
	public static String getDefaultFilesPathPrefix() {
		return ourDefaultFilesPathPrefix;
	}
	
	//Action
	static abstract public class ZLAction {
		
		public boolean isVisible() {
			return true;
		}

		public boolean isEnabled() {
			return isVisible();
		}
		
		public void checkAndRun() {
			if (isEnabled()) {
				run();
			}
		}
		
		public boolean useKeyDelay() {
			return true;
		}
		
		abstract protected void run();
	}

	//full screen action
	protected static class FullscreenAction extends ZLAction {
		private final ZLApplication myApplication;
		private	final boolean myIsToggle;

		public FullscreenAction(ZLApplication application, boolean toggle) {
			this.myApplication = application;
			this.myIsToggle = toggle;
		}
		
		public boolean isVisible() {
			return myIsToggle || !myApplication.isFullscreen();
		}
		
		public void run() {
			myApplication.setFullscreen(!myApplication.isFullscreen());
		}
	}
    
	//rotation action
	protected static class RotationAction extends ZLAction {
		private ZLApplication myApplication;

		public RotationAction(ZLApplication application) {
			myApplication = application;
		}
		
		public boolean isVisible() {
			return (myApplication.getMyViewWidget() != null) &&
			 ((myApplication.RotationAngleOption.getValue() != ZLViewWidget.Angle.DEGREES0.getDegrees()) ||
				(myApplication.getMyViewWidget().getRotation() != ZLViewWidget.Angle.DEGREES0));

		}
		
		public void run() {
			int optionValue = (int)myApplication.RotationAngleOption.getValue();
			ZLViewWidget.Angle oldAngle = myApplication.getMyViewWidget().getRotation();
			ZLViewWidget.Angle newAngle = ZLViewWidget.Angle.DEGREES0;
			if (optionValue == -1) {
				switch (oldAngle) {
					case DEGREES0:
						newAngle = ZLViewWidget.Angle.DEGREES90;
						break;
					case DEGREES90:
						newAngle = ZLViewWidget.Angle.DEGREES180;
						break;
					case DEGREES180:
						newAngle = ZLViewWidget.Angle.DEGREES270;
						break;
					case DEGREES270:
						newAngle = ZLViewWidget.Angle.DEGREES0;
						break;
				}
			} else {
				newAngle = (oldAngle == ZLViewWidget.Angle.DEGREES0) ?
						ZLViewWidget.Angle.getByDegrees(optionValue) : ZLViewWidget.Angle.DEGREES0;
			}
			myApplication.getMyViewWidget().rotate(newAngle);
			myApplication.AngleStateOption.setValue(newAngle.getDegrees());
			myApplication.refreshWindow();		
		}
	}
	
	//toolbar
	static public class Toolbar {
		private final List<Item> myItems;
		private final ZLResource myResource;

		Toolbar() {
			myItems = new LinkedList<Item>();
			myResource = ZLResource.resource("toolbar");
		}
		
		public void addButton(int actionId, ZLResourceKey key) {
			addButton(actionId, key, null);
		}

		private void addButton(int actionId, ZLResourceKey key, ButtonGroup group) {
			ButtonItem button = new ButtonItem(actionId, key.Name, myResource.getResource(key));
			myItems.add(button);
			button.setButtonGroup(group);
		}
		
		ButtonGroup createButtonGroup(int unselectAllButtonsActionId) {
			return new ButtonGroup(unselectAllButtonsActionId);
		}
		
		/*public void addOptionEntry(ZLOptionEntry entry) {
			if (entry != null) {
				myItems.add(new OptionEntryItem(entry));
			}
		}*/
		
		public void addSeparator() {
			myItems.add(new SeparatorItem());
		}

		List<Item> getItems() {
			return Collections.unmodifiableList(myItems);
		}
		
		public interface Item {
		}
		
		public class ButtonItem implements Item {
			private int myActionId;
			private String myIconName;
			private ZLResource myTooltip;
			private	ButtonGroup myButtonGroup;
			
			public ButtonItem(int actionId, String iconName, ZLResource tooltip) {
				myActionId = actionId;
				myIconName = iconName;
				myTooltip = tooltip;
			}

			int getActionId() {
				return myActionId;
			}
			
			public String getIconName() {
				return myIconName;
			}
			
			public String getTooltip() {
				if (!myTooltip.hasValue()) {
					return "";
				}
				return myTooltip.value();
			}

			ButtonGroup getButtonGroup() {
				return myButtonGroup;
			}
			
			boolean isToggleButton() {
				return myButtonGroup != null;
			}
			
			void press() {
				if (isToggleButton()) { 
					myButtonGroup.press(this);
				}
			}
			
			boolean isPressed() {
				return isToggleButton() && (this == myButtonGroup.PressedItem);
			}

			private void setButtonGroup(ButtonGroup bg) {
				if (myButtonGroup != null) {
					myButtonGroup.Items.remove(this);
				}
				
				myButtonGroup = bg;
				
				if (myButtonGroup != null) {
					myButtonGroup.Items.add(this);
				}
			}	
		}
		
		public class SeparatorItem implements Item {
		}
		
		public class OptionEntryItem implements Item {
			//private ZLOptionEntry myOptionEntry;

			//public OptionEntryItem(ZLOptionEntry entry) {
				//myOptionEntry = entry;
			//}
				
			//public ZLOptionEntry entry() {
			//	return myOptionEntry;
			//}
		}

		public class ButtonGroup {
			public int UnselectAllButtonsActionId;
			public	Set<ButtonItem> Items = new HashSet<ButtonItem>();
			public	ButtonItem PressedItem;

			ButtonGroup(int unselectAllButtonsActionId) {
				UnselectAllButtonsActionId = unselectAllButtonsActionId;
				PressedItem = null;
			}
			
			void press(ButtonItem item) {
				PressedItem = item;
			}
		}
	}
	
	
	//Menu
	static public class Menu {
		public interface Item {
		}

		private final List<Item> myItems = new LinkedList<Item>();;
		private final ZLResource myResource;

		Menu(ZLResource resource) {
			myResource = resource;
		}

		ZLResource getResource() {
			return myResource;
		}

		public void addItem(int actionId, ZLResourceKey key) {
			myItems.add(new Menubar.PlainItem(myResource.getResource(key).value(), actionId));
		}
		
		public void addSeparator() {
			myItems.add(new Menubar.Separator());
		}
		
		public Menu addSubmenu(ZLResourceKey key) {
			Menubar.Submenu submenu = new Menubar.Submenu(myResource.getResource(key));
			myItems.add(submenu);
			return submenu;
		}

		List<Item> getItems() {
			return Collections.unmodifiableList(myItems);
		}
	}
	
	//MenuBar
	static public class Menubar extends Menu {
		public static class PlainItem implements  Item {
			private String myName;
			private int myActionId;

			public  PlainItem() {}
			public PlainItem(String name, int actionId) {
				myName = name;
				myActionId = actionId;
			}

			public String getName() {
				return myName;
			}
			
			public int getActionId() {
				return myActionId;
			}
		};

		public static class Submenu extends Menu implements Item {
			public Submenu(ZLResource resource) {
				super(resource);
			}

			public String getMenuName() {
				return getResource().value();
			}
		};
		
		public static class Separator implements Item {
		};
			
		public Menubar() {
			super(ZLResource.resource("menu"));
		}
	}

    //MenuVisitor
	static public abstract class MenuVisitor {
		public void processMenu(Menu menu) {
			for (Menu.Item item : menu.getItems()) {
				if (item instanceof Menubar.PlainItem) {
					processItem((Menubar.PlainItem)item);
				} else if (item instanceof Menubar.Submenu) {
					Menubar.Submenu submenu = (Menubar.Submenu)item;
					processSubmenuBeforeItems(submenu);
					processMenu(submenu);
					processSubmenuAfterItems(submenu);
				} else if (item instanceof Menubar.Separator) {
					processSepartor((Menubar.Separator)item);
				}
			}
		}

		protected abstract void processSubmenuBeforeItems(Menubar.Submenu submenu);
		protected abstract void processSubmenuAfterItems(Menubar.Submenu submenu);
		protected abstract void processItem(Menubar.PlainItem item);
		protected abstract void processSepartor(Menubar.Separator separator);
	}
	
	static public class PresentWindowHandler {//extends ZLMessageHandler {
		private ZLApplication myApplication;
		private String myLastCaller;

		//public PresentWindowHandler(ZLApplication application);
		//public void onMessageReceived(List<String> arguments);
		//public String lastCaller();
		//public void resetLastCaller();
	}
}

