package org.geometerplus.zlibrary.text.view;

import java.util.Timer;
import java.util.TimerTask;

public class ZLTextSelection {
	private static final int SELECTION_DISTANCE = 10;  

	private int myCurrentChangingBoundID;
	private Bound myBounds[];
	private final StringBuilder myText;
	private boolean myIsTextValid;
	private ZLTextView myView;
	private Scroller myScroller;

	ZLTextSelection(ZLTextView view) {
		myView = view;
		myText = new StringBuilder();
		myBounds = new Bound[2];
		myBounds[0] = new StartBound();
		myBounds[1] = new EndBound();
		myScroller = new Scroller();
		clear();
	}

	boolean isEmpty() {
		return myBounds[0].compareTo(myBounds[1]) > 0;
	}

	boolean isEmptyOnPage(ZLTextPage page) {
		return getStartAreaID(page) == -1;
	}

	boolean clear() { // returns if it was filled before.
		boolean res = !isEmpty();
		myBounds[0].clear();
		myBounds[1].clear();
		myCurrentChangingBoundID = -1;
		return res;
	}
	boolean start(int x, int y) {
		clear();
		final ZLTextRegion newSelectedRegion = findSelectedRegion(x, y);
		if (newSelectedRegion == null) // whitespace.
			return false;

		myBounds[0].set(newSelectedRegion);
		myBounds[1].set(newSelectedRegion);
		return true;
	}
	void stop() {
		myScroller.stop();
	}
	void update() {
		myScroller.update();
	}
	private boolean expandBy(int boundID, ZLTextRegion newSelectedRegion) {
		if (myBounds[boundID].expandBy(newSelectedRegion)) {
			myCurrentChangingBoundID = boundID;
			return true;
		}
		return false;
	}
	private boolean equalsTo(int boundID, ZLTextRegion newSelectedRegion) {
		if (myBounds[boundID].equalsTo(newSelectedRegion)) {
			myCurrentChangingBoundID = boundID;
			return true;
		}
		return false;
	}
	boolean expandTo(int x, int y) { // TODO scroll if out of page.
		if (isEmpty()) {
			return start(x, y);
		}
		final ZLTextRegion newSelectedRegion = findSelectedRegion(x, y);

		myScroller.stop();
		// possible page rim.
		if (newSelectedRegion == null || equalsTo(0, newSelectedRegion) || equalsTo(1, newSelectedRegion)) {
			return myScroller.handle(x, y, newSelectedRegion);
		}
		
		if (!expandBy(0, newSelectedRegion) && !expandBy(1, newSelectedRegion)) {
			myBounds[myCurrentChangingBoundID].set(newSelectedRegion); // selection is now being shrinked
		}

		myIsTextValid = false;
		return true;
	}

	private void prepareParagraphText(int paragraphID) {
		final ZLTextParagraphCursor paragraph = ZLTextParagraphCursor.cursor(myView.getModel(), paragraphID);
		final int startElementID = myBounds[0].getParagraphIndex() == paragraphID ? myBounds[0].getElementIndex() : 0;
		final boolean isLastSelectedParagraph = myBounds[1].getParagraphIndex() == paragraphID; 
		final int endElementID = isLastSelectedParagraph ? myBounds[1].getElementIndex() : paragraph.getParagraphLength() - 1;

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
		if (!myIsTextValid) {
			myText.delete(0, myText.length());

			for (int i = myBounds[0].getParagraphIndex(); i <= myBounds[1].getParagraphIndex(); ++i) {
				prepareParagraphText(i);
			}
			myIsTextValid = true;
		}
		return myText.toString();
	}

	public ZLTextPosition getStartPosition() {
		return myBounds[0];
	}

	private boolean areaWithinSelection(ZLTextElementArea area) {
		return myBounds[0].isAreaWithin(area) && myBounds[1].isAreaWithin(area);
	}

	public boolean areaWithinStartBound(ZLTextElementArea area) {
		return !myBounds[1].isExpandedBy(area) && myBounds[0].isAreaWithin(area);
	}

	public boolean areaWithinEndBound(ZLTextElementArea area) {
		return !myBounds[0].isExpandedBy(area) && myBounds[1].isAreaWithin(area);
	}

	public boolean isAreaSelected(ZLTextElementArea area) {
		return !myBounds[0].isExpandedBy(area) && !myBounds[1].isExpandedBy(area);
	}

	public int getStartAreaID(ZLTextPage page) {
		final int id = page.TextElementMap.indexOf(myBounds[0].myArea);
		if (id == -1) {
			if (areaWithinSelection(page.TextElementMap.get(0))) {
				return 0;
			}
		}
		return id;
	}

	public int getEndAreaID(ZLTextPage page) {
		final int id = page.TextElementMap.indexOf(myBounds[1].myArea); 
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

		protected void set(ZLTextRegion region) {
			myArea = getArea(region);
			myParagraphID = myArea.ParagraphIndex;
			myElementID = myArea.ElementIndex;
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

		protected boolean expandBy(ZLTextRegion region) {
			if (isExpandedBy(region)) {
				set(region);
				return true;
			}
			return false;
		}

		protected abstract void clear();
		protected abstract ZLTextElementArea getArea(ZLTextRegion region);
		protected abstract boolean isYOutOfPage(int y);
		protected abstract boolean isExpandedBy(ZLTextElementArea area);
		protected abstract boolean isAreaWithin(ZLTextElementArea area);
	}

	private class StartBound extends Bound {
		@Override
		protected ZLTextElementArea getArea(ZLTextRegion region) {
			return getTextElementMap().get(region.getFromIndex());
		}

		@Override
		protected boolean isYOutOfPage(int y) {
			final ZLTextElementArea startPageArea = getTextElementMap().get(0);
			return (startPageArea.YStart + startPageArea.YEnd) / 2 > y;
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
		protected boolean isYOutOfPage(int y) {
			final ZLTextElementArea endPageArea = getTextElementMap().get(getTextElementMap().size() - 1);
			return (endPageArea.YStart + endPageArea.YEnd) / 2 < y;
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
	
	private class Scroller {
		private final Timer myTimer = new Timer();
		private TimerTask myScrollingTask;
		private int myStoredX, myStoredY;
		private boolean myScollForward;

		private void start(int x, int y) {
			myStoredX = x;
			myStoredY = y;
			myScrollingTask = new TimerTask() {
				public void run() {
					myView.scrollPage(myScollForward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
				}
			};
			myTimer.schedule(myScrollingTask, 200, 400);
		}

		private void stop() {
			if (myScrollingTask != null) {
				myScrollingTask.cancel();
			}
			myScrollingTask = null;
		}
		
		private void update() {
			if (myScrollingTask != null)
				expandTo(myStoredX, myStoredY);
		}
		private boolean isYOutOfPage(int boundID, int y) {
			if (!myBounds[boundID].isYOutOfPage(y))
				return false;
			myScollForward = boundID == 1;
			return true;
		}
		private boolean handle(int x, int y, ZLTextRegion newSelectedRegion) {
			if (!isYOutOfPage(0, y) && !isYOutOfPage(1, y))
				return false; // the whitespace is within the page.
			
			final ZLTextRegion nearestRegion = newSelectedRegion == null? findNearestRegion(x, y) : newSelectedRegion;
			if (nearestRegion != null)
				if (expandBy(0, nearestRegion) || expandBy(1, nearestRegion))
					return true;
			
			start(x, y);
			return false;
		}
	}
}
