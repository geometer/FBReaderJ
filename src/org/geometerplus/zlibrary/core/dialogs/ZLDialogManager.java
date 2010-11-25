/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.dialogs;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class ZLDialogManager {
	protected static ZLDialogManager ourInstance;
	
	public static final String COLOR_KEY = "color";
	
	protected ZLDialogManager() {
		ourInstance = this;
	}
	
	public static ZLDialogManager Instance() {
		return ourInstance;
	} 
	
	public abstract ZLOptionsDialog createOptionsDialog(String key);
	
	public abstract void wait(String key, Runnable runnable);
	
	protected static ZLResource getResource() {
		return ZLResource.resource("dialog");
	}
}
