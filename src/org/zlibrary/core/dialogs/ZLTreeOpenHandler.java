package org.zlibrary.core.dialogs;

public abstract class ZLTreeOpenHandler extends ZLTreeHandler {
	public boolean isOpenHandler() {
		return true;
	}

	protected abstract boolean accept(ZLTreeNode node);
}
