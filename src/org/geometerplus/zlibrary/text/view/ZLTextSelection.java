package org.geometerplus.zlibrary.text.view;

public class ZLTextSelection {
	private static final int SELECTION_DISTANCE = 10;

	private ZLTextRegion myInitialRegion;
	private final Bound myLeftBound = new StartBound();
	private final Bound myRightBound = new EndBound();
	private Bound myCurrentChangingBound;
	private final StringBuilder myText = new StringBuilder();
	private boolean myTextIsInvalid;
	private final ZLTextView myView;
	private Scroller myScroller;

	ZLTextSelection(ZLTextView view) {
		myView = view;
		clear();
	}

	boolean isEmpty() {
		return myLeftBound.compareTo(myRightBound) > 0;
	}

	boolean isEmptyOnPage(ZLTextPage page) {
		return getStartAreaID(page) == -1;
	}

	boolean clear() { // returns if it was filled before.
		boolean res = !isEmpty();
		myInitialRegion = null;
		myLeftBound.clear();
		myRightBound.clear();
		myCurrentChangingBound = null;
		myTextIsInvalid = true;
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
			myTextIsInvalid = true;
		}
		return changed;
	}

	private void prepareParagraphText(int paragraphID) {
		final ZLTextParagraphCursor paragraph = ZLTextParagraphCursor.cursor(myView.getModel(), paragraphID);
		final int startElementID = myLeftBound.getParagraphIndex() == paragraphID ? myLeftBound.getElementIndex() : 0;
		final boolean isLastSelectedParagraph = myRightBound.getParagraphIndex() == paragraphID;
		final int endElementID = isLastSelectedParagraph ? myRightBound.getElementIndex() : paragraph.getParagraphLength() - 1;

		for (int elementID = startElementID; elementID <= endElementID; elementID++) {
			final ZLTextElement element = paragraph.getElement(elementID);
			if (element == ZLTextElement.HSpace)
				myText.append(" ");
			else if (element instanceof ZLTextWord) {
				ZLTextWord word = (ZLTextWord)element;
				myText.append(word.Data, word.Offset, word.Length);
			}
		}
		if (!isLastSelectedParagraph)
			myText.append("\n");
	}

	String getText() {
		if (myTextIsInvalid) {
			myText.delete(0, myText.length());

			for (int i = myLeftBound.getParagraphIndex(); i <= myRightBound.getParagraphIndex(); ++i) {
				prepareParagraphText(i);
			}
			myTextIsInvalid = false;
		}
		return myText.toString();
	}

	public ZLTextPosition getStartPosition() {
		return myLeftBound;
	}

	private boolean areaWithinSelection(ZLTextElementArea area) {
		return myLeftBound.isAreaWithin(area) && myRightBound.isAreaWithin(area);
	}

	public boolean areaWithinStartBound(ZLTextElementArea area) {
		return !myRightBound.isExpandedBy(area) && myLeftBound.isAreaWithin(area);
	}

	public boolean areaWithinEndBound(ZLTextElementArea area) {
		return !myLeftBound.isExpandedBy(area) && myRightBound.isAreaWithin(area);
	}

	public boolean isAreaSelected(ZLTextElementArea area) {
		return !myLeftBound.isExpandedBy(area) && !myRightBound.isExpandedBy(area);
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

	private ZLTextElementArea getStartArea() {
		return getArea(getStartAreaID(myView.myCurrentPage));
	}

	private ZLTextElementArea getEndArea() {
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
		protected int myParagraphID, myElementID;

		@Override
		public int getParagraphIndex() {
			return myParagraphID;
		}

		@Override
		public int getElementIndex() {
			return myElementID;
		}

		@Override
		public int getCharIndex() {
			return 0;
		}

		protected boolean set(ZLTextRegion region) {
			myArea = getArea(region);
			if (myParagraphID != myArea.ParagraphIndex || myElementID != myArea.ElementIndex) { 
				myParagraphID = myArea.ParagraphIndex;
				myElementID = myArea.ElementIndex;
				return true;
			}
			return false;
		}

		protected boolean isLessThan(ZLTextElementArea area) {
			return
				myParagraphID < area.ParagraphIndex ||
				(myParagraphID == area.ParagraphIndex && myElementID < area.ElementIndex);
		}

		protected boolean isGreaterThan(ZLTextElementArea area) {
			return
				myParagraphID > area.ParagraphIndex ||
				(myParagraphID == area.ParagraphIndex && myElementID > area.ElementIndex);
		}

		protected boolean equalsTo(ZLTextRegion region) {
			ZLTextElementArea area = getArea(region);
			return myParagraphID  == area.ParagraphIndex && myElementID == area.ElementIndex;
		}

		protected boolean isExpandedBy(ZLTextRegion region) {
			return isExpandedBy(getArea(region));
		}

		protected abstract void clear();
		protected abstract ZLTextElementArea getArea(ZLTextRegion region);
		protected abstract boolean isExpandedBy(ZLTextElementArea area);
		protected abstract boolean isAreaWithin(ZLTextElementArea area);
	}

	private class StartBound extends Bound {
		@Override
		protected ZLTextElementArea getArea(ZLTextRegion region) {
			return getTextElementMap().get(region.getFromIndex());
		}

		@Override
		protected boolean isExpandedBy(ZLTextElementArea area) {
			return isGreaterThan(area);
		}

		@Override
		protected boolean isAreaWithin(ZLTextElementArea area) {
			return isLessThan(area);
		}

		@Override
		protected void clear() {
			myParagraphID = Integer.MAX_VALUE;
		}
	}

	private class EndBound extends Bound {
		@Override
		protected ZLTextElementArea getArea(ZLTextRegion region) {
			return getTextElementMap().get(region.getToIndex() - 1);
		}

		@Override
		protected boolean isExpandedBy(ZLTextElementArea area) {
			return isLessThan(area);
		}

		@Override
		protected boolean isAreaWithin(ZLTextElementArea area) {
			return isGreaterThan(area);
		}

		@Override
		protected void clear() {
			myParagraphID = -1;
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
