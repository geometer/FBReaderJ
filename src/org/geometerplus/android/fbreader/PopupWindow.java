/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

public class PopupWindow extends LinearLayout {
	public static enum Location {
		Bottom,
		Floating
	}

	private final Activity myActivity;

	public PopupWindow(Activity activity, RelativeLayout root, Location location, boolean fillWidth) {
		super(activity);
		myActivity = activity;

		setFocusable(false);
		
		final LayoutInflater inflater =
			(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(
			location == Location.Bottom
				? R.layout.control_panel_bottom : R.layout.control_panel_floating,
			this,
			true
		);

		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
			fillWidth ? ViewGroup.LayoutParams.FILL_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		root.addView(this, p);

		setVisibility(View.GONE);
	}

	Activity getActivity() {
		return myActivity;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	public void show() {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				setVisibility(View.VISIBLE);
			}
		});
	}

	public void hide() {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				setVisibility(View.GONE);
			}
		});
	}
	
	public void addView(View view) {
		((LinearLayout)findViewById(R.id.tools_plate)).addView(view);
	}
}
