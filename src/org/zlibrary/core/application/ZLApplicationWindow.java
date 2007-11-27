package org.zlibrary.core.application;

import java.util.List;
import java.util.Set;

import org.zlibrary.core.application.toolbar.Toolbar;
import org.zlibrary.core.view.ZLViewWidget;

abstract public class ZLApplicationWindow {
	private ZLApplication myApplication;
	private boolean myToggleButtonLock;

	protected ZLApplicationWindow(ZLApplication application) {
		myApplication = application;
		myApplication.setWindow(this);
		myToggleButtonLock = false;
	}

	public ZLApplication application() {
		return myApplication;
	}

	public void init() {
		myApplication.setMyViewWidget(createViewWidget());

		List<Toolbar.Item> toolbarItems = myApplication.getToolbar().items();
		for (Toolbar.Item item: toolbarItems) {
			addToolbarItem(item);
		}

		initMenu();
	}
	abstract public void initMenu();

	
	public void onButtonPress(Toolbar.ButtonItem button) {
		
		if (myToggleButtonLock) {
			return;
		}
		if (button.isToggleButton()) {
			myToggleButtonLock = true;
			if (button.isPressed()) {
				setToggleButtonState(button);
				myToggleButtonLock = false;
				return;
			} else {
				button.press();
				Toolbar.ButtonGroup group = button.getButtonGroup();
				Set<Toolbar.ButtonItem> items = group.Items;
				for (Toolbar.ButtonItem bitem: items) {
					setToggleButtonState(bitem);
				}
			}
			myToggleButtonLock = false;
		}
		
		
		application().doAction(button.getActionId());
	}
	
	public abstract void setToggleButtonState(Toolbar.ButtonItem item);
	
	public abstract void setToolbarItemState(Toolbar.Item item, boolean visible, boolean enabled);
	
	abstract protected ZLViewWidget createViewWidget();
	
	abstract public void addToolbarItem(Toolbar.Item item);

	public void refresh() {
		List<Toolbar.Item> items = application().getToolbar().items();
		boolean enableToolbarSpace = false;
		Toolbar.Item lastSeparator = null;
		for (Toolbar.Item item: items) {
			if (item instanceof Toolbar.OptionEntryItem) {
			/*case OPTION_ENTRY:
			{
				boolean visible = ((OptionEntryItem)item.entry().isVisible())//((Toolbar.OptionEntryItem)**it).entry()->isVisible();
						if (visible) {
							if (lastSeparator != null) {
								setToolbarItemState(lastSeparator, true, true);
								lastSeparator = null;
							}
							enableToolbarSpace = true;
						}
						setToolbarItemState(item, visible, true);
					}
					break;*/
			} else if (item instanceof Toolbar.ButtonItem) {
				Toolbar.ButtonItem button = (Toolbar.ButtonItem)item;
				int id = button.getActionId();
	        
				boolean visible = application().isActionVisible(id);
				boolean enabled = application().isActionEnabled(id);
	        
				if (visible) {
					if (lastSeparator != null) {
						setToolbarItemState(lastSeparator, true, true);
						lastSeparator = null;
					}
					enableToolbarSpace = true;
				}
				if (!enabled && button.isPressed()) {
					Toolbar.ButtonGroup group = button.getButtonGroup();
					group.press(null);
					application().doAction(group.UnselectAllButtonsActionId);
					myToggleButtonLock = true;
					setToggleButtonState(button);
					myToggleButtonLock = false;
				}
				setToolbarItemState(item, visible, enabled);
			} else if (item instanceof Toolbar.SeparatorItem) {
				if (enableToolbarSpace) {
					lastSeparator = item;
					enableToolbarSpace = false;
				} else {
					setToolbarItemState(item, false, true);
				}
				break;
			}
		}
		
		if (lastSeparator != null) {
			setToolbarItemState(lastSeparator, false, true);
		}
	}
	// TODO: change to pure virtual
	//virtual void present() {}

//*/
	abstract public void close();
	abstract public void setCaption(String caption);
	abstract public void setFullscreen(boolean fullscreen);
	abstract public boolean isFullscreen();

	abstract public boolean isFingerTapEventSupported();
	abstract public boolean isMousePresented();
	abstract public boolean isKeyboardPresented();
	abstract public boolean isFullKeyboardControlSupported();

/*
	virtual void grabAllKeys(bool grab) = 0;
	virtual void setHyperlinkCursor(bool) {}
*/
}
