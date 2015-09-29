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

package org.geometerplus.android.fbreader.bookmark;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Window;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.fbreader.book.BookmarkUtil;
import org.geometerplus.fbreader.book.HighlightingStyle;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.preferences.*;

public class EditStyleActivity extends PreferenceActivity {
	static final String STYLE_ID_KEY = "style.id";

	private final ZLResource myRootResource = ZLResource.resource("editStyle");
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private HighlightingStyle myStyle;
	private BgColorPreference myBgColorPreference;

	@Override
	protected void onCreate(Bundle bundle) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
		setPreferenceScreen(screen);

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				myStyle = myCollection.getHighlightingStyle(getIntent().getIntExtra(STYLE_ID_KEY, -1));
				if (myStyle == null) {
					finish();
					return;
				}
				screen.addPreference(new NamePreference());
				screen.addPreference(new InvisiblePreference());
				myBgColorPreference = new BgColorPreference();
				screen.addPreference(myBgColorPreference);
			}
		});
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();

		super.onDestroy();
	}

	private class NamePreference extends ZLStringPreference {
		NamePreference() {
			super(EditStyleActivity.this, myRootResource, "name");
			super.setValue(BookmarkUtil.getStyleName(myStyle));
		}

		@Override
		protected void setValue(String value) {
			super.setValue(value);
			BookmarkUtil.setStyleName(myStyle, value);
			myCollection.saveHighlightingStyle(myStyle);
		}
	}

	private class InvisiblePreference extends ZLCheckBoxPreference {
		private ZLColor mySavedBgColor;

		InvisiblePreference() {
			super(EditStyleActivity.this, myRootResource.getResource("invisible"));
			setChecked(myStyle.getBackgroundColor() == null);
		}

		@Override
		protected void onClick() {
			super.onClick();
			if (isChecked()) {
				mySavedBgColor = myStyle.getBackgroundColor();
				myStyle.setBackgroundColor(null);
				myBgColorPreference.setEnabled(false);
			} else {
				myStyle.setBackgroundColor(
					mySavedBgColor != null ? mySavedBgColor : new ZLColor(127, 127, 127)
				);
				myBgColorPreference.setEnabled(true);
			}
			myCollection.saveHighlightingStyle(myStyle);
		}
	}

	private class BgColorPreference extends ColorPreference {
		BgColorPreference() {
			super(EditStyleActivity.this);
			setEnabled(getSavedColor() != null);
		}

		@Override
		public String getTitle() {
			return myRootResource.getResource("bgColor").getValue();
		}

		@Override
		protected ZLColor getSavedColor() {
			return myStyle.getBackgroundColor();
		}

		@Override
		protected void saveColor(ZLColor color) {
			myStyle.setBackgroundColor(color);
			myCollection.saveHighlightingStyle(myStyle);
		}
	}
}
