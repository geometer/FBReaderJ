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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.view.impl.ZLTextWordCursor;

import org.geometerplus.fbreader.bookmodel.ContentsModel;

class ContentsView extends FBView {
	ContentsView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public boolean onStylusRelease(int x, int y) {
		if (super.onStylusRelease(x, y)) {
			return false;
		}

		final int index = getParagraphIndexByCoordinate(y);
		if (index == -1) {
			return false;
		}

		final ContentsModel contentsModel = (ContentsModel)getModel();
		final ZLTextTreeParagraph paragraph = contentsModel.getTreeParagraph(index);
		final ContentsModel.Reference reference = contentsModel.getReference(paragraph);

		final FBReader fbreader = (FBReader)Application;
	//	fbreader.BookTextView.gotoPosition(reference, 0, 0);
		fbreader.BookTextView.gotoParagraphSafe(reference.Model, reference.ParagraphIndex);
		fbreader.setMode(FBReader.ViewMode.BOOK_TEXT);

		return true;
	}

	boolean isEmpty() {
		final ContentsModel contentsModel = (ContentsModel)getModel();
		return (contentsModel == null) || (contentsModel.getParagraphsNumber() == 0);
	}

	int currentTextViewParagraph(boolean includeStart) {
		final FBReader fbreader = (FBReader)Application;
		final ZLTextWordCursor cursor = fbreader.BookTextView.StartCursor;
		if (!cursor.isNull()) {
			int reference = cursor.getParagraphCursor().Index;
			boolean startOfParagraph = cursor.getWordIndex() == 0;
			if (cursor.isEndOfParagraph()) {
				++reference;
				startOfParagraph = true;
			}
			final int length = getModel().getParagraphsNumber();
			final ContentsModel contentsModel = (ContentsModel)getModel();
			final ZLTextModel currentModel = (ZLTextModel) fbreader.BookTextView.getModel();
			for (int i = 1; i < length; ++i) {
				final ContentsModel.Reference contentsReference =
					contentsModel.getReference(contentsModel.getTreeParagraph(i));
				if ((contentsReference.Model == currentModel)
						&& ((contentsReference.ParagraphIndex > reference) ||
						(!includeStart && startOfParagraph
							 && (contentsReference.ParagraphIndex == reference)))) {
					return i - 1;
				}
			}
			return length - 1;
		}
		return -1;
	}

	void gotoReference() {
		getModel().removeAllMarks();
		final int selected = currentTextViewParagraph(true);
		highlightParagraph(selected);
		gotoParagraph(selected, false);
		scrollPage(false, ScrollingMode.SCROLL_PERCENTAGE, 40);
	}
}
