/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

public interface ApiListener {
	String EVENT_READ_MODE_OPENED = "startReading";
	String EVENT_READ_MODE_CLOSED = "stopReading";

	void onEvent(int event);
}
