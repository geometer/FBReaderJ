package org.geometerplus.zlibrary.text.view;

public class ZLTextSelection {
	private static final int SELECTION_DISTANCE = 10;

	private ZLTextRegion myInitialRegion;
	private final Bound myLeftBound = new StartBound();
	private final Bound myRightBound = new EndBound();
	private final StringBuilder myText = new StringBuilder();
	private final ZLTextView myView;
	private Scroller myScroller;

	ZLTextSelection(ZLTextView view) {
		myView = view;
		clear();
	}

	boolean isEmpty() {
		return myInitialRegion == null;
	}

	boolean isEmptyOnPage(ZLTextPage page) {
		return getStartAreaID(page) == -1;
	}

	boolean clear() { // returns if it was filled before.
		boolean res = !isEmpty();
		myInitialRegion = null;
		return res;
	}

	boolean start(int x, int y) {
		clear();
		myInitialRegion = findSelectedRegion(x, y);
		if (myInitialRegion == null) {
			return false;
		}

		myLeftBound.set(myInitialRegion);
		myRightBound.set(myInitialRegion);
		return true;
	}

	void stop() {
		if (myScroller != null) {
			myScroller.stop();
			myScroller = null;
		}
	}

	boolean expandTo(int x, int y) {
		if (myInitialRegion == null) {
			return start(x, y);
		}

		if (y < 10) {
			if (myScroller != null && myScroller.scrollsForward()) {
				myScroller.stop();
				myScroller = null;
			}
			if (myScroller == null) {
				myScroller = new Scroller(false, x, y);
				return false;
			}
		} else if (y > myView.getTextAreaHeight() - 10) {
			if (myScroller != null && !myScroller.scrollsForward()) {
				myScroller.stop();
				myScroller = null;
			}
			if (myScroller == null) {
				myScroller = new Scroller(true, x, y);
				return false;
			}
		} else {
			if (myScroller != null) {
				myScroller.stop();
				myScroller = null;
			}
		}

		if (myScroller != null) {
			myScroller.setXY(x, y);
		}

		ZLTextRegion region = findSelectedRegion(x, y);
		if (region == null && myScroller != null) {
			region = findNearestRegion(x, y);
		}
		if (region == null) {
			return false;
		}

		final int cmp = myInitialRegion.compareTo(region);
		boolean changed = false;
		if (cmp < 0) {
			changed |= myRightBound.set(region);
		} else if (cmp > 0) {
			changed |= myLeftBound.set(region);
		} else {
			changed |= myRightBound.set(region);
			changed |= myLeftBound.set(region);
		}
		if (changed) {
			myText.delete(0, myText.length());
		}
		return changed;
	}

	private void prepareParagraphText(int paragraphID) {
		final ZLTextParagraphCursor paragraph = ZLTextParagraphCursor.cursor(myView.getModel(), paragraphID);
		final int startElementID = myLeftBound.getParagraphIndex() == paragraphID ? myLeftBound.getElementIndex() : 0;
		final int endElementID = myRightBound.getParagraphIndex() == paragraphID ? myRightBound.getElementIndex() : paragraph.getParagraphLength() - 1;

		for (int elementID = startElementID; elementID <= endElementID; elementID++) {
			final ZLTextElement element = paragraph.getElement(elementID);
			if (element == ZLTextElement.HSpace) {
				myText.append(" ");
			} else if (element instanceof ZLTextWord) {
				ZLTextWord word = (ZLTextWord)element;
				myText.append(word.Data, word.Offset, word.Length);
			}
		}
	}

	String getText() {
		if (isEmpty()) {
			return "";
		}
		if (myText.length() == 0) {
			final int from = myLeftBound.getParagraphIndex();
			final int to = myRightBound.getParagraphIndex();
			for (int i = from; i < to; ++i) {
				prepareParagraphText(i);
				myText.append("\n");
			}
			prepareParagraphText(to);
		}
		return myText.toString();
	}

	public ZLTextPosition getStartPosition() {
		return myLeftBound;
	}

	private boolean areaWithinSelection(ZLTextElementArea area) {
		return !isEmpty() && myLeftBound.isAreaWithin(area) && myRightBound.isAreaWithin(area);
	}

	public boolean areaWithinStartBound(ZLTextElementArea area) {
		return !isEmpty() && !myRightBound.isExpandedBy(area) && myLeftBound.isAreaWithin(area);
	}

	public boolean areaWithinEndBound(ZLTextElementArea area) {
		return !isEmpty() && !myLeftBound.isExpandedBy(area) && myRightBound.isAreaWithin(area);
	}

