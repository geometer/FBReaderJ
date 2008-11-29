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

package org.geometerplus.zlibrary.core.dialogs;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

public abstract class ZLSelectionDialog {
	public final static int UPDATE_NONE = 0;
	public final static int UPDATE_STATE = 1;
	public final static int UPDATE_LIST = 2;
	public final static int UPDATE_SELECTION = 4;
	public final static int UPDATE_ALL = UPDATE_STATE | UPDATE_LIST | UPDATE_SELECTION;

	private final ZLTreeHandler myHandler;

	protected ZLSelectionDialog(ZLTreeHandler myHandler) {
		this.myHandler = myHandler;
	}
	
	private void updateSelection() {
		ArrayList nodes = handler().subnodes();
		if (nodes.size() == 0) {
			return;
		}

		int index = handler().selectedIndex();
		if ((index < 0) || (index >= nodes.size())) {
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
		if (node.IsFolder) {
			myHandler.changeFolder(node);
			update();
		} else if (myHandler.isOpenHandler()) {
			if (((ZLTreeOpenHandler)myHandler).accept(node)) {
				exitDialog();
			} else {
				update();
			}
		} else {
			((ZLTreeSaveHandler)myHandler).processNode(node);
			update();
		}
	}
	
	protected void runState(String state) {
		if (!myHandler.isOpenHandler()) {
			if (((ZLTreeSaveHandler)myHandler).accept(state)) {
				exitDialog();
			}
		}
	}
	
	protected void invalidateAll() {
		handler().invalidateUpdateInfo();
	}

	protected void update() {
		int info = handler().updateInfo();
		if ((info & UPDATE_STATE) != 0) {
			updateStateLine();
		}
		if ((info & UPDATE_LIST) != 0) {
			updateList();
		}
		if ((info & UPDATE_SELECTION) != 0) {
			updateSelection();
		}
		handler().resetUpdateInfo();
	}
	
	protected abstract void exitDialog();

	protected abstract void updateStateLine();
	
	protected abstract void updateList();
	
	protected abstract void selectItem(int index);
}
