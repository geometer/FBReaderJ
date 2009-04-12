package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;

public class ScrollingPreferences {
	private static ScrollingPreferences ourInstance;

	public static ScrollingPreferences Instance() {
		return (ourInstance != null) ? ourInstance : new ScrollingPreferences();
	}

	public final ZLBooleanOption FlickOption = new ZLBooleanOption("Scrolling", "Flick", true);
	public final ZLBooleanOption VolumeKeysOption = new ZLBooleanOption("Scrolling", "VolumeKeys", true);
	public final ZLBooleanOption AnimateOption = new ZLBooleanOption("Scrolling", "ShowAnimated", true);
	public final ZLBooleanOption HorizontalOption = new ZLBooleanOption("Scrolling", "Horizontal", false);

	private ScrollingPreferences() {
		ourInstance = this;
	}
}
