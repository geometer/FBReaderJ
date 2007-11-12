package org.zlibrary.core.application;

import java.util.List;
import java.util.Set;

import org.zlibrary.core.application.toolbar.ButtonGroup;
import org.zlibrary.core.application.toolbar.ButtonItem;
import org.zlibrary.core.application.toolbar.Item;
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

		List<Item> toolbarItems = myApplication.getToolbar().items();
		for (Item item: toolbarItems) {
			addToolbarItem(item);
		}

		initMenu();
	}
	abstract public void initMenu();

	
	public void onButtonPress(ButtonItem button) {
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
				ButtonGroup group = button.buttonGroup();
				Set<ButtonItem> items = group.Items;
				for (ButtonItem bitem: items) {
					setToggleButtonState(bitem);
				}
			}
			myToggleButtonLock = false;
		}
		application().doAction(button.actionId());
	}
	
	public abstract void setToggleButtonState(ButtonItem item);
	
	public abstract void setToolbarItemState(Item item, boolean visible, boolean enabled);
	
	abstract protected ZLViewWidget createViewWidget();
	
	abstract public void addToolbarItem(Item item);

	// TODO: change to non-virtual (?)
	public void refresh() {
		/*
		List<Item> items = application().getToolbar().items();
		boolean enableToolbarSpace = false;
		Item lastSeparator = null;
		for (Item item: items) {
			switch (item.type()) {
			case ItemType.OPTION_ENTRY:
			{
				boolean visible = ((Toolbar.OptionEntryItem)**it).entry()->isVisible();
						if (visible) {
							if (!lastSeparator.isNull()) {
								setToolbarItemState(lastSeparator, true, true);
								lastSeparator = 0;
							}
							enableToolbarSpace = true;
						}
						setToolbarItemState(*it, visible, true);
					}
					break;
				case ZLApplication::Toolbar::Item::BUTTON:
					{
						const ZLApplication::Toolbar::ButtonItem &button = (const ZLApplication::Toolbar::ButtonItem&)**it;
						int id = button.actionId();
	        
						const bool visible = application().isActionVisible(id);
						const bool enabled = application().isActionEnabled(id);
	        
						if (visible) {
							if (!lastSeparator.isNull()) {
								setToolbarItemState(lastSeparator, true, true);
								lastSeparator = 0;
							}
							enableToolbarSpace = true;
						}
						if (!enabled && button.isPressed()) {
							shared_ptr<ZLApplication::Toolbar::ButtonGroup> group = button.buttonGroup();
							group->press(0);
							application().doAction(group->UnselectAllButtonsActionId);
							myToggleButtonLock = true;
							setToggleButtonState(button);
							myToggleButtonLock = false;
						}
						setToolbarItemState(*it, visible, enabled);
					}
					break;
				case ZLApplication::Toolbar::Item::SEPARATOR:
					if (enableToolbarSpace) {
						lastSeparator = *it;
						enableToolbarSpace = false;
					} else {
						setToolbarItemState(*it, false, true);
					}
					break;
			}
		}
		if (!lastSeparator.isNull()) {
			setToolbarItemState(lastSeparator, false, true);
		}*/
	}
	// TODO: change to pure virtual
	//virtual void present() {}

	//virtual void close() = 0;
//*/

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
}
