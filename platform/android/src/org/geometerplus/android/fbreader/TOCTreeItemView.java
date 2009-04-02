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

import android.view.ContextMenu;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;

public class TOCTreeItemView extends LinearLayout {
	public TOCTreeItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}

	public TOCTreeItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TOCTreeItemView(Context context) {
		super(context);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu) {
		System.err.println("onCreateContextMenu");
		menu.add("Item 0");
		menu.add("Item 1");
		menu.add("Item 2");
	}
}
