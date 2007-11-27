package org.zlibrary.ui.android.library;

import java.util.Stack;

import android.app.Activity;
import android.os.Bundle;
//import android.view.Menu;

import org.zlibrary.core.application.ZLApplication;

public class ZLAndroidActivity extends Activity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		new ZLAndroidLibrary().run(this);
	}

	private class MyMenuVisitor extends ZLApplication.MenuVisitor {
		private int myItemCount = android.view.Menu.FIRST;
		private final Stack<android.view.Menu> myMenuStack = new Stack<android.view.Menu>();

		private MyMenuVisitor(android.view.Menu menu) {
			myMenuStack.push(menu);
		}
		protected void processSubmenuBeforeItems(ZLApplication.Menubar.Submenu submenu) {
			myMenuStack.push(myMenuStack.peek().addSubMenu(0, myItemCount++, submenu.getMenuName()));	
		}
		protected void processSubmenuAfterItems(ZLApplication.Menubar.Submenu submenu) {
			myMenuStack.pop();
		}
		protected void processItem(ZLApplication.Menubar.PlainItem item) {
			myMenuStack.peek().add(0, myItemCount++, item.getName());
		}
		protected void processSepartor(ZLApplication.Menubar.Separator separator) {
			myMenuStack.peek().addSeparator(0, myItemCount++);
		}
	}

	public boolean onCreateOptionsMenu(final android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		new MyMenuVisitor(menu).processMenu(
			((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).application().getMenubar()
		);
		return true;
	}
}
