package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class FMBaseAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
	protected List<FileItem> myItems = new ArrayList<FileItem>();

	public synchronized void clear() {
		myItems.clear();
	}

	public synchronized void add(FileItem item){
		myItems.add(item);
	}
	
	public synchronized void remove(FileItem fileItem) {
		myItems.remove(fileItem);
	}

	public synchronized void deleteFile(ZLFile file) {
		for (FileItem item : myItems) {
			if (file.equals(item.getFile())) {
				myItems.remove(item);
				break;
			}
		}
	}

	public synchronized int getCount() {
		return myItems.size();
	}

	public synchronized FileItem getItem(int position) {
		return myItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	@Override
	public abstract void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo);
}