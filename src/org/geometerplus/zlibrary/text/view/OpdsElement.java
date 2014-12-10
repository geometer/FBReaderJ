/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.view;

import java.util.List;

import org.geometerplus.zlibrary.core.application.ZLApplication;

public final class OpdsElement extends ZLTextElement {
	public final String Url;
	private final ZLTextParagraphCursor myCursor;
	private final List<ZLTextElement> myContainer;

	OpdsElement(String url, ZLTextParagraphCursor cursor, List<ZLTextElement> container) {
		Url = url;
		myCursor = cursor;
		myContainer = container;
		new Thread() {
			public void run() {
				try {
					sleep(2000);
				} catch (InterruptedException e) {
				}
				synchronized (myContainer) {
					final int index = myContainer.indexOf(OpdsElement.this);
					if (index != -1) {
						myContainer.remove(index);
						myContainer.add(index, new BookElement(null, null));
						myContainer.add(index, new BookElement(null, null));
						myContainer.add(index, new BookElement(null, null));
						myCursor.View.clearCaches(false);
						ZLApplication.Instance().getViewWidget().repaint();
					}
				}
			}
		}.start();
	}
}
