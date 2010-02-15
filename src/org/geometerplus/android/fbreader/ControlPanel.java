/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.application.ZLApplication;

class ActionButton extends ZoomButton {
	final String ActionId;
	final boolean IsCloseButton;

	ActionButton(Context context, String actionId, boolean isCloseButton) {
		super(context);
		ActionId = actionId;
		IsCloseButton = isCloseButton;
	}
}

public class ControlPanel extends LinearLayout implements View.OnClickListener {
	private final ArrayList<ActionButton> myButtons = new ArrayList<ActionButton>();
	private final LinearLayout myPlateLayout;

	public ControlPanel(Context context) {
		super(context);

		setFocusable(false);
		
		final LayoutInflater inflater =
			(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.control_panel, this, true);
		myPlateLayout = (LinearLayout)findViewById(R.id.tools_plate);
	}

	public void addButton(String actionId, boolean isCloseButton, int imageId) {
		final ActionButton button = new ActionButton(getContext(), actionId, isCloseButton);
		button.setImageResource(imageId);
		button.setOnClickListener(this);
		myPlateLayout.addView(button);
		myButtons.add(button);
	}

	public void onClick(View view) {
		final ActionButton button = (ActionButton)view;
		ZLApplication.Instance().doAction(button.ActionId);
		if (button.IsCloseButton) {
			hide(true);
		}
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
	
	public void updateStates() {
		final ZLApplication application = ZLApplication.Instance();
		for (ActionButton button : myButtons) {
			button.setEnabled(application.isActionEnabled(button.ActionId));
		}
	}
	
	@Override
	public boolean hasFocus() {
		for (ActionButton button : myButtons) {
			if (button.hasFocus()) {
				return true;
			}
		}
		return false;
	}
}
