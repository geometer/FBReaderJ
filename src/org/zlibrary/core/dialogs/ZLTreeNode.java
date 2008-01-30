package org.zlibrary.core.dialogs;

public class ZLTreeNode {
	private String myId;
	private String myDisplayName;
	private String myPixmapName;
	private boolean myIsFolder;
	
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
	
}
