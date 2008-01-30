package org.zlibrary.core.dialogs;

public abstract class ZLTreeSaveHandler extends ZLTreeHandler {

	@Override
	public boolean isOpenHandler() {
		return false;
	}

	public abstract void processNode(ZLTreeNode node);
	
	public abstract boolean accept(String state);
}
