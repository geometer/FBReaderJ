/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network;

import java.util.LinkedList;
import java.util.Set;

import org.geometerplus.zlibrary.core.constants.MimeTypes;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.tree.FBTree;

public abstract class NetworkTree extends FBTree {
	protected NetworkTree(int level) {
		super(level);
	}

	protected NetworkTree() {
		super();
	}

	protected NetworkTree(NetworkTree parent) {
		super(parent);
	}

	protected NetworkTree(NetworkTree parent, int position) {
		super(parent, position);
	}

	public static ZLImage createCover(NetworkLibraryItem item) {
		if (item.Cover == null) {
			return null;
		}
		return createCover(item.Cover, null);
	}

	private static final String DATA_PREFIX = "data:";

	public static ZLImage createCover(String url, String mimeType) {
		if (url == null) {
			return null;
		}
		if (mimeType == null) {
			mimeType = MimeTypes.MIME_IMAGE_AUTO;
		}
		if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://")) {
			return new NetworkImage(url, mimeType);
		} else if (url.startsWith(DATA_PREFIX)) {
			int commaIndex = url.indexOf(',');
			if (commaIndex == -1) {
				return null;
			}
			if (mimeType == MimeTypes.MIME_IMAGE_AUTO) {
				int index = url.indexOf(';');
				if (index == -1 || index > commaIndex) {
					index = commaIndex;
				}
	 			// string starts with "data:image/"
				if (url.startsWith(MimeTypes.MIME_IMAGE_PREFIX, DATA_PREFIX.length())) {
					mimeType = url.substring(DATA_PREFIX.length(), index);
				}
			}
			int key = url.indexOf("base64");
			if (key != -1 && key < commaIndex) {
				Base64EncodedImage img = new Base64EncodedImage(mimeType);
				img.setData(url.substring(commaIndex + 1));
				return img;
			}
		}
		return null;
	}


	public abstract NetworkLibraryItem getHoldedItem();

	public void removeItems(Set<NetworkLibraryItem> items) {
		if (items.isEmpty() || subTrees().isEmpty()) {
			return;
		}
		final LinkedList<FBTree> treesList = new LinkedList<FBTree>();
		for (FBTree tree: subTrees()) {
			final NetworkLibraryItem treeItem = ((NetworkTree)tree).getHoldedItem();
			if (treeItem != null && items.contains(treeItem)) {
				treesList.add(tree);
				items.remove(treeItem);
			}
		}
		for (FBTree tree: treesList) {
			tree.removeSelf();
		}
		if (items.isEmpty()) {
			return;
		}
		treesList.clear();
		treesList.addAll(subTrees());
		while (!treesList.isEmpty()) {
			final NetworkTree tree = (NetworkTree) treesList.remove(treesList.size() - 1);
			tree.removeItems(items);
		}
	}
}
