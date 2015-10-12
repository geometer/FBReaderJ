/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.File;
import java.text.DateFormat;
import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.HtmlUtil;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.preferences.EditBookInfoActivity;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.android.util.OrientationUtil;

public class BookInfoActivity extends Activity implements IBookCollection.Listener<Book> {
	private static final boolean ENABLE_EXTENDED_FILE_INFO = false;

	public static final String FROM_READING_MODE_KEY = "fbreader.from.reading.mode";

	private final ZLResource myResource = ZLResource.resource("bookInfo");
	private Book myBook;
	private boolean myDontReloadBook;

	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);

	private final BookCollectionShadow myCollection = new BookCollectionShadow();

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(
			new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this)
		);

		final Intent intent = getIntent();
		myDontReloadBook = intent.getBooleanExtra(FROM_READING_MODE_KEY, false);
		myBook = FBReaderIntents.getBookExtra(intent, myCollection);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.book_info);
	}

	@Override
	protected void onStart() {
		super.onStart();

		OrientationUtil.setOrientation(this, getIntent());

		final PluginCollection pluginCollection =
			PluginCollection.Instance(Paths.systemInfo(this));

		if (myBook != null) {
			// we force language & encoding detection
			BookUtil.getEncoding(myBook, pluginCollection);

			setupCover(myBook, pluginCollection);
			setupBookInfo(myBook);
			setupAnnotation(myBook, pluginCollection);
			setupFileInfo(myBook);
		}

		setupButton(R.id.book_info_button_open, "openBook", new View.OnClickListener() {
			public void onClick(View view) {
				if (myDontReloadBook) {
					finish();
				} else {
					FBReader.openBookActivity(BookInfoActivity.this, myBook, null);
				}
			}
		});
		setupButton(R.id.book_info_button_edit, "edit", new View.OnClickListener() {
			public void onClick(View view) {
				final Intent intent =
					new Intent(getApplicationContext(), EditBookInfoActivity.class);
				FBReaderIntents.putBookExtra(intent, myBook);
				OrientationUtil.startActivity(BookInfoActivity.this, intent);
			}
		});
		setupButton(R.id.book_info_button_reload, "reloadInfo", new View.OnClickListener() {
			public void onClick(View view) {
				if (myBook != null) {
					BookUtil.reloadInfoFromFile(myBook, pluginCollection);
					setupBookInfo(myBook);
					myDontReloadBook = false;
					myCollection.bindToService(BookInfoActivity.this, new Runnable() {
						public void run() {
							myCollection.saveBook(myBook);
						}
					});
				}
			}
		});

		final View root = findViewById(R.id.book_info_root);
		root.invalidate();
		root.requestLayout();

		myCollection.bindToService(this, null);
		myCollection.addListener(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}

	@Override
	protected void onDestroy() {
		myCollection.removeListener(this);
		myCollection.unbind();
		myImageSynchronizer.clear();

		super.onDestroy();
	}

	private Button findButton(int buttonId) {
		return (Button)findViewById(buttonId);
	}

	private void setupButton(int buttonId, String resourceKey, View.OnClickListener listener) {
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Button button = findButton(buttonId);
		button.setText(buttonResource.getResource(resourceKey).getValue());
		button.setOnClickListener(listener);
	}

	private void setupInfoPair(int id, String key, CharSequence value) {
		setupInfoPair(id, key, value, 0);
	}

	private void setupInfoPair(int id, String key, CharSequence value, int param) {
		final LinearLayout layout = (LinearLayout)findViewById(id);
		if (value == null || value.length() == 0) {
			layout.setVisibility(View.GONE);
			return;
		}
		layout.setVisibility(View.VISIBLE);
		((TextView)layout.findViewById(R.id.book_info_key)).setText(myResource.getResource(key).getValue(param));
		((TextView)layout.findViewById(R.id.book_info_value)).setText(value);
	}

	private void setupCover(Book book, PluginCollection pluginCollection) {
		final ImageView coverView = (ImageView)findViewById(R.id.book_cover);
		final Object oldBook = coverView.getTag();
		if (oldBook instanceof Book && book.getId() == ((Book)oldBook).getId()) {
			return;
		}
		coverView.setTag(book);

		coverView.setVisibility(View.GONE);
		coverView.setImageDrawable(null);

		final ZLImage image = CoverUtil.getCover(book, pluginCollection);

		if (image == null) {
			return;
		}

		if (image instanceof ZLImageProxy) {
			((ZLImageProxy)image).startSynchronization(myImageSynchronizer, new Runnable() {
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							setCover(coverView, image);
						}
					});
				}
			});
		} else {
			setCover(coverView, image);
		}
	}

	private void setCover(ImageView coverView, ZLImage image) {
		final ZLAndroidImageData data =
			((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
		if (data == null) {
			return;
		}

		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		final int maxHeight = metrics.heightPixels * 2 / 3;
		final int maxWidth = maxHeight * 2 / 3;

		final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
		if (coverBitmap == null) {
			return;
		}

		coverView.setVisibility(View.VISIBLE);
		coverView.getLayoutParams().width = maxWidth;
		coverView.getLayoutParams().height = maxHeight;
		coverView.setImageBitmap(coverBitmap);
	}

	private void setupBookInfo(Book book) {
		((TextView)findViewById(R.id.book_info_title)).setText(myResource.getResource("bookInfo").getValue());

		setupInfoPair(R.id.book_title, "title", book.getTitle());
		setupInfoPair(R.id.book_authors, "authors", book.authorsString(", "), book.authors().size());

		final SeriesInfo series = book.getSeriesInfo();
		setupInfoPair(R.id.book_series, "series", series == null ? null : series.Series.getTitle());
		String seriesIndexString = null;
		if (series != null && series.Index != null) {
			seriesIndexString = series.Index.toPlainString();
		}
		setupInfoPair(R.id.book_series_index, "indexInSeries", seriesIndexString);
		setupInfoPair(R.id.book_tags, "tags", book.tagsString(", "), book.tags().size());
		String language = book.getLanguage();
		if (!ZLLanguageUtil.languageCodes().contains(language)) {
			language = Language.OTHER_CODE;
		}
		setupInfoPair(R.id.book_language, "language", new Language(language).Name);
	}

	private void setupAnnotation(Book book, PluginCollection pluginCollection) {
		final TextView titleView = (TextView)findViewById(R.id.book_info_annotation_title);
		final TextView bodyView = (TextView)findViewById(R.id.book_info_annotation_body);
		final String annotation = BookUtil.getAnnotation(book, pluginCollection);
		if (annotation == null) {
			titleView.setVisibility(View.GONE);
			bodyView.setVisibility(View.GONE);
		} else {
			titleView.setText(myResource.getResource("annotation").getValue());
			bodyView.setText(HtmlUtil.getHtmlText(NetworkLibrary.Instance(Paths.systemInfo(this)), annotation));
			bodyView.setMovementMethod(new LinkMovementMethod());
			bodyView.setTextColor(ColorStateList.valueOf(bodyView.getTextColors().getDefaultColor()));
		}
	}

	private void setupFileInfo(Book book) {
		((TextView)findViewById(R.id.file_info_title)).setText(myResource.getResource("fileInfo").getValue());

		setupInfoPair(R.id.file_name, "name", book.getPath());
		if (ENABLE_EXTENDED_FILE_INFO) {
			final ZLFile bookFile = BookUtil.fileByBook(book);
			setupInfoPair(R.id.file_type, "type", bookFile.getExtension());

			final ZLPhysicalFile physFile = bookFile.getPhysicalFile();
			final File file = physFile == null ? null : physFile.javaFile();
			if (file != null && file.exists() && file.isFile()) {
				setupInfoPair(R.id.file_size, "size", formatSize(file.length()));
				setupInfoPair(R.id.file_time, "time", formatDate(file.lastModified()));
			} else {
				setupInfoPair(R.id.file_size, "size", null);
				setupInfoPair(R.id.file_time, "time", null);
			}
		} else {
			setupInfoPair(R.id.file_type, "type", null);
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
			return myResource.getResource("sizeInBytes").getValue((int)size).replaceAll("%s", String.valueOf(size));
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

	public void onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.Updated && myCollection.sameBook(book, myBook)) {
			myBook.updateFrom(book);
			setupBookInfo(book);
			myDontReloadBook = false;
		}
	}

	public void onBuildEvent(IBookCollection.Status status) {
	}
}
