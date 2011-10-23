/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.tips;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.network.atom.ATOMXMLReader;

public class TipsManager {
	public static ZLBooleanOption ShowTipsOption =
		new ZLBooleanOption("tips", "showTips", true);

	// time when last tip was shown, 2^16 milliseconds
	private static final ZLIntegerOption ourLastShownOption =
		new ZLIntegerOption("tips", "shownAt", 0);
	// index of next tip to show
	private static final ZLIntegerOption ourIndexOption =
		new ZLIntegerOption("tips", "index", 0);

	private static ZLFile getFile() {
		return ZLFile.createFileByPath(Paths.networkCacheDirectory() + "/tips/tips.xml");
	}

	private List<Tip> myTips;
	private List<Tip> getTips() {
		if (myTips == null) {
			final ZLFile file = getFile();
			if (file.exists()) {
				final TipsFeedHandler handler = new TipsFeedHandler();
				new ATOMXMLReader(handler, false).read(file);
				final List<Tip> tips = Collections.unmodifiableList(handler.Tips);
				if (tips.size() > 0) {
					myTips = tips;
				}
			}
		}
		return myTips;
	}

	public boolean hasNextTip() {
		final List<Tip> tips = getTips();
		return tips != null && ourIndexOption.getValue() < tips.size();
	}

	public Tip getNextTip() {
		final List<Tip> tips = getTips();
		if (tips == null) {
			return null;
		}

		final int index = ourIndexOption.getValue();
		if (index >= tips.size()) {
			getFile().getPhysicalFile().delete();
			ourIndexOption.setValue(0);
			return null;
		}

		ourIndexOption.setValue(index + 1);
		ourLastShownOption.setValue(currentTime());
		return tips.get(index);
	}

	private static final int DELAY = 0;//(24 * 60 * 60 * 1000) >> 16; // 1 day

	private static int currentTime() {
		return (int)(new Date().getTime() >> 16);
	}
}
