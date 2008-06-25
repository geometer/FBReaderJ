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

package org.geometerplus.zlibrary.text.view;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.view.*;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.text.model.ZLTextModel;

public abstract class ZLTextView extends ZLView {
	public interface ScrollingMode {
		int NO_OVERLAPPING = 0;
		int KEEP_LINES = 1;
		int SCROLL_LINES = 2;
		int SCROLL_PERCENTAGE = 3;
	};

	public ZLTextView(ZLApplication application, ZLPaintContext context) {
		super(application, context);
	}

	public final void setModel(ZLTextModel model) {
		final ArrayList list = new ArrayList(1);
		list.add(model);
		setModels(list, 0);
	}
	
	public abstract void setModelIndex(int modelIndex);
	
	public abstract void setModels(ArrayList/*<ZLTextModel>*/ model, int current);

	public abstract void scrollPage(boolean forward, int scrollingMode, int value);

	public abstract void search(String text, boolean ignoreCase, boolean wholeText, boolean backward, boolean thisSectionOnly);
	
	public abstract boolean canFindNext();
	public abstract void findNext();
	public abstract boolean canFindPrevious();
	public abstract void findPrevious();

	public abstract int getLeftMargin();
	public abstract int getRightMargin();
	public abstract int getTopMargin();
	public abstract int getBottomMargin();

	public abstract ZLTextIndicatorInfo getIndicatorInfo();
}
