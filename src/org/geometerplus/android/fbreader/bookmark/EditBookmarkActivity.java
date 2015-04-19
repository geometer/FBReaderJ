/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.bookmark;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;

import yuku.ambilwarna.widget.AmbilWarnaPrefWidgetView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import org.geometerplus.fbreader.book.*;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.ViewUtil;

public class EditBookmarkActivity extends Activity implements IBookCollection.Listener {
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private Bookmark myBookmark;
	private StyleListAdapter myStylesAdapter;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_bookmark);

		myBookmark = FBReaderIntents.getBookmarkExtra(getIntent());
		if (myBookmark == null) {
			finish();
			return;
		}

        final TabHost tabHost = (TabHost)findViewById(R.id.edit_bookmark_tabhost);
		tabHost.setup();

		final TabHost.TabSpec textSpec = tabHost.newTabSpec("text");
		textSpec.setIndicator("Text");
		textSpec.setContent(R.id.edit_bookmark_content_text);
        tabHost.addTab(textSpec);

		final TabHost.TabSpec styleSpec = tabHost.newTabSpec("style");
		styleSpec.setIndicator("Style");
		styleSpec.setContent(R.id.edit_bookmark_content_style);
        tabHost.addTab(styleSpec);

		final TabHost.TabSpec deleteSpec = tabHost.newTabSpec("delete");
		deleteSpec.setIndicator("Delete");
		deleteSpec.setContent(R.id.edit_bookmark_content_delete);
        tabHost.addTab(deleteSpec);

		final EditText editor = (EditText)findViewById(R.id.edit_bookmark_text);
		editor.setText(myBookmark.getText());
		final int len = editor.getText().length();
		editor.setSelection(len, len);

		final Button deleteButton = (Button)findViewById(R.id.edit_bookmark_delete_button);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				myCollection.bindToService(EditBookmarkActivity.this, new Runnable() {
					public void run() {
						myCollection.deleteBookmark(myBookmark);
						finish();
					}
				});
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				final List<HighlightingStyle> styles = myCollection.highlightingStyles();
				if (styles.isEmpty()) {
					finish();
					return;
				}
				myStylesAdapter = new StyleListAdapter(styles);
				final ListView stylesList =
					(ListView)findViewById(R.id.edit_bookmark_content_style);
				stylesList.setAdapter(myStylesAdapter);
				stylesList.setOnItemClickListener(myStylesAdapter);
				myCollection.addListener(EditBookmarkActivity.this);
			}
		});
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();
		super.onDestroy();
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.BookmarkStyleChanged) {
			myStylesAdapter.setStyleList(myCollection.highlightingStyles());
		}
	}

	// method from IBookCollection.Listener
	public void onBuildEvent(IBookCollection.Status status) {
	}

	private class StyleListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final List<HighlightingStyle> myStyles;

		StyleListAdapter(List<HighlightingStyle> styles) {
			myStyles = new ArrayList<HighlightingStyle>(styles);
		}

		public synchronized void setStyleList(List<HighlightingStyle> styles) {
			myStyles.clear();
			myStyles.addAll(styles);
			notifyDataSetChanged();
		}

		public final synchronized int getCount() {
			return myStyles.size();
		}

		public final synchronized HighlightingStyle getItem(int position) {
			return myStyles.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public final synchronized View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.style_item, parent, false);
			final HighlightingStyle style = getItem(position);

			final AmbilWarnaPrefWidgetView colorView =
				(AmbilWarnaPrefWidgetView)ViewUtil.findView(view, R.id.style_item_color);
			final TextView titleView = ViewUtil.findTextView(view, R.id.style_item_title);
			final Button button = (Button)ViewUtil.findView(view, R.id.style_item_edit_button);

			final ZLResource resource = ZLResource.resource("highlightingStyleMenu");

			String name = style.getName();
			if (name == null || "".equals(name)) {
				name = resource
					.getResource("style").getValue()
					.replace("%s", String.valueOf(style.Id));
			}
			final ZLColor color = style.getBackgroundColor();
			final int rgb = color != null ? ZLAndroidColorUtil.rgb(color) : -1;

			colorView.setVisibility(View.VISIBLE);
			if (rgb != -1) {
				colorView.showCross(false);
				colorView.setBackgroundColor(rgb);
			} else {
				colorView.showCross(true);
				colorView.setBackgroundColor(0);
			}
			titleView.setText(name);

			button.setVisibility(View.VISIBLE);
			button.setText(resource.getResource("editStyle").getValue());
			button.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View view) {
					startActivity(
						new Intent(EditBookmarkActivity.this, EditStyleActivity.class)
							.putExtra(EditStyleActivity.STYLE_ID_KEY, style.Id)
					);
				}
			});

			return view;
		}

		public final synchronized void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final HighlightingStyle style = getItem(position);
			myCollection.bindToService(EditBookmarkActivity.this, new Runnable() {
				public void run() {
					myBookmark.setStyleId(style.Id);
					myCollection.saveBookmark(myBookmark);
				}
			});
		}
	}
}
