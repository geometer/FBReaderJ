/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.network.atom;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class ATOMUpdated extends ATOMDateConstruct {
	public ATOMUpdated(ZLStringMap attributes) {
		super(attributes);
	}

	/*
	public ATOMUpdated(int year) {
		super(year);
	}

	public ATOMUpdated(int year, int month, int day) {
		super(year, month, day);
	}

	public ATOMUpdated(int year, int month, int day, int hour, int minutes, int seconds) {
		super(year, month, day, hour, minutes, seconds);
	}

	public ATOMUpdated(int year, int month, int day, int hour, int minutes, int seconds, float sfract) {
		super(year, month, day, hour, minutes, seconds, sfract);
	}

	public ATOMUpdated(int year, int month, int day, int hour, int minutes, int seconds, float sfract, int tzhour, int tzminutes) {
		super(year, month, day, hour, minutes, seconds, sfract, tzhour, tzminutes);
	}
	*/
}
