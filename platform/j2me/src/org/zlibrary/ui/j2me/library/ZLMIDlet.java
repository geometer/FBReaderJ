package org.zlibrary.ui.j2me.library;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

public class ZLMIDlet extends MIDlet /*implements CommandListener*/ {
	/*
	public ZLMIDlet() {
		super();
	}
	*/
	private boolean myStarted;

	public void startApp() {
		if (!myStarted) {
			new ZLJ2MELibrary().run(this);
			myStarted = true;
		}
	}

	public void destroyApp(boolean destroy) {
		notifyDestroyed();
	}

	public void pauseApp() {
	}

	/*
	public void commandAction(Command command, Displayable displayable) {
	}

	void doQuit() {
		destroyApp(true);
	}
	*/
}
