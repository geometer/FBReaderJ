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

import android.app.SearchManager;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.library.Library;

abstract class LibraryBaseActivity extends BaseActivity implements MenuItem.OnMenuItemClickListener {
	static final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
			getListView().invalidateViews();
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		}
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	protected static final String ACTION_FOUND = "fbreader.library.intent.FOUND";

	protected boolean runSearch(Intent intent) {
	   	final String pattern = intent.getStringExtra(SearchManager.QUERY);
		if (pattern == null || pattern.length() == 0) {
			return false;
		}
		BookSearchPatternOption.setValue(pattern);
		return LibraryInstance.searchBooks(pattern) != null;
	}

	protected void showNotFoundToast() {
		UIUtil.showErrorMessage(this, "bookNotFound");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        addMenuItem(menu, 1, "localSearch", R.drawable.ic_menu_search);
        return true;
    }

    private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
        final String label = Library.resource().getResource("menu").getResource(resourceKey).getValue();
        final MenuItem item = menu.add(0, index, Menu.NONE, label);
        item.setOnMenuItemClickListener(this);
        item.setIcon(iconId);
        return item;
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                return onSearchRequested();
            default:
                return true;
        }
    }
}
