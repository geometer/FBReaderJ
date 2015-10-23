/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.view.View;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

final class TextSearchPopup extends PopupPanel implements View.OnClickListener {
	final static String ID = "TextSearchPopup";

	TextSearchPopup(FBReaderApp fbReader) {
		super(fbReader);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void hide_() {
		getReader().getTextView().clearFindResults();
		super.hide_();
	}

	@Override
	public synchronized void createControlPanel(FBReader activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getContext()) {
			return;
		}

		activity.getLayoutInflater().inflate(R.layout.search_panel, root);
		myWindow = (SimplePopupWindow)root.findViewById(R.id.search_panel);

		final ZLResource resource = ZLResource.resource("textSearchPopup");
		setupButton(R.id.search_panel_previous, resource.getResource("findPrevious").getValue());
		setupButton(R.id.search_panel_next, resource.getResource("findNext").getValue());
		setupButton(R.id.search_panel_close, resource.getResource("close").getValue());
	}

	private void setupButton(int buttonId, String description) {
		final View button = myWindow.findViewById(buttonId);
		button.setOnClickListener(this);
		button.setContentDescription(description);
	}

	@Override
	protected synchronized void update() {
		if (myWindow == null) {
			return;
		}

		myWindow.findViewById(R.id.search_panel_previous).setEnabled(
			Application.isActionEnabled(ActionCode.FIND_PREVIOUS)
		);
		myWindow.findViewById(R.id.search_panel_next).setEnabled(
			Application.isActionEnabled(ActionCode.FIND_NEXT)
		);
	}

	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.search_panel_previous:
				Application.runAction(ActionCode.FIND_PREVIOUS);
				break;
			case R.id.search_panel_next:
				Application.runAction(ActionCode.FIND_NEXT);
				break;
			case R.id.search_panel_close:
				Application.runAction(ActionCode.CLEAR_FIND_RESULTS);
				storePosition();
				StartPosition = null;
				Application.hideActivePopup();
		}
	}
}
