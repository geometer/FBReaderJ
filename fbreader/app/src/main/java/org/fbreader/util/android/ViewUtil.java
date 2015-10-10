/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.fbreader.util.android;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewUtil {
	private static final Object NULL_VIEW = new Object();

	public static View findView(View container, int id) {
		Object view = container.getTag(id);
		if (view == null) {
			view = container.findViewById(id);
			container.setTag(id, view != null ? view : NULL_VIEW);
		}
		return view != NULL_VIEW ? (View)view : null;
	}

	public static TextView findTextView(View container, int id) {
		return (TextView)findView(container, id);
	}

	public static ImageView findImageView(View container, int id) {
		return (ImageView)findView(container, id);
	}

	public static void setSubviewText(View view, int resourceId, String text) {
		final TextView textView = findTextView(view, resourceId);
		if (textView != null) {
			textView.setText(text);
		}
	}
}
