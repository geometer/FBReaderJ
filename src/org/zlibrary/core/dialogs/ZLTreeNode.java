package org.zlibrary.core.dialogs;

public class ZLTreeNode {
	private final String myId;
	private final String myDisplayName;
	private final String myPixmapName;
	private final boolean myIsFolder;
	
	public ZLTreeNode(String myId, String myDisplayName, String myPixmapName, boolean myIsFolder) {
		this.myId = myId;
		this.myDisplayName = myDisplayName;
		this.myPixmapName = myPixmapName;
		this.myIsFolder = myIsFolder;
	}

	public String displayName() {
		return myDisplayName;
	}

	public String id() {
		return myId;
	}

	public boolean isFolder() {
		return myIsFolder;
	}

	public String pixmapName() {
		return myPixmapName;
	}

	@Override
	public String toString() {
		return myDisplayName;
	}
	
}