	public boolean isAreaSelected(ZLTextElementArea area) {
		return
			!isEmpty()
			&& myLeftBound.myArea.weakCompareTo(area) <= 0
			&& myRightBound.myArea.weakCompareTo(area) >= 0;
	}

	public int getStartAreaID(ZLTextPage page) {
		final int id = page.TextElementMap.indexOf(myLeftBound.myArea);
		if (id == -1) {
			if (areaWithinSelection(page.TextElementMap.get(0))) {
				return 0;
			}
		}
		return id;
	}

	public int getEndAreaID(ZLTextPage page) {
		final int id = page.TextElementMap.indexOf(myRightBound.myArea);
		if (id == -1) {
			final int lastID = page.TextElementMap.size() - 1;
			if (areaWithinSelection(page.TextElementMap.get(lastID))) {
				return lastID;
			}
		}
		return id;
	}

	private ZLTextElementArea getArea(int areaID) {
		if (areaID != -1) {
			return getTextElementMap().get(areaID);
		}
		return null;
	}

	ZLTextElementArea getStartArea() {
		return getArea(getStartAreaID(myView.myCurrentPage));
	}

	ZLTextElementArea getEndArea() {
		return getArea(getEndAreaID(myView.myCurrentPage));
	}

	public int getStartY() {
		final ZLTextElementArea startArea = getStartArea();
		if (startArea != null)
			return startArea.YStart;
		return 0;
	}
	public int getEndY() {
		final ZLTextElementArea endArea = getEndArea();
		if (endArea != null)
			return endArea.YEnd;
		return 0;
	}

	private ZLTextElementAreaVector getTextElementMap() {
		return myView.myCurrentPage.TextElementMap;
	}

	private ZLTextRegion findSelectedRegion(int x, int y) { // TODO fast find
		return myView.findRegion(x, y, SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
	}

	private ZLTextRegion findNearestRegion(int x, int y) { // TODO fast find
		return myView.findRegion(x, y, Integer.MAX_VALUE - 1, ZLTextRegion.AnyRegionFilter);
	}

	private abstract class Bound extends ZLTextPosition {
		protected ZLTextElementArea myArea;

		@Override
		public int getParagraphIndex() {
			return myArea.ParagraphIndex;
		}

		@Override
		public int getElementIndex() {
			return myArea.ElementIndex;
		}

		@Override
		public int getCharIndex() {
			return 0;
		}

		protected boolean set(ZLTextRegion region) {
			final ZLTextElementArea area = getArea(region);
			if (myArea == null
				|| area.ParagraphIndex != myArea.ParagraphIndex
				|| area.ElementIndex != myArea.ElementIndex) { 
				myArea = area;
				return true;
			}
			return false;
		}

		protected boolean isLessThan(ZLTextElementArea area) {
			return myArea.weakCompareTo(area) <= 0;
		}

		protected boolean isGreaterThan(ZLTextElementArea area) {
			return myArea.weakCompareTo(area) >= 0;
		}

		protected abstract ZLTextElementArea getArea(ZLTextRegion region);
		protected abstract boolean isExpandedBy(ZLTextElementArea area);
		protected abstract boolean isAreaWithin(ZLTextElementArea area);
	}

	private class StartBound extends Bound {
		@Override
		protected ZLTextElementArea getArea(ZLTextRegion region) {
			return region.getFirstArea();
		}

		@Override
		protected boolean isExpandedBy(ZLTextElementArea area) {
			return isGreaterThan(area);
		}

		@Override
		protected boolean isAreaWithin(ZLTextElementArea area) {
			return isLessThan(area);
		}
	}

	private class EndBound extends Bound {
		@Override
		protected ZLTextElementArea getArea(ZLTextRegion region) {
			return region.getLastArea();
		}

		@Override
		protected boolean isExpandedBy(ZLTextElementArea area) {
			return isLessThan(area);
		}

		@Override
		protected boolean isAreaWithin(ZLTextElementArea area) {
			return isGreaterThan(area);
		}
	}

	private class Scroller implements Runnable {
		private final boolean myScrollForward;
		private int myX, myY;

		Scroller(boolean forward, int x, int y) {
			myScrollForward = forward;
			setXY(x, y);
			myView.Application.addTimerTask(this, 400);
		}

		boolean scrollsForward() {
			return myScrollForward;
		}

		void setXY(int x, int y) {
			myX = x;
			myY = y;
		}

		public void run() {
			myView.scrollPage(myScrollForward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
			myView.preparePaintInfo();
			expandTo(myX, myY);
			myView.Application.getViewWidget().reset();
			myView.Application.getViewWidget().repaint();
		}

		private void stop() {
			myView.Application.removeTimerTask(this);
		}
	}
}
