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

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

import org.geometerplus.zlibrary.text.model.ZLTextMetrics;

import org.geometerplus.zlibrary.text.view.style.*;

abstract class ZLTextViewBase extends ZLView {
	public static enum ImageFitting {
		none, covers, all
	}

	private ZLTextStyle myTextStyle;
	private int myWordHeight = -1;
	private ZLTextMetrics myMetrics;

	ZLTextViewBase(ZLApplication application) {
		super(application);
	}

	private int myMaxSelectionDistance = 0;
	protected final int maxSelectionDistance() {
		if (myMaxSelectionDistance == 0) {
			myMaxSelectionDistance = ZLibrary.Instance().getDisplayDPI() / 20;
		}
		return myMaxSelectionDistance;
	}

	protected void resetMetrics() {
		myMetrics = null;
	}

	protected ZLTextMetrics metrics() {
		// this local variable is used to guarantee null will not
		// be returned from this method enen in multi-thread environment
		ZLTextMetrics m = myMetrics;
		if (m == null) {
			m = new ZLTextMetrics(
				ZLibrary.Instance().getDisplayDPI(),
				// TODO: screen area width
				100,
				// TODO: screen area height
				100,
				getTextStyleCollection().getBaseStyle().getFontSize()
			);
			myMetrics = m;
		}
		return m;
	}

	final int getWordHeight() {
		if (myWordHeight == -1) {
			final ZLTextStyle textStyle = myTextStyle;
			myWordHeight = getContext().getStringHeight() * textStyle.getLineSpacePercent() / 100 + textStyle.getVerticalAlign(metrics());
		}
		return myWordHeight;
	}

	public abstract ZLTextStyleCollection getTextStyleCollection();

	public abstract ImageFitting getImageFitting();

	public abstract int getLeftMargin();
	public abstract int getRightMargin();
	public abstract int getTopMargin();
	public abstract int getBottomMargin();
	public abstract int getSpaceBetweenColumns();

	public abstract boolean twoColumnView();

	public abstract ZLFile getWallpaperFile();
	public abstract ZLPaintContext.FillMode getFillMode();
	public abstract ZLColor getBackgroundColor();
	public abstract ZLColor getSelectionBackgroundColor();
	public abstract ZLColor getSelectionForegroundColor();
	public abstract ZLColor getHighlightingBackgroundColor();
	public abstract ZLColor getHighlightingForegroundColor();
	public abstract ZLColor getTextColor(ZLTextHyperlink hyperlink);

	ZLPaintContext.Size getTextAreaSize() {
		return new ZLPaintContext.Size(getTextColumnWidth(), getTextAreaHeight());
	}

	int getTextAreaHeight() {
		return getContextHeight() - getTopMargin() - getBottomMargin();
	}

	protected int getColumnIndex(int x) {
		if (!twoColumnView()) {
			return -1;
		}
		return 2 * x <= getContextWidth() + getLeftMargin() - getRightMargin() ? 0 : 1;
	}

	public int getTextColumnWidth() {
		return twoColumnView()
			? (getContextWidth() - getLeftMargin() - getSpaceBetweenColumns() - getRightMargin()) / 2
			: getContextWidth() - getLeftMargin() - getRightMargin();
	}

	final ZLTextStyle getTextStyle() {
		if (myTextStyle == null) {
			resetTextStyle();
		}
		return myTextStyle;
	}

	final void setTextStyle(ZLTextStyle style) {
		if (myTextStyle != style) {
			myTextStyle = style;
			myWordHeight = -1;
		}
		getContext().setFont(style.getFontEntries(), style.getFontSize(metrics()), style.isBold(), style.isItalic(), style.isUnderline(), style.isStrikeThrough());
	}

	final void resetTextStyle() {
		setTextStyle(getTextStyleCollection().getBaseStyle());
	}

	boolean isStyleChangeElement(ZLTextElement element) {
		return
			element == ZLTextElement.StyleClose ||
			element instanceof ZLTextStyleElement ||
			element instanceof ZLTextControlElement;
	}

	void applyStyleChangeElement(ZLTextElement element) {
		if (element == ZLTextElement.StyleClose) {
			applyStyleClose();
		} else if (element instanceof ZLTextStyleElement) {
			applyStyle((ZLTextStyleElement)element);
		} else if (element instanceof ZLTextControlElement) {
			applyControl((ZLTextControlElement)element);
		}
	}

	void applyStyleChanges(ZLTextParagraphCursor cursor, int index, int end) {
		for (; index != end; ++index) {
			applyStyleChangeElement(cursor.getElement(index));
		}
	}

	private void applyControl(ZLTextControlElement control) {
		if (control.IsStart) {
			final ZLTextHyperlink hyperlink = control instanceof ZLTextHyperlinkControlElement
				? ((ZLTextHyperlinkControlElement)control).Hyperlink : null;
			final ZLTextNGStyleDescription description =
				getTextStyleCollection().getDescription(control.Kind);
			if (description != null) {
				setTextStyle(new ZLTextNGStyle(myTextStyle, description, hyperlink));
			}
		} else {
			setTextStyle(myTextStyle.Parent);
		}
	}

	private void applyStyle(ZLTextStyleElement element) {
		setTextStyle(new ZLTextExplicitlyDecoratedStyle(myTextStyle, element.Entry));
	}

	private void applyStyleClose() {
		setTextStyle(myTextStyle.Parent);
	}

