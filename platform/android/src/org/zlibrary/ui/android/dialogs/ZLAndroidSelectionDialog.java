package org.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.app.Dialog;
import android.os.*;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLSelectionDialog;
import org.zlibrary.core.dialogs.ZLTreeHandler;
import org.zlibrary.core.dialogs.ZLTreeNode;

class ZLAndroidSelectionDialog extends ZLSelectionDialog {
	private boolean myReturnValue = false;
	private final Runnable myActionOnAccept;
	private final TextView myHeader;
	private final SelectionView mySelectionView;
	private final AndroidDialog myDialog;
	
	protected ZLAndroidSelectionDialog(Context context, String caption, ZLTreeHandler handler, Runnable actionOnAccept) {
		super(handler);
		myActionOnAccept = actionOnAccept;
		myHeader = new TextView(context);
		mySelectionView = new SelectionView(context);
		myDialog = new AndroidDialog(context, mySelectionView, caption);
		update();
	}

	protected void exitDialog() {
		myDialog.dismiss();
		if (myReturnValue) {
			new Handler().post(myActionOnAccept);
		}
	}

	public void run() {
		myDialog.show();
	}

	protected void selectItem(int index) {
		mySelectionView.setSelection(index + 1);
	}

	protected void updateList() {
		mySelectionView.setAdapter(new SelectionViewAdapter());
		mySelectionView.invalidate();
	}

	protected void updateStateLine() {
		myHeader.setText(handler().stateDisplayName());
	}
	
	private class SelectionViewAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new ItemView(myDialog.getContext(), (ZLTreeNode)getItem(position));
			}

			return convertView;
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

	private class SelectionView extends ListView {
		public SelectionView(Context context) {
			super(context);
			setFocusable(true);
			addHeaderView(myHeader, null, false);
		}

		public boolean onKeyUp(int keyCode, KeyEvent event) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_NEWLINE:
					final ItemView view = (ItemView)getSelectedView();
					if (view != null) {
						final ZLTreeNode node = view.getNode();
						myReturnValue = !node.isFolder();
        		runNode(view.getNode());
					}
					return false;
				default:	
					return super.onKeyUp(keyCode, event);
			}
		}

		public boolean onTouchEvent(MotionEvent event) {
			final int x = (int)event.getX();
			final int y = (int)event.getY();
			final ZLTreeNode node = (ZLTreeNode)getAdapter().getItem(pointToPosition(x, y));
			if (node != null) {
				myReturnValue = !node.isFolder();
				runNode(node);
			}
			return false;
		}
	}
}
