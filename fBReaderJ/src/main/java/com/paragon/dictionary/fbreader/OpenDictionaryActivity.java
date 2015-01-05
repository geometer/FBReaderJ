/*
 * Copyright (C) 2013 Paragon Software Group
 * Author: Andrey Tumanov <Andrey_Tumanov@penreader.com>
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

package com.paragon.dictionary.fbreader;

import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;

import com.paragon.open.dictionary.api.*;

import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.ui.android.R;

public class OpenDictionaryActivity extends Activity {
	public final static String OPEN_DICTIONARY_QUERY_KEY = "open_dictionary_query";
	public final static String OPEN_DICTIONARY_HEIGHT_KEY = "open_dictionary_height";
	public final static String OPEN_DICTIONARY_GRAVITY_KEY = "open_dictionary_gravity";

	private WebView myArticleView = null;
	private TextView myTitleLabel = null;
	private ImageButton myOpenDictionaryButton = null;
	private String myQuery = null;
	private int myHeight;
	private int myGravity;
	private static Dictionary ourDictionary = null;

	static void setDictionary(Dictionary dictionary) {
		ourDictionary = dictionary;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.opendictionary_flyout);

		myArticleView = (WebView) findViewById(R.id.opendictionary_article_view);
		myTitleLabel = (TextView) findViewById(R.id.opendictionary_title_label);
		myOpenDictionaryButton = (ImageButton) findViewById(R.id.opendictionary_open_button);

		myOpenDictionaryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
				openTextInDictionary(myQuery);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		myQuery = getIntent().getStringExtra(OPEN_DICTIONARY_QUERY_KEY);
		if (myQuery == null)
			myQuery = "";
		myHeight = getIntent().getIntExtra(OPEN_DICTIONARY_HEIGHT_KEY, -1);
		myGravity = getIntent().getIntExtra(OPEN_DICTIONARY_GRAVITY_KEY, android.view.Gravity.BOTTOM);
		setViewSize(myHeight, myGravity);

		myArticleView.loadData("", "text/text", "UTF-8");
		if (ourDictionary != null) {
			myTitleLabel.setText(ourDictionary.getName());
			Log.d("FBReader", "OpenDictionaryActivity: get translation as text");
			ourDictionary.getTranslationAsText(myQuery, TranslateMode.SHORT, TranslateFormat.HTML, new Dictionary.TranslateAsTextListener() {
				@Override
				public void onComplete(String s, TranslateMode translateMode) {
					final String url = saveArticle(s.replace("</BODY>", "<br><br></BODY>"), getApplicationContext());
					if (MiscUtil.isEmptyString(url)) {
						openTextInDictionary(myQuery);
					} else {
						myArticleView.loadUrl(url);
					}
					Log.d("FBReader", "OpenDictionaryActivity: translation ready");
				}

				@Override
				public void onWordNotFound(ArrayList<String> similarWords) {
					finish();
					openTextInDictionary(myQuery);
					Log.d("FBReader", "OpenDictionaryActivity: word not found");
				}

				@Override
				public void onError(com.paragon.open.dictionary.api.Error error) {
					finish();
					Log.e("FBReader", error.getName());
					Log.e("FBReader", error.getMessage());
				}

				@Override
				public void onIPCError(String s) {
					finish();
					Log.e("FBReader", s);
				}
			});
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		setViewSize(myHeight, myGravity);
	}

	private void setViewSize(int height, int gravity) {
		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		if (height < 0)
			height = metrics.heightPixels / 3;
		TableRow bottomRow = (TableRow)findViewById(R.id.bottom_row);
		TableRow topRow = (TableRow)findViewById(R.id.top_row);

		View.OnTouchListener touchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				finish();
				return false;
			}
		};

		topRow.setOnTouchListener(touchListener);
		bottomRow.setOnTouchListener(touchListener);

		switch (gravity) {
			case android.view.Gravity.TOP:
				topRow.setMinimumHeight(0);
				bottomRow.setMinimumHeight(metrics.heightPixels - height);
				break;
			case android.view.Gravity.BOTTOM:
			default:
				bottomRow.setMinimumHeight(0);
				topRow.setMinimumHeight(metrics.heightPixels - height);
				break;
		}
	}

	private void openTextInDictionary(String text) {
		if (ourDictionary != null)
			ourDictionary.showTranslation(text);
	}

	private String saveArticle(String data, Context context) {
		final String filename = "open_dictionary_article.html";
		final FileOutputStream outputStream;

		try {
			outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(data.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return "file://" + context.getFilesDir().getAbsolutePath() + "/" + filename;
	}
}
