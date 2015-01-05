/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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
import android.app.Activity;
import android.view.*;
import android.widget.*;
import android.content.Context;


class SimpleContainer extends ViewGroup {
	private final View myEditText;
	private final Button myOkButton;
	private final Button myCancelButton;

	SimpleContainer(Context context) {
		super(context);
		myEditText = new EditText(context);
		myOkButton = new Button(context);
		myOkButton.setText("ok");
		myCancelButton = new Button(context);
		myCancelButton.setText("cancel");
		addView(myOkButton);
		addView(myCancelButton);
		addView(myEditText);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int buttonHeight = Math.max(54, Math.max(myOkButton.getHeight(), myCancelButton.getHeight()));
		myEditText.layout(left + 8, top + 8, right - 8, bottom - buttonHeight - 16);
		myOkButton.layout(left + 8, bottom - buttonHeight - 8, (left + right) / 2 - 4, bottom - 8);
		myCancelButton.layout((left + right) / 2 + 4, bottom - buttonHeight - 8, right - 8, bottom - 8);
	}
}

public class BookmarkEditActivity extends Activity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final SimpleContainer container = new SimpleContainer(this);
		setContentView(container);
	}
}
