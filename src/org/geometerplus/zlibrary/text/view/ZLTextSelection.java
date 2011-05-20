package org.geometerplus.zlibrary.text.view;

public class ZLTextSelection {
	private static final int SELECTION_DISTANCE = 10;

	private ZLTextRegion myInitialRegion;
	private final Bound myLeftBound = new Bound();
	private final Bound myRightBound = new Bound();
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

		myLeftBound.set(myInitialRegion.getFirstArea());
		myRightBound.set(myInitialRegion.getLastArea());
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
			changed |= myRightBound.set(region.getLastArea());
		} else if (cmp > 0) {
			changed |= myLeftBound.set(region.getFirstArea());
		} else {
			changed |= myRightBound.set(region.getLastArea());
			changed |= myLeftBound.set(region.getFirstArea());
		}
		if (changed) {
			myText.delete(0, myText.length());
		}
		return changed;
	}

	private void prepareParagraphText(int paragraphID) {
		final ZLTextParagraphCursor paragraph = ZLTextParagraphCursor.cursor(myView.getModel(), paragraphID);
		final int startElementID = myLeftBound.myArea.ParagraphIndex == paragraphID ? myLeftBound.myArea.ElementIndex : 0;
		final int endElementID = myRightBound.myArea.ParagraphIndex == paragraphID ? myRightBound.myArea.ElementIndex : paragraph.getParagraphLength() - 1;

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
			final int from = myLeftBound.myArea.ParagraphIndex;
			final int to = myRightBound.myArea.ParagraphIndex;
			for (int i = from; i < to; ++i) {
				prepareParagraphText(i);
				myText.append("\n");
			}
			prepareParagraphText(to);
		}
		return myText.toString();
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
			if (isAreaSelected(page.TextElementMap.get(0))) {
				return 0;
			}
		}
		return id;
	}

	public int getEndAreaID(ZLTextPage page) {
		final int id = page.TextElementMap.indexOf(myRightBound.myArea);
		if (id == -1) {
			final int lastID = page.TextElementMap.size() - 1;
			if (isAreaSelected(page.TextElementMap.get(lastID))) {
				return lastID;
			}
		}
		return id;
	}

	private ZLTextElementArea getArea(int areaID) {
		if (areaID != -1) {
			return myView.myCurrentPage.TextElementMap.get(areaID);
		}
		return null;
	}

	ZLTextElementArea getStartArea() {
		return myLeftBound.myArea;
	}

	ZLTextElementArea getEndArea() {
		return myRightBound.myArea;
	}

	public int getStartY() {
		final ZLTextElementArea startArea = getArea(getStartAreaID(myView.myCurrentPage));
		if (startArea != null) {
			return startArea.YStart;
		}
		return 0;
	}
	public int getEndY() {
		final ZLTextElementArea endArea = getArea(getEndAreaID(myView.myCurrentPage));
		if (endArea != null) {
			return endArea.YEnd;
		}
		return 0;
	}

	private ZLTextRegion findSelectedRegion(int x, int y) { // TODO fast find
		return myView.findRegion(x, y, SELECTION_DISTANCE, ZLTextRegion.AnyRegionFilter);
	}

	private ZLTextRegion findNearestRegion(int x, int y) { // TODO fast find
		return myView.findRegion(x, y, Integer.MAX_VALUE - 1, ZLTextRegion.AnyRegionFilter);
	}

	private class Bound {
		private ZLTextElementArea myArea;

		private boolean set(ZLTextElementArea area) {
			if (myArea == null || myArea.compareTo(area) != 0) {
				myArea = area;
				return true;
			}
			return false;
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
