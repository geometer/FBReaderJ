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

package org.geometerplus.android.fbreader.tree;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.tree.FBTree;

public abstract class BaseActivity extends ListActivity {
	private FBTree myCurrentTree;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
	}

	@Override
	public ListAdapter getListAdapter() {
		return (ListAdapter)super.getListAdapter();
	}

	protected FBTree getCurrentTree() {
		return myCurrentTree;
	}

	protected void setCurrentTree(FBTree tree) {
		myCurrentTree = tree;
	}

	public abstract boolean isTreeSelected(FBTree tree);

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && myCurrentTree.Parent != null) {
			final FBTree oldTree = myCurrentTree;
			openTree(myCurrentTree.Parent);
			setSelection(getListAdapter().getIndex(oldTree));
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	protected void openTree(final FBTree tree) {
		switch (tree.getOpeningStatus()) {
			case WAIT_FOR_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				final String messageKey = tree.getOpeningStatusMessage();
				if (messageKey != null) {
					UIUtil.runWithMessage(
						BaseActivity.this, messageKey,
						new Runnable() {
							public void run() {
								tree.waitForOpening();
							}
						},
						new Runnable() {
							public void run() {
								openTreeInternal(tree);
							}
						}
					);
				} else {
					tree.waitForOpening();
					openTreeInternal(tree);
				}
				break;
			default:
				openTreeInternal(tree);
				break;
		}
	}

	private void openTreeInternal(FBTree tree) {
		switch (tree.getOpeningStatus()) {
			case READY_TO_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				myCurrentTree = tree;
				getListAdapter().replaceAll(myCurrentTree.subTrees());
				setTitle(myCurrentTree.getTreeTitle());
				setSelection(getListAdapter().getFirstSelectedItemIndex());
				break;
			case CANNOT_OPEN:
				UIUtil.showErrorMessage(BaseActivity.this, tree.getOpeningStatusMessage());
				break;
		}
	}
}
