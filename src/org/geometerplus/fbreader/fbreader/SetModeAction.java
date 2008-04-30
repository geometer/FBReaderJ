/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

class SetModeAction extends FBAction {
	SetModeAction(FBReader fbreader, int modeToSet, int visibleInModes) {
		super(fbreader);
		myModeToSet = modeToSet;
		myVisibleInModes = visibleInModes;
	}

	public boolean isVisible() {
		return (Reader.getMode() & myVisibleInModes) != 0;
	}

	public void run() {
		Reader.setMode(myModeToSet);
	}

	private final int myModeToSet;
	private final int myVisibleInModes;
}
