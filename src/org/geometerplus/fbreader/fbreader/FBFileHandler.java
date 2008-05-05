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

package org.geometerplus.fbreader.fbreader;

import java.util.*;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.filesystem.*;

import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.formats.*;

public class FBFileHandler extends ZLTreeOpenHandler {
	private final ZLStringOption DirectoryOption =
		new ZLStringOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "OpenFileDialog", "Directory", System.getProperty("user.home"));

	private ZLDir myDir;
	private boolean myIsUpToDate;
	private final ArrayList mySubnodes = new ArrayList();
	private BookDescription myDescription;
	private	int mySelectedIndex;

	private final static String FOLDER_ICON = "folder";
	private final static String ZIP_FOLDER_ICON = "zipfolder";
	private final static String UPFOLDER_ICON = "upfolder";
	private final static HashMap pluginIcons = new HashMap();
	
	public FBFileHandler() {
		myIsUpToDate = false;
		mySelectedIndex = 0; 
		myDir = (new ZLFile(DirectoryOption.getValue())).getDirectory();
		if (myDir == null) {
			myDir = (new ZLFile(System.getProperty("user.home"))).getDirectory();
		}
		if (myDir == null) {
			myDir = ZLDir.getRoot();
		}
	}
	
	public BookDescription getDescription() {
		return myDescription;
	}
	
	protected boolean accept(ZLTreeNode node) {
		final String name = myDir.getItemPath(node.Id);
		FormatPlugin plugin = PluginCollection.instance().getPlugin(new ZLFile(name), false);
		final String message = (plugin == null) ? "Unknown File Format" : plugin.tryOpen(name);
		if (message.length() != 0) {
			final String boxKey = "openBookErrorBox";
			ZLDialogManager.getInstance().showErrorBox(boxKey,
				ZLDialogManager.getDialogMessage(boxKey) + " " + message, null);
			return false;
		}
		myDescription = BookDescription.getDescription(name);
		return true;
	}

	public void changeFolder(ZLTreeNode node) {
		// TODO Auto-generated method stub
		// id != null
		ZLDir dir = new ZLFile(myDir.getItemPath(node.Id)).getDirectory();
		if (dir != null) {
			final String selectedId = myDir.getName();
			myDir = dir;
			DirectoryOption.setValue(myDir.getPath()); //?
			myIsUpToDate = false;
			mySubnodes.clear();
			mySelectedIndex = 0;
			if ("..".equals(node.Id)) {
				final ArrayList subnodes = this.subnodes();
				final int size = subnodes.size();
				for (int index = 0; index < size; index++) {
					if (((ZLTreeNode)subnodes.get(index)).Id.equals(selectedId)) {
						mySelectedIndex = index;
						break;
					}
				} 
			}
			addUpdateInfo(ZLSelectionDialog.UPDATE_ALL);
		}
	}

	public int selectedIndex() {
		return mySelectedIndex;
	}

	public String stateDisplayName() {
		return myDir.getPath();
	}

	public ArrayList subnodes() {
		if (!myIsUpToDate) {
			ArrayList/*<String>*/ names = myDir.collectSubDirs();
			if (names != null) {
				int size = names.size();
				for (int i = 0; i < size; i++) {
					final String subDir = (String) names.get(i);
					final String displayName = new ZLFile(subDir).getName(false);
					final ZLTreeNode node = new ZLTreeNode(subDir, displayName, FOLDER_ICON, true);
					mySubnodes.add(node);
				}
				names.clear();
		    }    
			names = myDir.collectFiles();
			if (names != null) {
				int size = names.size();
				for (int i = 0; i < size; i++) {
					final String fileName = (String) names.get(i);
					if ("".equals(fileName)) {
						continue;
					}
					ZLFile file = new ZLFile(myDir.getItemPath(fileName));
					final String displayName = file.getName(false);
					if ("".equals(displayName)) {
						continue;
					}
					FormatPlugin plugin = PluginCollection.instance().getPlugin(file, false);
					if (plugin != null && !file.isDirectory()) {
						String icon = (String) pluginIcons.get(plugin);
						if (icon == null) {
							icon = plugin.getIconName();
							pluginIcons.put(plugin, icon);
						}
						final ZLTreeNode node = new ZLTreeNode(fileName, displayName, icon, false);
						//fileNodeMap.put(displayName, node);
						mySubnodes.add(node);
					} else if (file.isArchive()) {
						final ZLTreeNode node = new ZLTreeNode(fileName, displayName, ZIP_FOLDER_ICON, true);
						//folderNodeMap.put(displayName, node);
						mySubnodes.add(node);
					}
				}
			}

			myIsUpToDate = true;
			Collections.sort(mySubnodes, new ZLTreeNodeComparator());
			if (!myDir.isRoot()) {
				mySubnodes.add(0, new ZLTreeNode("..", "..", UPFOLDER_ICON, true));
			}
		}
		
		return mySubnodes;
	}
	
	private static class ZLTreeNodeComparator implements Comparator {
		public int compare(Object o1, Object o2) {	
			final ZLTreeNode node1 = (ZLTreeNode)o1;
			final ZLTreeNode node2 = (ZLTreeNode)o2;
			if (node1.IsFolder == node2.IsFolder) {
				return node1.DisplayName.compareToIgnoreCase(node2.DisplayName);
			} else {
				return node1.IsFolder ? -1 : 1;
			}
		}		
	}

}
