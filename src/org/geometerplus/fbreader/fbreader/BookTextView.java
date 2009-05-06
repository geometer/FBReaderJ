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

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.impl.*;

public class BookTextView extends FBView {
	BookTextView(ZLPaintContext context) {
		super(context);
	}
	
	void scrollToHome() {
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphCursor().Index == 0) {
			return;
		}
		final ZLTextPosition position = new ZLTextPosition(cursor);
		gotoParagraph(0, false);
		gotoPosition(new ZLTextPosition(0, 0, 0));
		preparePaintInfo();
		ZLApplication.Instance().repaintView();
	}

	public void gotoParagraphSafe(int paragraphIndex) {
		preparePaintInfo();
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull()) {
			final ZLTextPosition position = new ZLTextPosition(cursor);
			gotoParagraph(paragraphIndex, false);
			preparePaintInfo();
		}
	}
	
	public boolean onStylusPress(int x, int y) {
		ZLTextElementArea area = getElementByCoordinates(x, y);
		if (area != null) {
			ZLTextElement element = area.Element;
			if ((element instanceof ZLTextImageElement) ||
					(element instanceof ZLTextWord)) {
				final ZLTextWordCursor cursor = new ZLTextWordCursor(getStartCursor());
				cursor.moveToParagraph(area.ParagraphIndex);
				cursor.moveToParagraphStart();
				final int elementIndex = area.TextElementIndex;
				byte hyperlinkKind = FBTextKind.REGULAR;
				String id = null;
				for (int i = 0; i < elementIndex; ++i) {
					ZLTextElement e = cursor.getElement();
					if (e instanceof ZLTextControlElement) {
						if (e instanceof ZLTextHyperlinkControlElement) {
							final ZLTextHyperlinkControlElement control = (ZLTextHyperlinkControlElement)e;
							hyperlinkKind = control.Kind;
							id = control.Label;
						} else {
							final ZLTextControlElement control = (ZLTextControlElement)e;
							if (!control.IsStart && (control.Kind == hyperlinkKind)) {
								hyperlinkKind = FBTextKind.REGULAR;
								id = null;
							}
						}
					}
					cursor.nextWord();
				}
				if (id != null) {
					switch (hyperlinkKind) {
						case FBTextKind.EXTERNAL_HYPERLINK:
							ZLibrary.Instance().openInBrowser(id);
							return true;
						case FBTextKind.FOOTNOTE:
						case FBTextKind.INTERNAL_HYPERLINK:
							((FBReader)ZLApplication.Instance()).tryOpenFootnote(id);
							return true;
					}
				}
			}
		}

		return super.onStylusPress(x, y);
	}
}
