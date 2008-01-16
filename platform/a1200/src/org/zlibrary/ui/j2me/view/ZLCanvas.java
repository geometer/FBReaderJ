package org.zlibrary.ui.j2me.view;

import javax.microedition.lcdui.*;

public class ZLCanvas extends Canvas {
	public void paint(Graphics g) {
		g.drawLine(0, 0, getWidth(), getHeight());
		System.err.println("HELLO");
		// TODO: implement
	}

	public void keyPressed(int keyCode) {
		repaint();
	}
}
