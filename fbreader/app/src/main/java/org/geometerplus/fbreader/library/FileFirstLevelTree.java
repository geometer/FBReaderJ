/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.library;

import java.util.List;

import org.fbreader.util.Pair;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;

public class FileFirstLevelTree extends FirstLevelTree {
	FileFirstLevelTree(RootTree root) {
		super(root, ROOT_FILE);
	}

	@Override
	public Pair<String,String> getTreeTitle() {
		return new Pair(getName(), null);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		for (String dir : Paths.BookPathOption.getValue()) {
			addChild(dir, resource().getResource("fileTreeLibrary").getValue(), dir);
		}
		addChild("/", "fileTreeRoot");
		final List<String> cards = Paths.allCardDirectories();
		if (cards.size() == 1) {
			addChild(cards.get(0), "fileTreeCard");
		} else {
			final ZLResource res = resource().getResource("fileTreeCard");
			final String title = res.getResource("withIndex").getValue();
			final String summary = res.getResource("summary").getValue();
			int index = 0;
			for (String dir : cards) {
				addChild(dir, title.replaceAll("%s", String.valueOf(++index)), summary);
			}
		}
	}

	private void addChild(String path, String title, String summary) {
		final ZLFile file = ZLFile.createFileByPath(path);
		if (file != null) {
			new FileTree(this, file, title, summary);
		}
	}

	private void addChild(String path, String resourceKey) {
		final ZLResource resource = resource().getResource(resourceKey);
		addChild(path, resource.getValue(), resource.getResource("summary").getValue());
	}
}
