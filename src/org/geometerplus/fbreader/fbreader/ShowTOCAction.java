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

package org.geometerplus.fbreader.fbreader;

//import android.net.Uri;
//import android.content.Intent;

import org.geometerplus.android.fbreader.TOCActivity;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

class ShowTOCAction extends FBAction {
	ShowTOCAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		// TODO: check if contents model is empty
		//return !Reader.ContentsView.isEmpty();
		return Reader.myBookModel.ContentsTree.getSize() > 1;
	}

	public void run() {
		final ZLAndroidDialogManager dialogManager =
			(ZLAndroidDialogManager)ZLAndroidDialogManager.getInstance();
		/*
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
        dialogManager.runActivity(intent);
		*/
		// TODO: open tree
		dialogManager.runActivity(TOCActivity.class, TOCActivity.DATA_KEY, Reader.myBookModel.ContentsTree);
	}
}
