/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Bookmark;

public class CancelMenuHelper {
	public final ZLBooleanOption ShowLibraryItemOption =
		new ZLBooleanOption("CancelMenu", "library", true);
	public final ZLBooleanOption ShowNetworkLibraryItemOption =
		new ZLBooleanOption("CancelMenu", "networkLibrary", true);
	public final ZLBooleanOption ShowPreviousBookItemOption =
		new ZLBooleanOption("CancelMenu", "previousBook", false);
	public final ZLBooleanOption ShowPositionItemsOption =
		new ZLBooleanOption("CancelMenu", "positions", true);

	static enum ActionType {
		library,
		networkLibrary,
		previousBook,
		returnTo,
		close
	}

	public static class ActionDescription {
		final ActionType Type;
		public final String Title;
		public final String Summary;

		ActionDescription(ActionType type, String summary) {
			final ZLResource resource = ZLResource.resource("cancelMenu");
			Type = type;
			Title = resource.getResource(type.toString()).getValue();
			Summary = summary;
		}
	}

	static class BookmarkDescription extends ActionDescription {
		final Bookmark Bookmark;

		BookmarkDescription(Bookmark b) {
			super(ActionType.returnTo, b.getText());
			Bookmark = b;
		}
	}
}
