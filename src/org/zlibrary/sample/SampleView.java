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
		context.fillRectangle(context.getWidth() / 2, context.getHeight() / 2, context.getWidth() - 2, context.getHeight() - 2);

		String text = "Hello, World!";
		final int w = context.stringWidth(text);
		context.drawString((context.getWidth() - w) / 2, context.stringHeight(), text);
	}

	public String caption() {
		return "SampleView";
	}
}
