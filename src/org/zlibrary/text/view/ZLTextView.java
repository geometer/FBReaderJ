package org.zlibrary.text.view;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.text.model.ZLTextModel;

public abstract class ZLTextView extends ZLView {
	public interface ScrollingMode {
		int NO_OVERLAPPING = 0;
		int KEEP_LINES = 1;
		int SCROLL_LINES = 2;
		int SCROLL_PERCENTAGE = 3;
	};

	public ZLTextView(ZLApplication application, ZLPaintContext context) {
		super(application, context);
	}

	public abstract void setModel(ZLTextModel model);
	public abstract void paint();
	public abstract String caption();

	public abstract void scrollPage(boolean forward, int scrollingMode, int value);

	//public abstract void gotoParagraph(int index);

	public abstract int getLeftMargin();
	public abstract int getRightMargin();
	public abstract int getTopMargin();
	public abstract int getBottomMargin();
}
