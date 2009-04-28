/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.fbreader.fbreader.ActionCode;

public class TextSearchControls extends LinearLayout implements View.OnClickListener, ZLApplication.ButtonPanel {
	private final ZoomButton myFindPreviousButton;
	private final ZoomButton myFindNextButton;
	private final ZoomButton myCloseButton;

	boolean Visible;
		
	public TextSearchControls(Context context) {
		super(context);

		setFocusable(false);
		
		final LayoutInflater inflater =
			(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.text_search_controls, this, true);
		//setBackgroundResource(android.R.drawable.zoom_plate);
		//setPadding(15, 15, 15, 0);

		myFindPreviousButton = (ZoomButton)findViewById(R.id.previous);
		//myFindPreviousButton = new ZoomButton(context);
		//myFindPreviousButton.setImageResource(R.drawable.text_search_previous);
		myFindPreviousButton.setOnClickListener(this);
		//addView(myFindPreviousButton);

		myCloseButton = (ZoomButton)findViewById(R.id.close);
		//myCloseButton = new ZoomButton(context);
		//myCloseButton.setImageResource(R.drawable.text_search_close);
		myCloseButton.setEnabled(true);
		myCloseButton.setOnClickListener(this);
		//addView(myCloseButton);

		myFindNextButton = (ZoomButton)findViewById(R.id.next);
		//myFindNextButton = new ZoomButton(context);
		//myFindNextButton.setImageResource(R.drawable.text_search_next);
		myFindNextButton.setOnClickListener(this);
		//addView(myFindNextButton);
	}

	public void onClick(View view) {
		if (view == myFindPreviousButton) {
			ZLApplication.Instance().doAction(ActionCode.FIND_PREVIOUS);
		} else if (view == myFindNextButton) {
			ZLApplication.Instance().doAction(ActionCode.FIND_NEXT);
		} else {
			ZLApplication.Instance().doAction(ActionCode.CLEAR_FIND_RESULTS);
			hide(true);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
	public void show(boolean animate) {
		if (animate) {
			fade(View.VISIBLE, 0.0f, 1.0f);
		} else {
			setVisibility(View.VISIBLE);
		}
		Visible = true;
	}
	
	public void hide(boolean animate) {
		if (animate) {
			fade(View.GONE, 1.0f, 0.0f);
		} else {
			setVisibility(View.GONE);
		}
		Visible = false;
	}
	
	private void fade(int visibility, float startAlpha, float endAlpha) {
		final AlphaAnimation animation = new AlphaAnimation(startAlpha, endAlpha);
		animation.setDuration(500);
		startAnimation(animation);
		setVisibility(visibility);
	}
	
	public void updateStates() {
		final ZLApplication application = ZLApplication.Instance();
		myFindNextButton.setEnabled(application.isActionEnabled(ActionCode.FIND_NEXT));
		myFindPreviousButton.setEnabled(application.isActionEnabled(ActionCode.FIND_PREVIOUS));
	}
	
	@Override
	public boolean hasFocus() {
		return myFindPreviousButton.hasFocus() || myFindNextButton.hasFocus() || myCloseButton.hasFocus();
	}
}
