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

package org.geometerplus.zlibrary.text.view;

public abstract class ZLTextTraverser {
	private final ZLTextView myView;

	protected ZLTextTraverser(ZLTextView view) {
		myView = view;
	}

	protected abstract void processWord(ZLTextWord word);
	protected abstract void processControlElement(ZLTextControlElement control);
	protected abstract void processSpace();
	protected abstract void processNbSpace();
	protected abstract void processEndOfParagraph();

	public void traverse(ZLTextPosition from, ZLTextPosition to) {
		final int fromParagraph = from.getParagraphIndex();
		final int toParagraph = to.getParagraphIndex();
		ZLTextParagraphCursor cursor = myView.cursor(fromParagraph);
		for (int i = fromParagraph; i <= toParagraph; ++i) {
			final int fromElement = i == fromParagraph ? from.getElementIndex() : 0;
			final int toElement = i == toParagraph ? to.getElementIndex() : cursor.getParagraphLength() - 1;

			for (int j = fromElement; j <= toElement; j++) {
				final ZLTextElement element = cursor.getElement(j);
				if (element == ZLTextElement.HSpace) {
					processSpace();
				} else if (element == ZLTextElement.NBSpace) {
					processNbSpace();
				} else if (element instanceof ZLTextWord) {
					processWord((ZLTextWord)element);
				}
			}
			if (i < toParagraph) {
				processEndOfParagraph();
				cursor = cursor.next();
			}
		}
	}
}
