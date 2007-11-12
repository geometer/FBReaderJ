package org.zlibrary.core.application.menu;

public interface MenuVisitor {
	//void processSubmenuBeforeItems(Menubar.Submenu submenu);
	//void processSubmenuAfterItems(Menubar.Submenu submenu);
	void processItem(Menubar.PlainItem item);
	void processSepartor(Menubar.Separator separator);
}
