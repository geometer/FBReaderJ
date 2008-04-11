package org.geometerplus.zlibrary.core.dialogs;

public interface UpdateType {
	int UPDATE_NONE = 0;
	int UPDATE_STATE = 1;
	int UPDATE_LIST = 2;
	int UPDATE_SELECTION = 4;
	int UPDATE_ALL = UPDATE_STATE | UPDATE_LIST | UPDATE_SELECTION;
}
