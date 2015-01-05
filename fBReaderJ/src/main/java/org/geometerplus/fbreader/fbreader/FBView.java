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

package org.geometerplus.fbreader.fbreader;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.options.*;

public final class FBView extends ZLTextView {
	private final FBReaderApp myReader;
	private final ViewOptions myViewOptions;
	private final BookElementManager myBookElementManager;

	FBView(FBReaderApp reader) {
		super(reader);
		myReader = reader;
		myViewOptions = reader.ViewOptions;
		myBookElementManager = new BookElementManager(this);
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
		if (myFooter != null) {
			myFooter.resetTOCMarks();
		}
	}

	private int myStartY;
	private boolean myIsBrightnessAdjustmentInProgress;
	private int myStartBrightness;

	private TapZoneMap myZoneMap;

	private TapZoneMap getZoneMap() {
		final PageTurningOptions prefs = myReader.PageTurningOptions;
		String id = prefs.TapZoneMap.getValue();
		if ("".equals(id)) {
			id = prefs.Horizontal.getValue() ? "right_to_left" : "up";
		}
		if (myZoneMap == null || !id.equals(myZoneMap.Name)) {
			myZoneMap = TapZoneMap.zoneMap(id);
		}
		return myZoneMap;
	}

	public boolean onFingerSingleTap(int x, int y) {
		if (super.onFingerSingleTap(x, y)) {
			return true;
		}

		final ZLTextRegion hyperlinkRegion = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.HyperlinkFilter);
		if (hyperlinkRegion != null) {
			selectRegion(hyperlinkRegion);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			myReader.runAction(ActionCode.PROCESS_HYPERLINK);
			return true;
		}

		final ZLTextRegion bookRegion = findRegion(x, y, 0, ZLTextRegion.ExtensionFilter);
		if (bookRegion != null) {
			myReader.runAction(ActionCode.DISPLAY_BOOK_POPUP, bookRegion);
			return true;
		}

		final ZLTextRegion videoRegion = findRegion(x, y, 0, ZLTextRegion.VideoFilter);
		if (videoRegion != null) {
			selectRegion(videoRegion);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			myReader.runAction(ActionCode.OPEN_VIDEO, (ZLTextVideoRegionSoul)videoRegion.getSoul());
			return true;
		}

		final ZLTextHighlighting highlighting = findHighlighting(x, y, MAX_SELECTION_DISTANCE);
		if (highlighting instanceof BookmarkHighlighting) {
			myReader.runAction(
				ActionCode.SELECTION_BOOKMARK,
				((BookmarkHighlighting)highlighting).Bookmark
			);
			return true;
		}

		myReader.runAction(getZoneMap().getActionByCoordinates(
			x, y, getContextWidth(), getContextHeight(),
			isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap
		), x, y);

