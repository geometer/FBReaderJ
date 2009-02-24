/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.description.Author;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.collection.BookCollection;

public class LibraryTabActivity extends TabActivity {
	static LibraryTabActivity ourActivity;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.library, host.getTabContentView(), true);

		host.addTab(host.newTabSpec("By Author").setIndicator("By Author").setContent(R.id.by_author));
		host.addTab(host.newTabSpec("By Tag").setIndicator("By Tag").setContent(R.id.by_tag));
		host.addTab(host.newTabSpec("Recent").setIndicator("Recent").setContent(R.id.recent));
		//host.addTab(host.newTabSpec("Network").setIndicator("Network").setContent(R.id.network));

		LibraryTabUtil.setAuthorList((ListView)findViewById(R.id.by_author), null);
		LibraryTabUtil.setTagList((ListView)findViewById(R.id.by_tag), "");
		LibraryTabUtil.setRecentBooksList((ListView)findViewById(R.id.recent));
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
}
