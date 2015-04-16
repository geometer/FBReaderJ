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

package org.geometerplus.fbreader.fbreader.options;

import org.geometerplus.zlibrary.core.options.*;

public class SyncOptions {
	public static final String DOMAIN = "books.fbreader.org";
	public static final String BASE_URL = "https://" + DOMAIN + "/";
	public static final String OPDS_URL = "https://" + DOMAIN + "/opds";
	public static final String REALM = "FBReader book network";

	public final ZLBooleanOption Enabled =
		new ZLBooleanOption("Sync", "Enabled", false);

	public static enum Condition {
		never, viaWifi, always
	}
	public final ZLEnumOption<Condition> UploadAllBooks =
		new ZLEnumOption<Condition>("Sync", "UploadAllBooks", Condition.viaWifi);
	public final ZLEnumOption<Condition> Positions =
		new ZLEnumOption<Condition>("Sync", "Positions", Condition.always);
	public final ZLBooleanOption ChangeCurrentBook =
		new ZLBooleanOption("Sync", "ChangeCurrentBook", true);
	public final ZLEnumOption<Condition> Bookmarks =
		new ZLEnumOption<Condition>("Sync", "Bookmarks", Condition.always);
	public final ZLEnumOption<Condition> CustomShelves =
		new ZLEnumOption<Condition>("Sync", "CustomShelves", Condition.always);
	public final ZLEnumOption<Condition> Metainfo =
		new ZLEnumOption<Condition>("Sync", "Metainfo", Condition.always);
}
