/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.android.fbreader.libraryService.Util;

public class FileFirstLevelTree extends FirstLevelTree {
	FileFirstLevelTree(RootTree root) {
		super(root, ROOT_FILE_TREE);
		List<String> directories = Util.splitDirectories(Paths.BooksDirectoryOption().getValue());
		if (directories.size() == 1)
			addChild(directories.get(0), NODE_LIBRARY_DIRECTORY);
		else {
			FirstLevelTree libraryGroup = new FirstLevelTree(this, NODE_LIBRARY_DIRECTORY);
			for (String d : directories) {
				final ZLFile file = ZLFile.createFileByPath(d);
				if (file != null) {
					new FileTree(
						libraryGroup,
						file,
						file.getShortName(),
						file.getPath());
				}
			}
		}
		addChild("/", "fileTreeRoot");
		addChild(Paths.cardDirectory(), "fileTreeCard");
	}

	private void addChild(String path, String resourceKey) {
		final ZLFile file = ZLFile.createFileByPath(path);
		if (file != null) {
			final ZLResource resource = resource().getResource(resourceKey);
			new FileTree(
				this,
				file,
				resource.getValue(),
				resource.getResource("summary").getValue()
			);
		}
	}

	@Override
	public String getTreeTitle() {
		return getName();
	}

	@Override
	public Status getOpeningStatus() {
		return Status.READY_TO_OPEN;
	}
}
