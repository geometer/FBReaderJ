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
import android.app.Activity;
import android.view.*;
import android.widget.*;
import android.content.Context;

import org.geometerplus.zlibrary.ui.android.R;

class SimpleContainer extends ViewGroup {
	private final View myChild;

	SimpleContainer(Context context, View child) {
		super(context);
		myChild = child;
		addView(child);
	}

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		myChild.layout(left + 5, top + 5, right - 5, bottom - 5);
	}
}

public class BookmarkEditActivity extends Activity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final LinearLayout v = new LinearLayout(this);
		v.setOrientation(LinearLayout.VERTICAL);
		v.addView(new EditText(this));
		/*
		final LinearLayout h = new LinearLayout(this);
		v.addView(h);
		final Button okButton = new Button(this);
		okButton.setText("ok");
		h.addView(okButton);
		final Button cancelButton = new Button(this);
		cancelButton.setText("cancel");
		h.addView(cancelButton);
		*/
		final SimpleContainer container = new SimpleContainer(this, v);
		//final SimpleContainer container = new SimpleContainer(this, new EditText(this));
		setContentView(container);
	}
}
