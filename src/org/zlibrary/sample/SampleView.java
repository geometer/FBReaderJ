package org.zlibrary.sample;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;

class SampleView extends ZLView {
	SampleView(SampleApplication application, ZLPaintContext context) {
		super(application, context);
	}

	public void paint() {
		ZLPaintContext context = getContext();
		context.drawLine(0, 0, context.getWidth() - 1, context.getHeight() - 1);
	}

	public String caption() {
		return "SampleView";
	}
}
