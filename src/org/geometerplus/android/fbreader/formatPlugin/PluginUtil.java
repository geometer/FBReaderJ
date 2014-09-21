/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.formatPlugin;

import android.content.Intent;

import org.geometerplus.fbreader.formats.ExternalFormatPlugin;

public abstract class PluginUtil {
	public static final String ACTION_VIEW = "android.fbreader.action.plugin.VIEW";
	public static final String ACTION_KILL = "android.fbreader.action.plugin.KILL";
	public static final String ACTION_CONNECT_COVER_SERVICE = "android.fbreader.action.plugin.CONNECT_COVER_SERVICE";

	public static Intent createIntent(ExternalFormatPlugin plugin, String action) {
		return new Intent(action).setPackage(plugin.packageName());
	}
}
