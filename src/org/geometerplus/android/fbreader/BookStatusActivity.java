/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageLoader;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.Author;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.SeriesInfo;
import org.geometerplus.fbreader.library.Tag;

import org.geometerplus.android.fbreader.preferences.BookInfoActivity;


public class BookStatusActivity extends Activity {

	private ZLResource myResource = ZLResource.resource("bookInfo");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
		if (fbreader == null || fbreader.Model == null || fbreader.Model.Book == null) {
			finish();
			return;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.book_info);
	}

	@Override
	protected void onStart() {
		super.onStart();

		final ZLFile bookFile = ((FBReaderApp)FBReaderApp.Instance()).Model.Book.File;
		final Book book = Book.getByFile(bookFile); 
		setupCover(book);
		setupBookInfo(book);
		setupFileInfo(book);

		final View root = findViewById(R.id.book_info_root);
		root.invalidate();
		root.requestLayout();
	}


	protected MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
		final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
		return menu.add(0, index, Menu.NONE, label).setIcon(iconId);
	}

	private static final int MENU_EDIT = 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, MENU_EDIT, "edit", android.R.drawable.ic_menu_edit);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EDIT:
			startEditDialog();
			return true;
		default:
			return true;
		}
	}

	private void startEditDialog() {
		final Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
		final BookModel model = ((FBReaderApp)FBReaderApp.Instance()).Model;
		if (model != null && model.Book != null) {
			final ZLFile file = model.Book.File;
			intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, file.getPath());
		}
		startActivity(intent);
	}

	private void setupInfoPair(int id, String key, CharSequence value) {
		LinearLayout layout = (LinearLayout)findViewById(id);
		if (value == null || value.length() == 0) {
			layout.setVisibility(View.GONE);
			return;
		}
		layout.setVisibility(View.VISIBLE);
		((TextView)layout.findViewById(R.id.book_info_key)).setText(myResource.getResource(key).getValue());
		((TextView)layout.findViewById(R.id.book_info_value)).setText(value);
	}

	private void setupCover(Book book) {
		final ImageView coverView = (ImageView) findViewById(R.id.book_cover);

		final int maxHeight = 250; // FIXME: hardcoded constant
		final int maxWidth = maxHeight * 3 / 4;

		coverView.setVisibility(View.GONE);
		coverView.setImageDrawable(null);

		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(book.File);
		if (plugin != null) {
			final ZLImage image = plugin.readCover(book);
			if (image != null) {
				final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager.Instance();
				final Runnable refreshRunnable = new Runnable() {
					public void run() {
						ZLAndroidImageData data = mgr.getImageData(image);
						if (data != null) {
							final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
							if (coverBitmap != null) {
								coverView.setVisibility(View.VISIBLE);
								coverView.getLayoutParams().width = maxWidth;
								coverView.getLayoutParams().height = maxHeight;
								coverView.setImageBitmap(coverBitmap);
							}
						}
					}
				};
				if (image instanceof ZLImageProxy) {
					ZLImageProxy proxy = (ZLImageProxy)image;
					if (!proxy.isSynchronized()) {
						proxy.synchronize();
					}
					refreshRunnable.run();
				} else if (image instanceof ZLLoadableImage) {
					ZLLoadableImage loadable = (ZLLoadableImage)image;
					if (loadable.isSynchronized()) {
						refreshRunnable.run();
					} else {
						ZLAndroidImageLoader.Instance().startImageLoading(loadable, refreshRunnable);
					}
				} else {
					refreshRunnable.run();
				}
			}
		}
	}

	private void setupBookInfo(Book book) {
		((TextView)findViewById(R.id.book_info_title)).setText(myResource.getResource("bookInfo").getValue());

		setupInfoPair(R.id.book_title, "title", book.getTitle());

		final StringBuilder buffer = new StringBuilder();
		for (Author author: book.authors()) {
			if (buffer.length() > 0) {
				buffer.append(", ");
			}
			buffer.append(author.DisplayName);
		}
		setupInfoPair(R.id.book_authors, "authors", buffer);

		SeriesInfo series = book.getSeriesInfo();
		setupInfoPair(R.id.book_series, "series",
				(series == null) ? null : series.Name);
		setupInfoPair(R.id.book_series_index, "indexInSeries",
				(series == null || series.Index <= 0) ? null : String.valueOf(series.Index));

		buffer.delete(0, buffer.length());
		for (Tag tag: book.tags()) {
			if (buffer.length() > 0) {
				buffer.append(", ");
			}
			buffer.append(tag.Name);
		}
		setupInfoPair(R.id.book_tags, "tags", buffer);
	}

	private void setupFileInfo(Book book) {
		((TextView)findViewById(R.id.file_info_title)).setText(myResource.getResource("fileInfo").getValue());

		setupInfoPair(R.id.file_name, "name", book.File.getPath());
		setupInfoPair(R.id.file_type, "type", book.File.getExtension());

		final ZLFile physFile = book.File.getPhysicalFile();
		final File file = physFile == null ? null : new File(physFile.getPath());
		if (file != null && file.exists() && file.isFile()) {
			setupInfoPair(R.id.file_size, "size", formatSize(file.length()));
			setupInfoPair(R.id.file_time, "time", formatDate(file.lastModified()));
		} else {
			setupInfoPair(R.id.file_size, "size", null);
			setupInfoPair(R.id.file_time, "time", null);
		}
	}

	private String formatSize(long size) {
		if (size <= 0) {
			return null;
		}
		final int kilo = 1024;
		if (size < kilo) { // less than 1 kilobyte
			return myResource.getResource("sizeInBytes").getValue().replaceAll("%s", String.valueOf(size));
		}
		final String value;
		if (size < kilo * kilo) { // less than 1 megabyte
			value = String.format("%.2f", ((float)size) / kilo);
		} else {
			value = String.valueOf(size / kilo);
		}
		return myResource.getResource("sizeInKiloBytes").getValue().replaceAll("%s", value);
	}

	private String formatDate(long date) {
		if (date == 0) {
			return null;
		}
		return DateFormat.getDateTimeInstance().format(new Date(date));
	}
}
