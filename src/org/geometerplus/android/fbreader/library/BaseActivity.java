/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.tree.FBTree;

abstract class BaseActivity extends ListActivity implements View.OnCreateContextMenuListener {
	static final String TREE_KEY_KEY = "TreeKey";
	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";

	protected static final int CHILD_LIST_REQUEST = 0;
	protected static final int BOOK_INFO_REQUEST = 1;

	protected static final int RESULT_DONT_INVALIDATE_VIEWS = 0;
	protected static final int RESULT_DO_INVALIDATE_VIEWS = 1;

	protected FBTree myCurrentTree;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public ListAdapter getListAdapter() {
		return (ListAdapter)super.getListAdapter();
	}

	protected abstract int getCoverResourceId(FBTree tree);
	abstract boolean isTreeSelected(FBTree tree);

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && myCurrentTree.Parent != null) {
			final FBTree oldTree = myCurrentTree;
			new OpenTreeRunnable(myCurrentTree.Parent).run();
			setSelection(getListAdapter().getIndex(oldTree));
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	protected class OpenTreeRunnable implements Runnable {
		private final FBTree myTree;

		public OpenTreeRunnable(FBTree tree) {
			myTree = tree;
		}

		public void run() {
			switch (myTree.getOpeningStatus()) {
				case WAIT_FOR_OPEN:
				case ALWAYS_RELOAD_BEFORE_OPENING:
					final String messageKey = myTree.getOpeningStatusMessage();
					if (messageKey != null) {
						UIUtil.runWithMessage(
							BaseActivity.this, messageKey,
							new Runnable() {
								public void run() {
									myTree.waitForOpening();
								}
							},
							new Runnable() {
								public void run() {
									openTree();
								}
							}
						);
					} else {
						myTree.waitForOpening();
						openTree();
					}
					break;
				default:
					openTree();
					break;
			}
		}

		protected void openTree() {
			switch (myTree.getOpeningStatus()) {
				case READY_TO_OPEN:
				case ALWAYS_RELOAD_BEFORE_OPENING:
					myCurrentTree = myTree;
					getListAdapter().replaceAll(myCurrentTree.subTrees());
					setTitle(myCurrentTree.getTreeTitle());
					setSelection(getListAdapter().getFirstSelectedItemIndex());
					break;
				case CANNOT_OPEN:
					UIUtil.showErrorMessage(BaseActivity.this, myTree.getOpeningStatusMessage());
					break;
			}
		}
	}
}
