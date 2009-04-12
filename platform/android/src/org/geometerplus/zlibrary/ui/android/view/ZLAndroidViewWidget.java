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

package org.geometerplus.zlibrary.ui.android.view;

import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

public class ZLAndroidViewWidget extends ZLViewWidget {
	public ZLAndroidViewWidget(int initialAngle) {
		super(initialAngle);

		final ZLAndroidWidget widget = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		widget.setViewWidget(this);
	}

	protected void scrollTo(int viewPage, int shift) {
		final ZLAndroidWidget widget = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		widget.setViewWidget(this);
		widget.scrollToPage(viewPage, shift);
	}

	protected void startAutoScrolling(int viewPage) {
		final ZLAndroidWidget widget = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		widget.setViewWidget(this);
		widget.startAutoScrolling(viewPage);
	}

	public void repaint() {
		final ZLAndroidWidget widget = 
			((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		widget.setViewWidget(this);
		// I'm not sure about threads, so postInvalidate() is used instead of invalidate()
		widget.postInvalidate();
	}
}
