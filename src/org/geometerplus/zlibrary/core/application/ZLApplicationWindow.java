/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.application;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.view.ZLViewWidget;

abstract public class ZLApplicationWindow {
	private ZLApplication myApplication;
	//private boolean myToggleButtonLock;

	protected ZLApplicationWindow(ZLApplication application) {
		myApplication = application;
		myApplication.setWindow(this);
		//myToggleButtonLock = false;
	}

	public ZLApplication getApplication() {
		return myApplication;
	}

	protected void init() {
		myApplication.setViewWidget(createViewWidget());

		final ZLApplication.Toolbar toolbar = myApplication.getToolbar();
		if (toolbar != null) {
			final int size = toolbar.size();
			for (int i = 0; i < size; ++i) {
				addToolbarItem(toolbar.getItem(i));
			}
		}

		initMenu();
	}
	
	abstract protected void initMenu();

	public void onButtonPress(ZLApplication.Toolbar.ButtonItem button) {
		/*
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
				ZLApplication.Toolbar.ButtonGroup group = button.getButtonGroup();
				final ArrayList<ZLApplication.Toolbar.ButtonItem> items = group.Items;
				final int size = items.size();
				for (int i = 0; i < size; ++i) {
					setToggleButtonState(items.get(i));
				}
			}
			myToggleButtonLock = false;
		}
		*/
		
		
		getApplication().doAction(button.getActionId());
	}
	
	//public abstract void setToggleButtonState(ZLApplication.Toolbar.ButtonItem item);
	
	public abstract void setToolbarItemState(ZLApplication.Toolbar.Item item, boolean visible, boolean enabled);
	
	abstract protected ZLViewWidget createViewWidget();
	
	abstract public void addToolbarItem(ZLApplication.Toolbar.Item item);

	protected void refresh() {
		final ZLApplication.Toolbar toolbar = myApplication.getToolbar();
		if (toolbar != null) {
			boolean enableToolbarSpace = false;
			ZLApplication.Toolbar.Item lastSeparator = null;
			final int size = toolbar.size();
			for (int i = 0; i < size; ++i) {
				final ZLApplication.Toolbar.Item item = toolbar.getItem(i);
				/*
				if (item instanceof ZLApplication.Toolbar.OptionEntryItem) {
				case OPTION_ENTRY:
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
						break;
				} else */
				if (item instanceof ZLApplication.Toolbar.ButtonItem) {
					ZLApplication.Toolbar.ButtonItem button = (ZLApplication.Toolbar.ButtonItem)item;
					final String id = button.getActionId();
	
					final boolean visible = getApplication().isActionVisible(id);
					final boolean enabled = getApplication().isActionEnabled(id);
    
					if (visible) {
						if (lastSeparator != null) {
							setToolbarItemState(lastSeparator, true, true);
							lastSeparator = null;
						}
						enableToolbarSpace = true;
					}
					/*
					if (!enabled && button.isPressed()) {
						ZLApplication.Toolbar.ButtonGroup group = button.getButtonGroup();
						group.press(null);
						getApplication().doAction(group.UnselectAllButtonsActionId);
						myToggleButtonLock = true;
						setToggleButtonState(button);
						myToggleButtonLock = false;
					}
					*/
					setToolbarItemState(item, visible, enabled);
				} else if (item instanceof ZLApplication.Toolbar.SeparatorItem) {
					if (enableToolbarSpace) {
						lastSeparator = item;
						enableToolbarSpace = false;
					} else {
						setToolbarItemState(item, false, true);
					}
					//break;
				}
			}
			
			if (lastSeparator != null) {
				setToolbarItemState(lastSeparator, false, true);
			}
		}
	}
	// TODO: change to pure virtual
	//virtual void present() {}

//*/
	final protected void close() {
		getApplication().onQuit();
		closeInternal();
	}

	abstract protected void closeInternal();
	abstract protected void setCaption(String caption);
	abstract protected void setFullscreen(boolean fullscreen);
	abstract protected boolean isFullscreen();

/*
	virtual void grabAllKeys(bool grab) = 0;
	virtual void setHyperlinkCursor(bool) {}
*/
}
