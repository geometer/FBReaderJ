/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleDecoration;

abstract class ZLTextViewBase extends ZLView {
	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;

	ZLTextViewBase(ZLApplication application) {
		super(application);
		resetTextStyle();
	}

	final int getWordHeight() {
		if (myWordHeight == -1) {
			final ZLTextStyle textStyle = myTextStyle;
			myWordHeight = (int)(myContext.getStringHeight() * textStyle.getLineSpacePercent() / 100) + textStyle.getVerticalShift();
		}
		return myWordHeight;
	}

	public abstract int getLeftMargin();
	public abstract int getRightMargin();
	public abstract int getTopMargin();
	public abstract int getBottomMargin();

	public abstract ZLFile getWallpaperFile();
	public abstract ZLColor getBackgroundColor();
	public abstract ZLColor getSelectedBackgroundColor();
	public abstract ZLColor getSelectedForegroundColor();
	public abstract ZLColor getTextColor(ZLTextHyperlink hyperlink);
	public abstract ZLColor getHighlightingColor();

	int getTextAreaHeight() {
		return myContext.getHeight() - getTopMargin() - getBottomMargin();
	}

	int getTextAreaWidth() {
		return myContext.getWidth() - getLeftMargin() - getRightMargin();
	}

	int getBottomLine() {
		return myContext.getHeight() - getBottomMargin() - 1;
	}

	int getRightLine() {
		return myContext.getWidth() - getRightMargin() - 1;
	}

	final ZLTextStyle getTextStyle() {
		return myTextStyle;
	}

	final void setTextStyle(ZLTextStyle style) {
		if (myTextStyle != style) {
			myTextStyle = style;
			myWordHeight = -1;
		}
		myContext.setFont(style.getFontFamily(), style.getFontSize(), style.isBold(), style.isItalic(), style.isUnderline());
	}

	final void resetTextStyle() {
		setTextStyle(ZLTextStyleCollection.Instance().getBaseStyle());
	}

	void applyControl(ZLTextControlElement control) {
		if (control.IsStart) {
			final ZLTextStyleDecoration decoration =
				ZLTextStyleCollection.Instance().getDecoration(control.Kind);
			if (control instanceof ZLTextHyperlinkControlElement) {
				setTextStyle(decoration.createDecoratedStyle(myTextStyle, ((ZLTextHyperlinkControlElement)control).Hyperlink));
			} else {
				setTextStyle(decoration.createDecoratedStyle(myTextStyle));
			}
		} else {
			setTextStyle(myTextStyle.Base);
		}
	}

	void applyControls(ZLTextParagraphCursor cursor, int index, int end) {
		for (; index != end; ++index) {
			final ZLTextElement element = cursor.getElement(index);
			if (element instanceof ZLTextControlElement) {
				applyControl((ZLTextControlElement)element);
			}
		}
	}

	final int getElementWidth(ZLTextElement element, int charIndex) {
		if (element instanceof ZLTextWord) {
			return getWordWidth((ZLTextWord)element, charIndex);
		} else if (element instanceof ZLTextImageElement) {
			return myContext.imageWidth(((ZLTextImageElement)element).ImageData);
		} else if (element == ZLTextElement.IndentElement) {
			return myTextStyle.getFirstLineIndentDelta();
		} else if (element instanceof ZLTextFixedHSpaceElement) {
			return myContext.getSpaceWidth() * ((ZLTextFixedHSpaceElement)element).Length;
		}
		return 0;
	}

	final int getElementHeight(ZLTextElement element) {
		if (element instanceof ZLTextWord) {
			return getWordHeight();
		} else if (element instanceof ZLTextImageElement) {
			return myContext.imageHeight(((ZLTextImageElement)element).ImageData) +
				Math.max(myContext.getStringHeight() * (myTextStyle.getLineSpacePercent() - 100) / 100, 3);
		}
		return 0;
	}

