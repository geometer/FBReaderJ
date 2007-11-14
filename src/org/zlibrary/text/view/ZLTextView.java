package org.zlibrary.text.view;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.application.ZLApplication;

public abstract class ZLTextView extends ZLView {
	public ZLTextView(ZLApplication application, ZLPaintContext context) {
		super(application, context);
	}

	public abstract void setModel(String fileName);
	public abstract void paint();
	public abstract String caption();
}

