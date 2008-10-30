/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLAndroidDialogContentWithFooter extends ZLAndroidDialogContent {
	private View myMainView;

	ZLAndroidDialogContentWithFooter(Context context, ZLResource resource, View footer) {
		super(context, resource);
		createListView(context);
		if (footer != null) {
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(myListView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			if (footer != null) {
				layout.addView(footer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			myMainView = layout;
		} else {
			myMainView = myListView;
		}
	}

	View getView() {
		return myMainView;
	}
}
