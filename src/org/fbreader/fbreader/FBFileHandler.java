package org.fbreader.fbreader;

import java.util.*;

import org.fbreader.description.BookDescription;
import org.fbreader.formats.FormatPlugin;
import org.fbreader.formats.FormatPlugin.PluginCollection;
import org.fbreader.formats.fb2.FB2Plugin;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.util.*;

import org.zlibrary.core.dialogs.UpdateType;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.dialogs.ZLTreeNode;
import org.zlibrary.core.dialogs.ZLTreeOpenHandler;
import org.zlibrary.core.filesystem.ZLDir;
import org.zlibrary.core.filesystem.ZLFile;

public class FBFileHandler extends ZLTreeOpenHandler {

	public ZLStringOption DirectoryOption; //?public

	private ZLDir myDir;
	private boolean myIsUpToDate;
	private final ArrayList mySubnodes = new ArrayList();
	private BookDescription myDescription;
	private	int mySelectedIndex;

	private final static String FOLDER_ICON = "folder";
	private final static String ZIP_FOLDER_ICON = "zipfolder";
	private final static String UPFOLDER_ICON = "upfolder";
	private final static HashMap pluginIcons = new HashMap(); // <FormatPlugin, String>
	
	public FBFileHandler() {
		DirectoryOption = new ZLStringOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "OpenFileDialog", "Directory", System.getProperty("user.home"));
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
	
	public BookDescription description() {
		return myDescription;
	}
	
// override private	
	
	protected boolean accept(ZLTreeNode node) {
		final String name = myDir.getItemPath(node.id());
		FormatPlugin plugin = PluginCollection.instance().getPlugin(new ZLFile(name), false);
		final String message = (plugin == null) ? "Unknown File Format" : plugin.tryOpen(name);
		if (! "".equals(message)) {
			final String boxKey = "openBookErrorBox";
			ZLDialogManager.getInstance().showErrorBox(boxKey,
				ZLDialogManager.getDialogMessage(boxKey) + " " + message);
			return false;
		}
		myDescription = BookDescription.getDescription(name);
		System.out.println(myDescription.getFileName());
		return true;
	}

	public void changeFolder(ZLTreeNode node) {
		// TODO Auto-generated method stub
		// id != null
		ZLDir dir = new ZLFile(myDir.getItemPath(node.id())).getDirectory();
		if (dir != null) {
			final String selectedId = myDir.getName();
			myDir = dir;
			DirectoryOption.setValue(myDir.getPath()); //?
			myIsUpToDate = false;
			mySubnodes.clear();
			mySelectedIndex = 0;
			if ("..".equals(node.id())) {
				final ArrayList subnodes = this.subnodes();
				final int size = subnodes.size();
				for (int index = 0; index < size; index++) {
					if (((ZLTreeNode) subnodes.get(index)).id().equals(selectedId)) {
						mySelectedIndex = index;
						break;
					}
				} 
			}
			addUpdateInfo(UpdateType.UPDATE_ALL);
		}
	}

	public int selectedIndex() {
		return mySelectedIndex;
	}

	public String stateDisplayName() {
		return ZLFile.fileNameToUtf8(myDir.getPath());
	}

	public ArrayList subnodes() {
		// TODO Auto-generated method stub
		
		if (!myIsUpToDate) {
			if (!myDir.isRoot()) {
				mySubnodes.add(new ZLTreeNode("..", "..", UPFOLDER_ICON, true));
			}

			HashMap folderNodes = new HashMap(); // <String, ZLTreeNode>
			HashMap fileNodes = new HashMap();

			ArrayList/*<String>*/ names = myDir.collectSubDirs();
			int size = names.size();
			for (int i = 0; i < size; i++) {
				final String subDir = (String) names.get(i);
				final String displayName = ZLFile.fileNameToUtf8(new ZLFile(subDir).getName(false));
				folderNodes.put(displayName, new ZLTreeNode(subDir, displayName, FOLDER_ICON, true));
			}
			names.clear();

			names = myDir.collectFiles();
			size = names.size();
			for (int i = 0; i < size; i++) {
				final String fileName = (String) names.get(i);
				if ("".equals(fileName)) {
					continue;
				}
				ZLFile file = new ZLFile(myDir.getItemPath(fileName));
				final String displayName = ZLFile.fileNameToUtf8(file.getName(false));
				if ("".equals(displayName)) {
					continue;
				}
				FormatPlugin plugin = PluginCollection.instance().getPlugin(file, false);
				if (plugin != null) {
					String icon = (String) pluginIcons.get(plugin);
					if (icon == null) {
						icon = plugin.getIconName();
						pluginIcons.put(plugin, icon);
					}
					fileNodes.put(displayName, new ZLTreeNode(fileName, displayName, icon, false));
				} else if (file.isArchive()) {
					folderNodes.put(displayName, new ZLTreeNode(fileName, displayName, ZIP_FOLDER_ICON, true));
				}
			}

	//		Collections.sort((List) folderNodes.values(), new ZLTreeNodeComparator());
			mySubnodes.addAll(folderNodes.values());
			mySubnodes.addAll(fileNodes.values());
			myIsUpToDate = true;
			Collections.sort(mySubnodes, new ZLTreeNodeComparator());
		}
		
		return mySubnodes;
	}
	
	private static class ZLTreeNodeComparator implements Comparator {
		public int compare(Object o1, Object o2) {	
			if (((ZLTreeNode) o1).isFolder() == ((ZLTreeNode) o2).isFolder()) {
				return ((ZLTreeNode) o1).displayName().toLowerCase().compareTo(((ZLTreeNode) o2).displayName().toLowerCase());
			} else if (((ZLTreeNode) o1).isFolder()) {
				return -1;
			} else {
				return 1;
			}
		}		
	}

}
