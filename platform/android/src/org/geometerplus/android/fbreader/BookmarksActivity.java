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

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.Context;
import android.app.ListActivity;
import android.graphics.drawable.Drawable;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.impl.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.fbreader.library.*;

public class BookmarksActivity extends ListActivity {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	private BookmarkList myBookmarks;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		myBookmarks = new BookmarkList(((FBReader)FBReader.Instance()).Model.Book);
		final BookmarksAdapter adapter = new BookmarksAdapter();
		final ListView listView = getListView();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(adapter);
		listView.setOnCreateContextMenuListener(adapter);
	}

	@Override
	public void onPause() {
		myBookmarks.save();
		super.onPause();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		switch (item.getItemId()) {
			case OPEN_ITEM_ID:
				gotoBookmark(position - 1);
				return true;
			case EDIT_ITEM_ID:
				// TODO: implement
				return true;
			case DELETE_ITEM_ID:
				myBookmarks.removeBookmark(position - 1);
				getListView().invalidateViews();
				getListView().requestLayout();
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void addBookmark() {
		ZLTextWordCursor cursor = ((FBReader)FBReader.Instance()).BookTextView.getStartCursor();
		if (!cursor.isNull()) {
			// TODO: implement
		}

		final ZLTextPosition position = new ZLTextPosition(cursor);
		final StringBuilder builder = new StringBuilder();
		int wordCounter = 0;
		cursor = new ZLTextWordCursor(cursor);

mainLoop:
		do {
			for (; !cursor.isEndOfParagraph(); cursor.nextWord()) {
				final ZLTextElement element = cursor.getElement();
				if (element instanceof ZLTextWord) {
					final ZLTextWord word = (ZLTextWord)element;
					if (builder.length() > 0) {
						builder.append(" ");
					}
					builder.append(word.Data, word.Offset, word.Length);
					if (++wordCounter >= 10) {
						break mainLoop;
					}
				}
			}
		} while ((builder.length() == 0) && cursor.nextParagraph());

		// TODO: text edit dialog
		myBookmarks.addNewBookmark(builder.toString(), position);
		getListView().invalidateViews();
		getListView().requestLayout();
	}

	private void gotoBookmark(int index) {
		myBookmarks.gotoBookmark(index, ((FBReader)FBReader.Instance()).BookTextView);
		finish();
	}

	private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
		BookmarksAdapter() {
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if (position > 0) {
				menu.setHeaderTitle(getItem(position).getText());
				final ZLResource resource = ZLResource.resource("bookmarksView");
				menu.add(0, OPEN_ITEM_ID, 0, resource.getResource("open").getValue());
				menu.add(0, EDIT_ITEM_ID, 0, resource.getResource("edit").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, resource.getResource("delete").getValue());
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
			((ImageView)view.findViewById(R.id.bookmark_item_icon)).setImageResource(
				(position > 0) ? R.drawable.tree_icon_strut : R.drawable.tree_icon_plus
			);
			((TextView)view.findViewById(R.id.bookmark_item_text)).setText(
				(position > 0) ?
					getItem(position).getText() :
					ZLResource.resource("bookmarksView").getResource("new").getValue()
			);
			return view;
		}

		public final boolean areAllItemsEnabled() {
			return true;
		}

		public final boolean isEnabled(int position) {
			return true;
		}

		public final long getItemId(int position) {
			return position;
		}
	
		public final Bookmark getItem(int position) {
			return (position > 0) ? myBookmarks.get(position - 1) : null;
		}

		public final int getCount() {
			return myBookmarks.size() + 1;
		}

		public final void onItemClick(AdapterView parent, View view, int position, long id) {
			if (position == 0) {
				addBookmark();
			} else {
				gotoBookmark(position - 1);
			}
		}
	}
}
