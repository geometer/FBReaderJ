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

import org.geometerplus.fbreader.network.atom.ATOMXMLReader;

public class TipsManager {
	public static ZLBooleanOption ShowTipsOption =
		new ZLBooleanOption("tips", "showTips", true);
	public static final ZLIntegerOption LastShownOption =
		new ZLIntegerOption("tips", "shownAt", 0);

	public List<Tip> collectTips(ZLFile file) {
		final TipsFeedHandler handler = new TipsFeedHandler();
		new ATOMXMLReader(handler, false).read(file);
		return Collections.unmodifiableList(handler.Tips);
	}
}
