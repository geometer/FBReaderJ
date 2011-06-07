/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;

public class SelectionButtonPanel extends SeveralButtonsPanel {
    SelectionButtonPanel(FBReaderApp fbReader) {
        super(fbReader);
    }

    @Override
	public void createControlPanel(FBReader activity, RelativeLayout root, ControlPanel.Location location) {
		super.createControlPanel(activity, root, location);
        addButton(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, R.drawable.selection_copy);
        addButton(ActionCode.SELECTION_SHARE, true, R.drawable.selection_share);
        addButton(ActionCode.SELECTION_OPEN_IN_DICTIONARY, true, R.drawable.selection_dictionary);
        addButton(ActionCode.SELECTION_ADD_BOOKMARK, true, R.drawable.selection_bookmark);
        addButton(ActionCode.SELECTION_HIDE_PANEL, true, R.drawable.selection_close);
    }
    
    public void move(int selectionStartY, int selectionEndY) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
		);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        final int verticalPosition; 
        final int screenHeight = ((View)myControlPanel.getParent()).getHeight();
		final int diffTop = screenHeight - selectionEndY;
		final int diffBottom = selectionStartY;
		if (diffTop > diffBottom) {
			verticalPosition = diffTop > myControlPanel.getHeight() + 10
				? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.CENTER_VERTICAL;
		} else {
			verticalPosition = diffBottom > myControlPanel.getHeight() + 10
				? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL;
		}

        layoutParams.addRule(verticalPosition);
        myControlPanel.setLayoutParams(layoutParams);
    }
}
