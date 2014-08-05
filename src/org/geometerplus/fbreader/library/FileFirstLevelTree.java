/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.Paths;

public class FileFirstLevelTree extends FirstLevelTree {
	FileFirstLevelTree(RootTree root) {
		super(root, ROOT_FILE);
	}

	@Override
	public String getTreeTitle() {
		return getName();
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		for (String dir : Paths.BookPathOption.getValue()) {
			addChild(dir, "fileTreeLibrary", dir);
		}
		addChild("/", "fileTreeRoot", null);
		addChild(Paths.cardDirectory(), "fileTreeCard", null);
	}

	private void addChild(String path, String resourceKey, String summary) {
		final ZLFile file = ZLFile.createFileByPath(path);
		if (file != null) {
			final ZLResource resource = resource().getResource(resourceKey);
			new FileTree(
				this,
				file,
				resource.getValue(),
				summary != null ? summary : resource.getResource("summary").getValue()
			);
		}
	}
}
