package org.geometerplus.fbreader.network.tree;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;

public class OpenFileChooseTree extends NetworkTree {
	public OpenFileChooseTree(NetworkTree parent) {
		super(parent);
	}

	@Override
	public String getName() {
		return NetworkLibrary.resource().getResource("yourDevice").getValue();
	}

	@Override
	public String getSummary() {
		return NetworkLibrary.resource().getResource("yourDeviceSummary").getValue();
	}

	@Override
	protected String getStringId() {
		return "@Open File Choose Tree";
	}
}