	final int getElementDescent(ZLTextElement element) {
		return element instanceof ZLTextWord ? myContext.getDescent() : 0;
	}

	final int getWordWidth(ZLTextWord word, int start) {
		return
			start == 0 ?
				word.getWidth(myContext) :
				myContext.getStringWidth(word.Data, word.Offset + start, word.Length - start);
	}

	final int getWordWidth(ZLTextWord word, int start, int length) {
		return myContext.getStringWidth(word.Data, word.Offset + start, length);
	}

	private char[] myWordPartArray = new char[20];

	final int getWordWidth(ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		if (length == -1) {
			if (start == 0) {
				return word.getWidth(myContext);
			}
			length = word.Length - start;
		}
		if (!addHyphenationSign) {
			return myContext.getStringWidth(word.Data, word.Offset + start, length);
		}
		char[] part = myWordPartArray;
		if (length + 1 > part.length) {
			part = new char[length + 1];
			myWordPartArray = part;
		}
		System.arraycopy(word.Data, word.Offset + start, part, 0, length);
		part[length] = '-';
		return myContext.getStringWidth(part, 0, length + 1);
	}

	int getAreaLength(ZLTextParagraphCursor paragraph, ZLTextElementArea area, int toCharIndex) {
		setTextStyle(area.Style);
		final ZLTextWord word = (ZLTextWord)paragraph.getElement(area.ElementIndex);
		int length = toCharIndex - area.CharIndex;
		boolean selectHyphenationSign = false;
		if (length >= area.Length) {
			selectHyphenationSign = area.AddHyphenationSign;
			length = area.Length;
		}
		if (length > 0) {
			return getWordWidth(word, area.CharIndex, length, selectHyphenationSign);
		}
		return 0;
	}

	final void drawWord(int x, int y, ZLTextWord word, int start, int length, boolean addHyphenationSign, ZLColor color) {
		final ZLPaintContext context = myContext;
		context.setTextColor(color);
		if (start == 0 && length == -1) {
			drawString(x, y, word.Data, word.Offset, word.Length, word.getMark(), 0);
		} else {
			if (length == -1) {
				length = word.Length - start;
			}
			if (!addHyphenationSign) {
				drawString(x, y, word.Data, word.Offset + start, length, word.getMark(), start);
			} else {
				char[] part = myWordPartArray;
				if (length + 1 > part.length) {
					part = new char[length + 1];
					myWordPartArray = part;
				}
				System.arraycopy(word.Data, word.Offset + start, part, 0, length);
				part[length] = '-';
				drawString(x, y, part, 0, length + 1, word.getMark(), start);
			}
		}
	}

	private final void drawString(int x, int y, char[] str, int offset, int length, ZLTextWord.Mark mark, int shift) {
		final ZLPaintContext context = myContext;
		if (mark == null) {
			context.drawString(x, y, str, offset, length);
		} else {
			int pos = 0;
			for (; (mark != null) && (pos < length); mark = mark.getNext()) {
				int markStart = mark.Start - shift;
				int markLen = mark.Length;

				if (markStart < pos) {
					markLen += markStart - pos;
					markStart = pos;
				}

				if (markLen <= 0) {
					continue;
				}

				if (markStart > pos) {
					int endPos = Math.min(markStart, length);
					context.drawString(x, y, str, offset + pos, endPos - pos);
					x += context.getStringWidth(str, offset + pos, endPos - pos);
				}

				if (markStart < length) {
					context.setFillColor(getHighlightingColor());
					int endPos = Math.min(markStart + markLen, length);
					final int endX = x + context.getStringWidth(str, offset + markStart, endPos - markStart);
					context.fillRectangle(x, y - context.getStringHeight(), endX - 1, y + context.getDescent());
					context.drawString(x, y, str, offset + markStart, endPos - markStart);
					x = endX;
				}
				pos = markStart + markLen;
			}

			if (pos < length) {
				context.drawString(x, y, str, offset + pos, length - pos);
			}
		}
	}
}
