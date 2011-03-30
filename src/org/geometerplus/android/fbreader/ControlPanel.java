/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

public class ControlPanel extends LinearLayout {
	public ControlPanel(Context context, RelativeLayout root, boolean fillWidth) {
		super(context);

		setFocusable(false);
		
		final LayoutInflater inflater =
			(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.control_panel, this, true);

		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
			fillWidth ? ViewGroup.LayoutParams.FILL_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		root.addView(this, p);

		setVisibility(View.GONE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	private interface VisibilityAction {
		int SHOW_ANIMATED = 0;
		int SHOW_INSTANTLY = 1;
		int HIDE_ANIMATED = 2;
		int HIDE_INSTANTLY = 3;
	}
	
	private Handler myVisibilityHandler = new Handler() {
		public void handleMessage(Message message) {
			switch (message.what) {
				case VisibilityAction.SHOW_ANIMATED:
					fade(View.VISIBLE, 0.0f, 1.0f);
					break;
				case VisibilityAction.SHOW_INSTANTLY:
					setVisibility(View.VISIBLE);
					break;
				case VisibilityAction.HIDE_ANIMATED:
					fade(View.GONE, 1.0f, 0.0f);
					break;
				case VisibilityAction.HIDE_INSTANTLY:
					setVisibility(View.GONE);
					break;
			}
		}
	};

	public void show(boolean animate) {
		myVisibilityHandler.sendEmptyMessage(animate ? VisibilityAction.SHOW_ANIMATED : VisibilityAction.SHOW_INSTANTLY);
	}

	public void hide(boolean animate) {
		myVisibilityHandler.sendEmptyMessage(animate ? VisibilityAction.HIDE_ANIMATED : VisibilityAction.HIDE_INSTANTLY);
	}
	
	private void fade(int visibility, float startAlpha, float endAlpha) {
		final AlphaAnimation animation = new AlphaAnimation(startAlpha, endAlpha);
		animation.setDuration(500);
		startAnimation(animation);
		setVisibility(visibility);
	}

	public void addView(View view) {
		((LinearLayout)findViewById(R.id.tools_plate)).addView(view);
	}
}
