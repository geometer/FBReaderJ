package org.geometerplus.zlibrary.core.dialogs;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

public abstract class ZLTreeHandler {
	private int myUpdateInfo;
	
	protected ZLTreeHandler() {
		myUpdateInfo = UpdateType.UPDATE_ALL;
	}
	
	protected void addUpdateInfo(int info) {
		myUpdateInfo = myUpdateInfo | info;
	}

	public int updateInfo() {
		return myUpdateInfo;
	}
	
	public void resetUpdateInfo() {
		myUpdateInfo = UpdateType.UPDATE_NONE;
	}
	
	public abstract boolean isOpenHandler();

	public abstract String stateDisplayName();

	public abstract ArrayList subnodes();

	public abstract	int selectedIndex();
		
	public abstract void changeFolder(ZLTreeNode node);
}
