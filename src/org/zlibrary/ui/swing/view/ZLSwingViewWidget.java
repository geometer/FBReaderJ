package org.zlibrary.ui.swing.view;

import java.awt.*;
import javax.swing.JPanel;

import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.core.view.ZLView;

public final class ZLSwingViewWidget extends ZLViewWidget {
	private class Panel extends JPanel {
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			final ZLView view = getView();
			final ZLSwingPaintContext context = (ZLSwingPaintContext)view.getContext();
			final Dimension size = getSize();
			switch (getRotation()) {
				case DEGREES0:
					context.setSize(size.width, size.height);
					break;
				case DEGREES90:
					g2d.rotate(-Math.PI / 2);
					g2d.translate(-size.height, 0);
					context.setSize(size.height, size.width);
					break;
				case DEGREES180:
					g2d.rotate(Math.PI);
					g2d.translate(-size.width, -size.height);
					context.setSize(size.width, size.height);
					break;
				case DEGREES270:
					g2d.rotate(Math.PI / 2);
					g2d.translate(0, -size.width);
					context.setSize(size.height, size.width);
					break;
			}
			context.setGraphics(g2d);
			view.paint();
		}
	}

	public ZLSwingViewWidget(Angle initialAngle) {
		super(initialAngle);
	}

	public void repaint() {
		myPanel.repaint();
	}

	public void trackStylus(boolean track) {
		// TODO: implement
	}

	public JPanel getPanel() {
		return myPanel;
	}

	private final Panel myPanel = new Panel();
}