		return true;
	}

	@Override
	public boolean isDoubleTapSupported() {
		return myReader.MiscOptions.EnableDoubleTap.getValue();
	}

	@Override
	public boolean onFingerDoubleTap(int x, int y) {
		if (super.onFingerDoubleTap(x, y)) {
			return true;
		}
		myReader.runAction(getZoneMap().getActionByCoordinates(
			x, y, getContextWidth(), getContextHeight(), TapZoneMap.Tap.doubleTap
		), x, y);
		return true;
	}

	public boolean onFingerPress(int x, int y) {
		if (super.onFingerPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = findSelectionCursor(x, y, MAX_SELECTION_DISTANCE);
		if (cursor != ZLTextSelectionCursor.None) {
			myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		if (myReader.MiscOptions.AllowScreenBrightnessAdjustment.getValue() && x < getContextWidth() / 10) {
			myIsBrightnessAdjustmentInProgress = true;
			myStartY = y;
			myStartBrightness = ZLibrary.Instance().getScreenBrightness();
			return true;
		}

		startManualScrolling(x, y);
		return true;
	}

	private boolean isFlickScrollingEnabled() {
		final PageTurningOptions.FingerScrollingType fingerScrolling =
			myReader.PageTurningOptions.FingerScrolling.getValue();
		return
			fingerScrolling == PageTurningOptions.FingerScrollingType.byFlick ||
			fingerScrolling == PageTurningOptions.FingerScrollingType.byTapAndFlick;
	}

	private void startManualScrolling(int x, int y) {
		if (!isFlickScrollingEnabled()) {
			return;
		}

		final boolean horizontal = myReader.PageTurningOptions.Horizontal.getValue();
		final Direction direction = horizontal ? Direction.rightToLeft : Direction.up;
		myReader.getViewWidget().startManualScrolling(x, y, direction);
	}

	public boolean onFingerMove(int x, int y) {
		if (super.onFingerMove(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		synchronized (this) {
			if (myIsBrightnessAdjustmentInProgress) {
				if (x >= getContextWidth() / 5) {
					myIsBrightnessAdjustmentInProgress = false;
					startManualScrolling(x, y);
				} else {
					final int delta = (myStartBrightness + 30) * (myStartY - y) / getContextHeight();
					ZLibrary.Instance().setScreenBrightness(myStartBrightness + delta);
					return true;
				}
			}

			if (isFlickScrollingEnabled()) {
				myReader.getViewWidget().scrollManuallyTo(x, y);
			}
		}
		return true;
	}

	public boolean onFingerRelease(int x, int y) {
		if (super.onFingerRelease(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			releaseSelectionCursor();
			return true;
		}

		if (myIsBrightnessAdjustmentInProgress) {
			myIsBrightnessAdjustmentInProgress = false;
			return true;
		}

		if (isFlickScrollingEnabled()) {
			myReader.getViewWidget().startAnimatedScrolling(
				x, y, myReader.PageTurningOptions.AnimationSpeed.getValue()
			);
			return true;
		}

		return true;
	}

	public boolean onFingerLongPress(int x, int y) {
		if (super.onFingerLongPress(x, y)) {
			return true;
		}

		final ZLTextRegion region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();
			boolean doSelectRegion = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				switch (myReader.MiscOptions.WordTappingAction.getValue()) {
					case startSelecting:
						myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
						initSelection(x, y);
						final ZLTextSelectionCursor cursor = findSelectionCursor(x, y);
						if (cursor != ZLTextSelectionCursor.None) {
							moveSelectionCursorTo(cursor, x, y);
						}
						return true;
					case selectSingleWord:
					case openDictionary:
						doSelectRegion = true;
						break;
				}
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doSelectRegion =
					myReader.ImageOptions.TapAction.getValue() !=
					ImageOptions.TapActionEnum.doNothing;
			} else if (soul instanceof ZLTextHyperlinkRegionSoul) {
				doSelectRegion = true;
			}

			if (doSelectRegion) {
				selectRegion(region);
				myReader.getViewWidget().reset();
				myReader.getViewWidget().repaint();
				return true;
			}
		}

		return false;
	}

	public boolean onFingerMoveAfterLongPress(int x, int y) {
		if (super.onFingerMoveAfterLongPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			moveSelectionCursorTo(cursor, x, y);
			return true;
		}

		ZLTextRegion region = getSelectedRegion();
		if (region != null) {
			ZLTextRegion.Soul soul = region.getSoul();
			if (soul instanceof ZLTextHyperlinkRegionSoul ||
				soul instanceof ZLTextWordRegionSoul) {
				if (myReader.MiscOptions.WordTappingAction.getValue() !=
					MiscOptions.WordTappingActionEnum.doNothing) {
					region = findRegion(x, y, MAX_SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
					if (region != null) {
						soul = region.getSoul();
						if (soul instanceof ZLTextHyperlinkRegionSoul
							 || soul instanceof ZLTextWordRegionSoul) {
							selectRegion(region);
							myReader.getViewWidget().reset();
							myReader.getViewWidget().repaint();
						}
					}
				}
			}
		}
		return true;
	}

	public boolean onFingerReleaseAfterLongPress(int x, int y) {
		if (super.onFingerReleaseAfterLongPress(x, y)) {
			return true;
		}

		final ZLTextSelectionCursor cursor = getSelectionCursorInMovement();
		if (cursor != ZLTextSelectionCursor.None) {
			releaseSelectionCursor();
			return true;
		}

		final ZLTextRegion region = getSelectedRegion();
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();

			boolean doRunAction = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				doRunAction =
					myReader.MiscOptions.WordTappingAction.getValue() ==
					MiscOptions.WordTappingActionEnum.openDictionary;
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doRunAction =
					myReader.ImageOptions.TapAction.getValue() ==
					ImageOptions.TapActionEnum.openImageView;
			}

			if (doRunAction) {
				myReader.runAction(ActionCode.PROCESS_HYPERLINK);
				return true;
			}
		}

		return false;
	}

	public boolean onTrackballRotated(int diffX, int diffY) {
		if (diffX == 0 && diffY == 0) {
			return true;
		}

		final Direction direction = (diffY != 0) ?
			(diffY > 0 ? Direction.down : Direction.up) :
			(diffX > 0 ? Direction.leftToRight : Direction.rightToLeft);

		new MoveCursorAction(myReader, direction).run();
		return true;
	}

	@Override
	public ZLTextStyleCollection getTextStyleCollection() {
		return myViewOptions.getTextStyleCollection();
	}

	@Override
	public ImageFitting getImageFitting() {
		return myReader.ImageOptions.FitToScreen.getValue();
	}

	@Override
	public int getLeftMargin() {
		return myViewOptions.LeftMargin.getValue();
	}

	@Override
	public int getRightMargin() {
		return myViewOptions.RightMargin.getValue();
	}

	@Override
	public int getTopMargin() {
		return myViewOptions.TopMargin.getValue();
	}

	@Override
	public int getBottomMargin() {
		return myViewOptions.BottomMargin.getValue();
	}

	@Override
	public int getSpaceBetweenColumns() {
		return myViewOptions.SpaceBetweenColumns.getValue();
	}

	@Override
	public boolean twoColumnView() {
		return getContextHeight() <= getContextWidth() && myViewOptions.TwoColumnView.getValue();
	}

	@Override
	public ZLFile getWallpaperFile() {
		final String filePath = myViewOptions.getColorProfile().WallpaperOption.getValue();
		if ("".equals(filePath)) {
			return null;
		}

		final ZLFile file = ZLFile.createFileByPath(filePath);
		if (file == null || !file.exists()) {
			return null;
		}
		return file;
	}

	@Override
	public ZLPaintContext.FillMode getFillMode() {
		return getWallpaperFile() instanceof ZLResourceFile
			? ZLPaintContext.FillMode.tileMirror
			: myViewOptions.getColorProfile().FillModeOption.getValue();
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myViewOptions.getColorProfile().BackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectionBackgroundColor() {
		return myViewOptions.getColorProfile().SelectionBackgroundOption.getValue();
	}

	@Override
	public ZLColor getSelectionForegroundColor() {
		return myViewOptions.getColorProfile().SelectionForegroundOption.getValue();
	}

	@Override
	public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
		final ColorProfile profile = myViewOptions.getColorProfile();
		switch (hyperlink.Type) {
			default:
			case FBHyperlinkType.NONE:
				return profile.RegularTextOption.getValue();
			case FBHyperlinkType.INTERNAL:
				return myReader.Collection.isHyperlinkVisited(myReader.getCurrentBook(), hyperlink.Id)
					? profile.VisitedHyperlinkTextOption.getValue()
					: profile.HyperlinkTextOption.getValue();
			case FBHyperlinkType.EXTERNAL:
				return profile.HyperlinkTextOption.getValue();
		}
	}

	@Override
	public ZLColor getHighlightingBackgroundColor() {
		return myViewOptions.getColorProfile().HighlightingBackgroundOption.getValue();
	}

	@Override
	public ZLColor getHighlightingForegroundColor() {
		return myViewOptions.getColorProfile().HighlightingForegroundOption.getValue();
	}

	private abstract class Footer implements FooterArea {
		private Runnable UpdateTask = new Runnable() {
			public void run() {
				myReader.getViewWidget().repaint();
			}
		};

		protected ArrayList<TOCTree> myTOCMarks;

		public int getHeight() {
			return myViewOptions.FooterHeight.getValue();
		}

		public synchronized void resetTOCMarks() {
			myTOCMarks = null;
		}

		private final int MAX_TOC_MARKS_NUMBER = 100;
		protected synchronized void updateTOCMarks(BookModel model) {
			myTOCMarks = new ArrayList<TOCTree>();
			TOCTree toc = model.TOCTree;
			if (toc == null) {
				return;
			}
			int maxLevel = Integer.MAX_VALUE;
			if (toc.getSize() >= MAX_TOC_MARKS_NUMBER) {
				final int[] sizes = new int[10];
				for (TOCTree tocItem : toc) {
					if (tocItem.Level < 10) {
						++sizes[tocItem.Level];
					}
				}
				for (int i = 1; i < sizes.length; ++i) {
					sizes[i] += sizes[i - 1];
				}
				for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
					if (sizes[maxLevel] < MAX_TOC_MARKS_NUMBER) {
						break;
					}
				}
			}
			for (TOCTree tocItem : toc.allSubtrees(maxLevel)) {
				myTOCMarks.add(tocItem);
			}
		}

		protected String buildInfoString(PagePosition pagePosition, String separator) {
			final StringBuilder info = new StringBuilder();
			final FooterOptions footerOptions = myViewOptions.getFooterOptions();
			if (footerOptions.ShowProgress.getValue()) {
				info.append(pagePosition.Current);
				info.append("/");
				info.append(pagePosition.Total);
			}
			if (footerOptions.ShowClock.getValue()) {
				if (info.length() > 0) {
					info.append(separator);
				}
				info.append(ZLibrary.Instance().getCurrentTimeString());
			}
			if (footerOptions.ShowBattery.getValue()) {
				if (info.length() > 0) {
					info.append(separator);
				}
				info.append(myReader.getBatteryLevel());
				info.append("%");
			}
			return info.toString();
		}

		private List<FontEntry> myFontEntry;
		private Map<String,Integer> myHeightMap = new HashMap<String,Integer>();
		private Map<String,Integer> myCharHeightMap = new HashMap<String,Integer>();
		protected synchronized int setFont(ZLPaintContext context, int height, boolean bold) {
			final String family = myViewOptions.getFooterOptions().Font.getValue();
			if (myFontEntry == null || !family.equals(myFontEntry.get(0).Family)) {
				myFontEntry = Collections.singletonList(FontEntry.systemEntry(family));
			}
			final String key = family + (bold ? "N" : "B") + height;
			final Integer cached = myHeightMap.get(key);
			if (cached != null) {
				context.setFont(myFontEntry, cached, bold, false, false, false);
				final Integer charHeight = myCharHeightMap.get(key);
				return charHeight != null ? charHeight : height;
			} else {
				int h = height + 2;
				int charHeight = height;
				final int max = height < 9 ? height - 1 : height - 2;
				for (; h > 5; --h) {
					context.setFont(myFontEntry, h, bold, false, false, false);
					charHeight = context.getCharHeight('H');
					if (charHeight <= max) {
						break;
					}
				}
				myHeightMap.put(key, h);
				myCharHeightMap.put(key, charHeight);
				return charHeight;
			}
		}
	}

	private class FooterOldStyle extends Footer {
		public synchronized void paint(ZLPaintContext context) {
			final ZLFile wallpaper = getWallpaperFile();
			if (wallpaper != null) {
				context.clear(wallpaper, getFillMode());
			} else {
				context.clear(getBackgroundColor());
			}

			final BookModel model = myReader.Model;
			if (model == null) {
				return;
			}

			//final ZLColor bgColor = getBackgroundColor();
			// TODO: separate color option for footer color
			final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
			final ZLColor fillColor = myViewOptions.getColorProfile().FooterFillOption.getValue();

			final int left = getLeftMargin();
			final int right = context.getWidth() - getRightMargin();
			final int height = getHeight();
			final int lineWidth = height <= 10 ? 1 : 2;
			final int delta = height <= 10 ? 0 : 1;
			setFont(context, height, height > 10);

			final PagePosition pagePosition = FBView.this.pagePosition();

			// draw info text
			final String infoString = buildInfoString(pagePosition, " ");
			final int infoWidth = context.getStringWidth(infoString);
			context.setTextColor(fgColor);
			context.drawString(right - infoWidth, height - delta, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			final int gaugeWidth = gaugeRight - left - 2 * lineWidth;

			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			context.drawLine(left, lineWidth, left, height - lineWidth);
			context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, left, lineWidth);

			final int gaugeInternalRight =
				left + lineWidth + (int)(1.0 * gaugeWidth * pagePosition.Current / pagePosition.Total);

			context.setFillColor(fillColor);
			context.fillRectangle(left + 1, height - 2 * lineWidth, gaugeInternalRight, lineWidth + 1);

			if (myViewOptions.getFooterOptions().ShowTOCMarks.getValue()) {
				if (myTOCMarks == null) {
					updateTOCMarks(model);
				}
				final int fullLength = sizeOfFullText();
				for (TOCTree tocItem : myTOCMarks) {
					TOCTree.Reference reference = tocItem.getReference();
					if (reference != null) {
						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
						final int xCoord =
							left + 2 * lineWidth + (int)(1.0 * gaugeWidth * refCoord / fullLength);
						context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
					}
				}
			}
		}
	}

	private class FooterNewStyle extends Footer {
		public synchronized void paint(ZLPaintContext context) {
			final ColorProfile cProfile = myViewOptions.getColorProfile();
			context.clear(cProfile.FooterNGBackgroundOption.getValue());

			final BookModel model = myReader.Model;
			if (model == null) {
				return;
			}

			final ZLColor textColor = cProfile.FooterNGForegroundOption.getValue();
			final ZLColor readColor = cProfile.FooterNGForegroundOption.getValue();
			final ZLColor unreadColor = cProfile.FooterNGForegroundUnreadOption.getValue();

			final int left = getLeftMargin();
			final int right = context.getWidth() - getRightMargin();
			final int height = getHeight();
			final int lineWidth = height <= 12 ? 1 : 2;
			final int charHeight = setFont(context, height, height > 12);

			final PagePosition pagePosition = FBView.this.pagePosition();

			// draw info text
			final String infoString = buildInfoString(pagePosition, "  ");
			final int infoWidth = context.getStringWidth(infoString);
			context.setTextColor(textColor);
			context.drawString(right - infoWidth, (height + charHeight + 1) / 2, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			final int gaugeInternalRight =
				left + (int)(1.0 * (gaugeRight - left) * pagePosition.Current / pagePosition.Total + 0.5);
			final int v = height / 2;

			context.setLineWidth(lineWidth);
			context.setLineColor(readColor);
			context.drawLine(left, v, gaugeInternalRight, v);
			if (gaugeInternalRight < gaugeRight) {
				context.setLineColor(unreadColor);
				context.drawLine(gaugeInternalRight + 1, v, gaugeRight, v);
			}

			// draw labels
			if (myViewOptions.getFooterOptions().ShowTOCMarks.getValue()) {
				final TreeSet<Integer> labels = new TreeSet<Integer>();
				labels.add(left);
				labels.add(gaugeRight);
				if (myTOCMarks == null) {
					updateTOCMarks(model);
				}
				final int fullLength = sizeOfFullText();
				for (TOCTree tocItem : myTOCMarks) {
					TOCTree.Reference reference = tocItem.getReference();
					if (reference != null) {
						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
						labels.add(left + (int)(1.0 * (gaugeRight - left) * refCoord / fullLength + 0.5));
					}
				}
				for (int l : labels) {
					context.setLineColor(l <= gaugeInternalRight ? readColor : unreadColor);
					context.drawLine(l, v + 3, l, v - lineWidth - 2);
				}
			}
		}
	}

	private Footer myFooter;

	@Override
	public Footer getFooterArea() {
		switch (myViewOptions.ScrollbarType.getValue()) {
			case SCROLLBAR_SHOW_AS_FOOTER:
				if (!(myFooter instanceof FooterNewStyle)) {
					if (myFooter != null) {
						myReader.removeTimerTask(myFooter.UpdateTask);
					}
					myFooter = new FooterNewStyle();
					myReader.addTimerTask(myFooter.UpdateTask, 15000);
				}
				break;
			case SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE:
				if (!(myFooter instanceof FooterOldStyle)) {
					if (myFooter != null) {
						myReader.removeTimerTask(myFooter.UpdateTask);
					}
					myFooter = new FooterOldStyle();
					myReader.addTimerTask(myFooter.UpdateTask, 15000);
				}
				break;
			default:
				if (myFooter != null) {
					myReader.removeTimerTask(myFooter.UpdateTask);
					myFooter = null;
				}
				break;
		}
		return myFooter;
	}

	@Override
	protected void releaseSelectionCursor() {
		super.releaseSelectionCursor();
		if (getCountOfSelectedWords() > 0) {
			myReader.runAction(ActionCode.SELECTION_SHOW_PANEL);
		}
	}

	public String getSelectedText() {
		final TextBuildTraverser traverser = new TextBuildTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getText();
	}

	public int getCountOfSelectedWords() {
		final WordCountTraverser traverser = new WordCountTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getCount();
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;
	public static final int SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE = 4;

	@Override
	public int scrollbarType() {
		return myViewOptions.ScrollbarType.getValue();
	}

	@Override
	public Animation getAnimationType() {
		return myReader.PageTurningOptions.Animation.getValue();
	}

	@Override
	protected ZLPaintContext.ColorAdjustingMode getAdjustingModeForImages() {
		if (myReader.ImageOptions.MatchBackground.getValue()) {
			if (ColorProfile.DAY.equals(myViewOptions.getColorProfile().Name)) {
				return ZLPaintContext.ColorAdjustingMode.DARKEN_TO_BACKGROUND;
			} else {
				return ZLPaintContext.ColorAdjustingMode.LIGHTEN_TO_BACKGROUND;
			}
		} else {
			return ZLPaintContext.ColorAdjustingMode.NONE;
		}
	}

	@Override
	public synchronized void onScrollingFinished(PageIndex pageIndex) {
		super.onScrollingFinished(pageIndex);
		myReader.storePosition();
	}

	@Override
	protected ExtensionElementManager getExtensionManager() {
		return myBookElementManager;
	}
}
