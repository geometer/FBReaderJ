package org.geometerplus.zlibrary.text.view;

public class ZLTextSelection {
	private static final int SELECTION_DISTANCE = 10;  

	private final Bound myLeftBound = new StartBound();
	private final Bound myRightBound = new EndBound();
	private Bound myCurrentChangingBound;
	private final StringBuilder myText = new StringBuilder();
	private boolean myIsTextValid;
	private final ZLTextView myView;
	private final Scroller myScroller = new Scroller();

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
		myLeftBound.clear();
		myRightBound.clear();
		myCurrentChangingBound = null;
		return res;
	}
	boolean start(int x, int y) {
		clear();
		final ZLTextRegion newSelectedRegion = findSelectedRegion(x, y);
		if (newSelectedRegion == null) // whitespace.
			return false;

		myLeftBound.set(newSelectedRegion);
		myRightBound.set(newSelectedRegion);
		return true;
	}
	void stop() {
		myScroller.stop();
	}
	void update() {
		myScroller.update();
	}
	boolean expandTo(int x, int y) { // TODO scroll if out of page.
		if (isEmpty()) {
			return start(x, y);
		}
		final ZLTextRegion newSelectedRegion = findSelectedRegion(x, y);

		myScroller.stop();
		// possible page rim.
		if (newSelectedRegion == null) {
			return myScroller.handle(x, y, newSelectedRegion);
		} else if (myLeftBound.equalsTo(newSelectedRegion)) {
			myCurrentChangingBound = myLeftBound;
			return myScroller.handle(x, y, newSelectedRegion);
		} else if (myRightBound.equalsTo(newSelectedRegion)) {
			myCurrentChangingBound = myRightBound;
			return myScroller.handle(x, y, newSelectedRegion);
		}
		
		if (myLeftBound.expandBy(newSelectedRegion)) {
			myCurrentChangingBound = myLeftBound;
		} else if (myRightBound.expandBy(newSelectedRegion)) {
			myCurrentChangingBound = myRightBound;
		} else if (myCurrentChangingBound != null) {
			myCurrentChangingBound.set(newSelectedRegion); // selection is now being shrinked
		}

		myIsTextValid = false;
		return true;
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
		if (!myIsTextValid) {
			myText.delete(0, myText.length());

			for (int i = myLeftBound.getParagraphIndex(); i <= myRightBound.getParagraphIndex(); ++i) {
				prepareParagraphText(i);
			}
			myIsTextValid = true;
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
		private Runnable myScrollingTask;
		private int myStoredX, myStoredY;
		private boolean myScrollForward;

		private void start(int x, int y) {
			myStoredX = x;
			myStoredY = y;
			myScrollingTask = new Runnable() {
				public void run() {
					myView.scrollPage(myScrollForward, ZLTextView.ScrollingMode.SCROLL_LINES, 1);
				}
			};
			myView.Application.addTimerTask(myScrollingTask, 400);
		}

		private void stop() {
			if (myScrollingTask != null) {
				myView.Application.removeTimerTask(myScrollingTask);
			}
			myScrollingTask = null;
		}
		
		private void update() {
			if (myScrollingTask != null) {
				expandTo(myStoredX, myStoredY);
			}
		}

		private boolean handle(int x, int y, ZLTextRegion newSelectedRegion) {
			if (myLeftBound.isYOutOfPage(y)) {
				myScrollForward = false;
			} else if (myRightBound.isYOutOfPage(y)) {
				myScrollForward = true;
			} else {
				return false; // the whitespace is within the page.
			}
			
			final ZLTextRegion nearestRegion = newSelectedRegion == null? findNearestRegion(x, y) : newSelectedRegion;
			if (nearestRegion != null) {
				if (myLeftBound.expandBy(nearestRegion)) {
					myCurrentChangingBound = myLeftBound;
					return true;
				} else if (myRightBound.expandBy(nearestRegion)) {
					myCurrentChangingBound = myRightBound;
					return true;
				}
			}
			
			start(x, y);
			return false;
		}
	}
}
