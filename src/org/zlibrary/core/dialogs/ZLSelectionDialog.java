package org.zlibrary.core.dialogs;

import java.util.*;
import org.zlibrary.core.util.*;

public abstract class ZLSelectionDialog {
	private ZLTreeHandler myHandler;

	protected ZLSelectionDialog(ZLTreeHandler myHandler) {
		this.myHandler = myHandler;
	}
	
	private void updateSelection() {
		ArrayList nodes = handler().subnodes();
		if (nodes.size() == 0) {
			return;
		}

		int index = handler().selectedIndex();
		if ((index < 0) || (index >= (int) nodes.size())) {
			if (handler().isOpenHandler()) {
				index = 0;
			} else {
				return;
			}
		}
		selectItem(index);
	}
	
	protected ZLTreeHandler handler() {
		return myHandler;
	}
	
	protected void runNode(ZLTreeNode node) {
		if (node == null) {
			return;
		}

		if (node.isFolder()) {
			myHandler.changeFolder(node);
			update();
		} else if (myHandler.isOpenHandler()) {
			if (((ZLTreeOpenHandler) myHandler).accept(node)) {
				exitDialog();
			} else {
				update();
			}
		} else {
			((ZLTreeSaveHandler) myHandler).processNode(node);
			update();
		}
	}
	
	protected void runState(String state) {
		if (!myHandler.isOpenHandler()) {
			if (((ZLTreeSaveHandler) myHandler).accept(state)) {
				exitDialog();
			}
		}
	}
	
	protected void update() {
		int info = handler().updateInfo();
		if ((info & UpdateType.UPDATE_STATE) != 0) {
			updateStateLine();
		}
		if ((info & UpdateType.UPDATE_LIST) != 0) {
			updateList();
		}
		if ((info & UpdateType.UPDATE_SELECTION) != 0) {
			updateSelection();
		}
		myHandler.resetUpdateInfo();
	}
	
	protected abstract void exitDialog();

	protected abstract void updateStateLine();
	
	protected abstract void updateList();
	
	protected abstract void selectItem(int index);

	public abstract boolean run();	
}
