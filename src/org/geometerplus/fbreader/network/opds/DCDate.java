/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.opds;

import org.geometerplus.fbreader.network.atom.*;

class DCDate extends ATOMDateConstruct {

	public DCDate() {
	}

	public DCDate(int year) {
		super(year);
	}

	public DCDate(int year, int month, int day) {
		super(year, month, day);
	}

	public DCDate(int year, int month, int day, int hour, int minutes, int seconds) {
		super(year, month, day, hour, minutes, seconds);
	}

	public DCDate(int year, int month, int day, int hour, int minutes, int seconds, float sfract) {
		super(year, month, day, hour, minutes, seconds, sfract);
	}

	public DCDate(int year, int month, int day, int hour, int minutes, int seconds, float sfract, int tzhour, int tzminutes) {
		super(year, month, day, hour, minutes, seconds, sfract, tzhour, tzminutes);
	}
}
