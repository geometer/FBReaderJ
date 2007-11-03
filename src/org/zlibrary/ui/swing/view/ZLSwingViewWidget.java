package org.zlibrary.ui.swing.view;

import java.awt.Graphics;
import javax.swing.JPanel;

import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.core.view.ZLView;

public class ZLSwingViewWidget extends ZLViewWidget {

	public ZLSwingViewWidget(Angle initialAngle) {
		super(initialAngle);
		myPanel = new ZLSwingPanel(this);
	}

	public void repaint() {
		myPanel.repaint();
	}

	public void setView(ZLView view) {
		super.setView(view);
		((ZLSwingPaintContext)view.getContext()).setComponent(myPanel);
	}

	public void trackStylus(boolean track) {
		// TODO: implement
	}

	public JPanel getPanel() {
		return myPanel;
	}

	private JPanel myPanel;
}

class ZLSwingPanel extends JPanel {
	ZLSwingPanel(ZLSwingViewWidget viewWidget) {
		myViewWidget = viewWidget;
	}

	public void paint(Graphics g) {
		super.paint(g);
		ZLView view = myViewWidget.getView();
		ZLSwingPaintContext context = (ZLSwingPaintContext)view.getContext();
		context.setGraphics(g);
		view.paint();
	}

	private ZLSwingViewWidget myViewWidget;
}
