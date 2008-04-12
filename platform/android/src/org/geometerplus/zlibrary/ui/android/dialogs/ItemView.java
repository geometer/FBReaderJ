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

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLTreeNode;

import org.geometerplus.zlibrary.ui.android.library.R;

class ItemView extends LinearLayout {
	static private final HashMap ourIconMap = new HashMap();

	private final ZLTreeNode myNode;

	ItemView(Context context, ZLTreeNode node) {
		super(context);

		myNode = node;

		final ImageView imageView = new ImageView(context);
		String iconName = node.pixmapName();
		Drawable icon = (Drawable)ourIconMap.get(iconName);
		if (icon == null) {
			try {
				int resourceId = R.drawable.class.getField("filetree__" + iconName).getInt(null);
				icon = context.getResources().getDrawable(resourceId);
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {
			}
		}
		if (icon != null) {
			imageView.setImageDrawable(icon);
		}
		imageView.setPadding(0, 2, 5, 0);
		addView(imageView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		final TextView textView = new TextView(context);
		textView.setPadding(0, 2, 0, 0);
		textView.setTextSize(18);
		textView.setText(node.displayName());
		addView(textView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	ZLTreeNode getNode() {
		return myNode;
	}
}
