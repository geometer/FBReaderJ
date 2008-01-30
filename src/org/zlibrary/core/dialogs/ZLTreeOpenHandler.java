package org.zlibrary.core.dialogs;

public abstract class ZLTreeOpenHandler extends ZLTreeHandler {

	@Override
	public boolean isOpenHandler() {
		return true;
	}

	public abstract boolean accept(ZLTreeNode node);
}
