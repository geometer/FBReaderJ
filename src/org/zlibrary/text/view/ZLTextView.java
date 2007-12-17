package org.zlibrary.text.view;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.text.model.ZLTextModel;

public abstract class ZLTextView extends ZLView {
	public ZLTextView(ZLApplication application, ZLPaintContext context) {
		super(application, context);
	}

	public abstract void setModel(ZLTextModel model);
	public abstract void paint();
	public abstract String caption();

	// TO BE DELETED;
	// this is temporary method for dummy scrolling
	public abstract void scroll(int numberOfParagraphs);
}