	protected final ZLPaintContext.ScalingType getScalingType(ZLTextImageElement imageElement) {
		switch (getImageFitting()) {
			default:
			case none:
				return ZLPaintContext.ScalingType.IntegerCoefficient;
			case covers:
				return imageElement.IsCover
					? ZLPaintContext.ScalingType.FitMaximum
					: ZLPaintContext.ScalingType.IntegerCoefficient;
			case all:
				return ZLPaintContext.ScalingType.FitMaximum;
		}
	}

	final int getElementWidth(ZLTextElement element, int charIndex) {
		if (element instanceof ZLTextWord) {
			return getWordWidth((ZLTextWord)element, charIndex);
		} else if (element instanceof ZLTextImageElement) {
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;
			final ZLPaintContext.Size size = getContext().imageSize(
				imageElement.ImageData,
				getTextAreaSize(),
				getScalingType(imageElement)
			);
			return size != null ? size.Width : 0;
		} else if (element instanceof ZLTextVideoElement) {
			return Math.min(300, getTextColumnWidth());
		} else if (element instanceof ExtensionElement) {
			return ((ExtensionElement)element).getWidth();
		} else if (element == ZLTextElement.NBSpace) {
			return getContext().getSpaceWidth();
		} else if (element == ZLTextElement.Indent) {
			return myTextStyle.getFirstLineIndent(metrics());
		} else if (element instanceof ZLTextFixedHSpaceElement) {
			return getContext().getSpaceWidth() * ((ZLTextFixedHSpaceElement)element).Length;
		}
		return 0;
	}

	final int getElementHeight(ZLTextElement element) {
		if (element == ZLTextElement.NBSpace ||
			element instanceof ZLTextWord ||
			element instanceof ZLTextFixedHSpaceElement) {
			return getWordHeight();
		} else if (element instanceof ZLTextImageElement) {
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;
			final ZLPaintContext.Size size = getContext().imageSize(
				imageElement.ImageData,
				getTextAreaSize(),
				getScalingType(imageElement)
			);
			return (size != null ? size.Height : 0) +
				Math.max(getContext().getStringHeight() * (myTextStyle.getLineSpacePercent() - 100) / 100, 3);
		} else if (element instanceof ZLTextVideoElement) {
			return Math.min(Math.min(200, getTextAreaHeight()), getTextColumnWidth() * 2 / 3);
		} else if (element instanceof ExtensionElement) {
			return ((ExtensionElement)element).getHeight();
		}
		return 0;
	}

	final int getElementDescent(ZLTextElement element) {
		return element instanceof ZLTextWord ? getContext().getDescent() : 0;
	}

	final int getWordWidth(ZLTextWord word, int start) {
		return
			start == 0 ?
				word.getWidth(getContext()) :
				getContext().getStringWidth(word.Data, word.Offset + start, word.Length - start);
	}

	final int getWordWidth(ZLTextWord word, int start, int length) {
		return getContext().getStringWidth(word.Data, word.Offset + start, length);
	}

	private char[] myWordPartArray = new char[20];

	final int getWordWidth(ZLTextWord word, int start, int length, boolean addHyphenationSign) {
		if (length == -1) {
			if (start == 0) {
				return word.getWidth(getContext());
			}
			length = word.Length - start;
		}
		if (!addHyphenationSign) {
			return getContext().getStringWidth(word.Data, word.Offset + start, length);
		}
		char[] part = myWordPartArray;
		if (length + 1 > part.length) {
			part = new char[length + 1];
			myWordPartArray = part;
		}
		System.arraycopy(word.Data, word.Offset + start, part, 0, length);
		part[length] = '-';
		return getContext().getStringWidth(part, 0, length + 1);
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
		final ZLPaintContext context = getContext();
		if (start == 0 && length == -1) {
			drawString(context, x, y, word.Data, word.Offset, word.Length, word.getMark(), color, 0);
		} else {
			if (length == -1) {
				length = word.Length - start;
			}
			if (!addHyphenationSign) {
				drawString(context, x, y, word.Data, word.Offset + start, length, word.getMark(), color, start);
			} else {
				char[] part = myWordPartArray;
				if (length + 1 > part.length) {
					part = new char[length + 1];
					myWordPartArray = part;
				}
				System.arraycopy(word.Data, word.Offset + start, part, 0, length);
				part[length] = '-';
				drawString(context, x, y, part, 0, length + 1, word.getMark(), color, start);
			}
		}
	}

	private final void drawString(ZLPaintContext context, int x, int y, char[] str, int offset, int length, ZLTextWord.Mark mark, ZLColor color, int shift) {
		if (mark == null) {
			context.setTextColor(color);
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
					context.setTextColor(color);
					context.drawString(x, y, str, offset + pos, endPos - pos);
					x += context.getStringWidth(str, offset + pos, endPos - pos);
				}

				if (markStart < length) {
					context.setFillColor(getHighlightingBackgroundColor());
					int endPos = Math.min(markStart + markLen, length);
					final int endX = x + context.getStringWidth(str, offset + markStart, endPos - markStart);
					context.fillRectangle(x, y - context.getStringHeight(), endX - 1, y + context.getDescent());
					context.setTextColor(getHighlightingForegroundColor());
					context.drawString(x, y, str, offset + markStart, endPos - markStart);
					x = endX;
				}
				pos = markStart + markLen;
			}

			if (pos < length) {
				context.setTextColor(color);
				context.drawString(x, y, str, offset + pos, length - pos);
			}
		}
	}
}
