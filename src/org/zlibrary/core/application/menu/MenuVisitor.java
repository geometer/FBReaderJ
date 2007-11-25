package org.zlibrary.core.application.menu;

public abstract class MenuVisitor {
	public void processMenu(Menu menu) {
		for (Menu.Item item : menu.items()) {
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
