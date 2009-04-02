/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.LayoutInflater;
import android.widget.TabHost;
import android.widget.ListView;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.description.Author;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.collection.BookCollection;

public class LibraryTabActivity extends TabActivity {
	static LibraryTabActivity ourActivity;

	final ZLStringOption mySelectedTabOption = new ZLStringOption("TabActivity", "SelectedTab", "");
	
	private ListView createTab(String tag, int id) {
		final TabHost host = getTabHost();
		final String label = ZLResource.resource("libraryView").getResource(tag).getValue();
		host.addTab(host.newTabSpec(tag).setIndicator(label).setContent(id));
		return (ListView)findViewById(id);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.library, host.getTabContentView(), true);

		//host.addTab(host.newTabSpec("Network").setIndicator("Network").setContent(R.id.network));

		LibraryTabUtil.setAuthorList(createTab("byAuthor", R.id.by_author), null);
		LibraryTabUtil.setTagList(createTab("byTag", R.id.by_tag), "");
		LibraryTabUtil.setRecentBooksList(createTab("recent", R.id.recent));

		host.setCurrentTabByTag(mySelectedTabOption.getValue());
	}

	@Override
	public void onResume() {
		super.onResume();
		ourActivity = this;
	}

	@Override
	public void onPause() {
		ourActivity = null;
		super.onPause();
	}

	@Override
	public void onStop() {
		mySelectedTabOption.setValue(getTabHost().getCurrentTabTag());
		super.onStop();
	}
}
