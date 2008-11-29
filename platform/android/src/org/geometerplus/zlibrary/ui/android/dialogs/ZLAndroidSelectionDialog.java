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

package org.geometerplus.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.app.Activity;
import android.os.*;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLSelectionDialog;
import org.geometerplus.zlibrary.core.dialogs.ZLTreeHandler;
import org.geometerplus.zlibrary.core.dialogs.ZLTreeNode;

class ZLAndroidSelectionDialog extends ZLSelectionDialog implements ZLAndroidDialogInterface {
	private final String myCaption;
	private boolean myReturnValue = false;
	private final Runnable myActionOnAccept;
	private final Context myContext;
	private SelectionView mySelectionView;
	
	protected ZLAndroidSelectionDialog(Context context, String caption, ZLTreeHandler handler, Runnable actionOnAccept) {
		super(handler);
		myCaption = caption;
		myActionOnAccept = actionOnAccept;
		myContext = context;
	}

	public void setActivity(DialogActivity activity) {
		mySelectionView = new SelectionView(activity);
		activity.setContentView(mySelectionView);
		invalidateAll();
		update();
	}

	public void endActivity() {
	}

	protected void runNode(ZLTreeNode node) {
		if (node != null) {
			myReturnValue = !node.IsFolder;
			super.runNode(node);
		}
	}

	protected void exitDialog() {
		((Activity)mySelectionView.getContext()).finish();
		if (myReturnValue) {
			new Handler().post(myActionOnAccept);
		}
	}

	protected void selectItem(int index) {
		mySelectionView.setSelection(index);
	}

	protected void updateList() {
		mySelectionView.setAdapter(new SelectionViewAdapter());
		mySelectionView.invalidate();
	}

	protected void updateStateLine() {
		((Activity)mySelectionView.getContext()).setTitle(myCaption + handler().stateDisplayName());
	}
	
	private class SelectionView extends ListView implements AdapterView.OnItemClickListener {
		public SelectionView(Context context) {
			super(context);
			setFocusable(true);
			setOnItemClickListener(this);
		}

		public boolean onKeyUp(int keyCode, KeyEvent event) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					final ItemView view = (ItemView)getSelectedView();
					if (view != null) {
        		runNode(view.getNode());
					}
					return false;
				default:	
					return super.onKeyUp(keyCode, event);
			}
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			runNode((ZLTreeNode)getAdapter().getItem(position));
		}
	}

	private class SelectionViewAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			return new ItemView(parent.getContext(), (ZLTreeNode)getItem(position));
		}

		public boolean areAllItemsSelectable() {
			return false;
		}

		public boolean isSelectable(int position) {
			return true;
		}

		public int getCount() {
			return handler().subnodes().size();
		}

		public Object getItem(int position) {
			return handler().subnodes().get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
