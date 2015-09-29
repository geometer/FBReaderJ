/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.library;

import java.util.List;

import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public abstract class ZLibrary {
	public static ZLibrary Instance() {
		return ourImplementation;
	}

	private static ZLibrary ourImplementation;

	public static final String SCREEN_ORIENTATION_SYSTEM = "system";
	public static final String SCREEN_ORIENTATION_SENSOR = "sensor";
	public static final String SCREEN_ORIENTATION_PORTRAIT = "portrait";
	public static final String SCREEN_ORIENTATION_LANDSCAPE = "landscape";
	public static final String SCREEN_ORIENTATION_REVERSE_PORTRAIT = "reversePortrait";
	public static final String SCREEN_ORIENTATION_REVERSE_LANDSCAPE = "reverseLandscape";

	public final ZLIntegerOption ScreenHintStageOption =
		new ZLIntegerOption("LookNFeel", "ScreenHintStage", 0);

	public final ZLStringOption getOrientationOption() {
		return new ZLStringOption("LookNFeel", "Orientation", "system");
	}

	protected ZLibrary() {
		ourImplementation = this;
	}

	abstract public ZLResourceFile createResourceFile(String path);
	abstract public ZLResourceFile createResourceFile(ZLResourceFile parent, String name);

	abstract public String getVersionName();
	abstract public String getFullVersionName();
	abstract public String getCurrentTimeString();
	abstract public int getDisplayDPI();
	abstract public int getWidthInPixels();
	abstract public int getHeightInPixels();
	abstract public List<String> defaultLanguageCodes();

	abstract public boolean supportsAllOrientations();
	public String[] allOrientations() {
		return supportsAllOrientations()
			? new String[] {
				SCREEN_ORIENTATION_SYSTEM,
				SCREEN_ORIENTATION_SENSOR,
				SCREEN_ORIENTATION_PORTRAIT,
				SCREEN_ORIENTATION_LANDSCAPE,
				SCREEN_ORIENTATION_REVERSE_PORTRAIT,
				SCREEN_ORIENTATION_REVERSE_LANDSCAPE
			}
			: new String[] {
				SCREEN_ORIENTATION_SYSTEM,
				SCREEN_ORIENTATION_SENSOR,
				SCREEN_ORIENTATION_PORTRAIT,
				SCREEN_ORIENTATION_LANDSCAPE
			};
	}
}
