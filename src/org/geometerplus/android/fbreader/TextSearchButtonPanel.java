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

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomButton;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class ActionButton extends ZoomButton {
	final String ActionId;
	final boolean IsCloseButton;

	ActionButton(Context context, String actionId, boolean isCloseButton) {
		super(context);
		ActionId = actionId;
		IsCloseButton = isCloseButton;
	}
}

final class TextSearchButtonPanel extends ControlButtonPanel implements View.OnClickListener {
	private final ArrayList<ActionButton> myButtons = new ArrayList<ActionButton>();

	TextSearchButtonPanel(FBReaderApp fbReader) {
		super(fbReader);
	}

	@Override
	public void onHide() {
		Reader.getTextView().clearFindResults();
	}

	@Override
	public void createControlPanel(FBReader activity, RelativeLayout root) {
		myControlPanel = new ControlPanel(activity, root, false);

		addButton(ActionCode.FIND_PREVIOUS, false, R.drawable.text_search_previous);
		addButton(ActionCode.CLEAR_FIND_RESULTS, true, R.drawable.text_search_close);
		addButton(ActionCode.FIND_NEXT, false, R.drawable.text_search_next);
	}

	private void addButton(String actionId, boolean isCloseButton, int imageId) {
		final ActionButton button = new ActionButton(myControlPanel.getContext(), actionId, isCloseButton);
		button.setImageResource(imageId);
		myControlPanel.addView(button);
		button.setOnClickListener(this);
		myButtons.add(button);
	}

	@Override
	public void updateStates() {
		for (ActionButton button : myButtons) {
			button.setEnabled(Reader.isActionEnabled(button.ActionId));
		}
	}

	public void onClick(View view) {
		final ActionButton button = (ActionButton)view;
		Reader.doAction(button.ActionId);
		if (button.IsCloseButton && myControlPanel != null) {
			storePosition();
			StartPosition = null;
			myControlPanel.hide(true);
		}
	}
}
