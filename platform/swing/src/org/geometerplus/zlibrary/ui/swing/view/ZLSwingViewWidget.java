/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.swing.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;

import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.core.view.ZLView;

public final class ZLSwingViewWidget extends ZLViewWidget implements MouseListener, MouseMotionListener {
	@SuppressWarnings("serial")
	private class Panel extends JPanel {
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			final ZLView view = getView();
			final ZLSwingPaintContext context = (ZLSwingPaintContext)view.getContext();
			final Dimension size = getSize();
			final int rotation = getRotation();
			context.setRotation(rotation);
			switch (rotation) {
				case Angle.DEGREES0:
					context.setSize(size.width, size.height);
					break;
				case Angle.DEGREES90:
					g2d.rotate(-Math.PI / 2);
					g2d.translate(-size.height, 0);
					context.setSize(size.height, size.width);
					break;
				case Angle.DEGREES180:
					g2d.rotate(Math.PI);
					g2d.translate(-size.width, -size.height);
					context.setSize(size.width, size.height);
					break;
				case Angle.DEGREES270:
					g2d.rotate(Math.PI / 2);
					g2d.translate(0, -size.width);
					context.setSize(size.height, size.width);
					break;
			}
			context.setGraphics(g2d);
			view.paint();
		}
	}

	public ZLSwingViewWidget(int initialAngle) {
		super(initialAngle);
		myPanel.addMouseListener(this);
		myPanel.addMouseMotionListener(this);
	}

	public void repaint() {
		myPanel.repaint();
	}

	public void trackStylus(boolean track) {
		// TODO: implement
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		switch (getRotation()) {
			case Angle.DEGREES0:
				break;
			case Angle.DEGREES90:
			{
				final Dimension size = myPanel.getSize();
				int swap = x;
				x = size.height - y - 1;
				y = swap;
				break;
			}
			case Angle.DEGREES180:
			{
				final Dimension size = myPanel.getSize();
				x = size.width - x - 1;
				y = size.height - y - 1;
				break;
			}
			case Angle.DEGREES270:
			{
				final Dimension size = myPanel.getSize();
				int swap = size.width - x - 1;
				x = y;
				y = swap;
				break;
			}
		}
		getView().onStylusPress(x, y);
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public JPanel getPanel() {
		return myPanel;
	}

	private final Panel myPanel = new Panel();
}
