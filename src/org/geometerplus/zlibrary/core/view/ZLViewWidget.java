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

package org.geometerplus.zlibrary.core.view;

import org.geometerplus.zlibrary.core.application.ZLApplication;

abstract public class ZLViewWidget {
	private int myRotation;

	public interface Angle {
		int DEGREES0 = 0;
		int DEGREES90 = 90;
		int DEGREES180 = 180;
		int DEGREES270 = 270;
	};

	protected ZLViewWidget(int initialAngle) {
		myRotation = initialAngle;
	}

	public final void rotate(int rotation) {
		myRotation = rotation;
	}

	public final int getRotation() {
		return myRotation;
	}

	// TODO: change to protected
	abstract public void repaint();
}
