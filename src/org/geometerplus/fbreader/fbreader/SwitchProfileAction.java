/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

class SwitchProfileAction extends FBAction {
	private String myProfileName;

	SwitchProfileAction(FBReaderApp fbreader, String profileName) {
		super(fbreader);
		myProfileName = profileName;
	}

	@Override
	public boolean isVisible() {
		return !myProfileName.equals(Reader.ViewOptions.ColorProfileName.getValue());
	}

	@Override
	protected void run(Object ... params) {
		Reader.ViewOptions.ColorProfileName.setValue(myProfileName);
		Reader.getViewWidget().reset();
		Reader.getViewWidget().repaint();
	}
}
