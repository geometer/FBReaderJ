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

package org.geometerplus.zlibrary.ui.android.util;

import android.view.KeyEvent;

public final class ZLAndroidKeyUtil {
	public static String getKeyNameByCode(int code) {
		switch (code) {
			case KeyEvent.KEYCODE_0:
				return "<0>";
			case KeyEvent.KEYCODE_1:
				return "<1>";
			case KeyEvent.KEYCODE_2:
				return "<2>";
			case KeyEvent.KEYCODE_3:
				return "<3>";
			case KeyEvent.KEYCODE_4:
				return "<4>";
			case KeyEvent.KEYCODE_5:
				return "<5>";
			case KeyEvent.KEYCODE_6:
				return "<6>";
			case KeyEvent.KEYCODE_7:
				return "<7>";
			case KeyEvent.KEYCODE_8:
				return "<8>";
			case KeyEvent.KEYCODE_9:
				return "<9>";
			case KeyEvent.KEYCODE_A:
				return "<A>";
			case KeyEvent.KEYCODE_B:
				return "<B>";
			case KeyEvent.KEYCODE_C:
				return "<C>";
			case KeyEvent.KEYCODE_D:
				return "<D>";
			case KeyEvent.KEYCODE_E:
				return "<E>";
			case KeyEvent.KEYCODE_F:
				return "<F>";
			case KeyEvent.KEYCODE_G:
				return "<G>";
			case KeyEvent.KEYCODE_H:
				return "<H>";
			case KeyEvent.KEYCODE_I:
				return "<I>";
			case KeyEvent.KEYCODE_J:
				return "<J>";
			case KeyEvent.KEYCODE_K:
				return "<K>";
			case KeyEvent.KEYCODE_L:
				return "<L>";
			case KeyEvent.KEYCODE_M:
				return "<M>";
			case KeyEvent.KEYCODE_N:
				return "<N>";
			case KeyEvent.KEYCODE_O:
				return "<O>";
			case KeyEvent.KEYCODE_P:
				return "<P>";
			case KeyEvent.KEYCODE_Q:
				return "<Q>";
			case KeyEvent.KEYCODE_R:
				return "<R>";
			case KeyEvent.KEYCODE_S:
				return "<S>";
			case KeyEvent.KEYCODE_T:
				return "<T>";
			case KeyEvent.KEYCODE_U:
				return "<U>";
			case KeyEvent.KEYCODE_V:
				return "<V>";
			case KeyEvent.KEYCODE_W:
				return "<W>";
			case KeyEvent.KEYCODE_X:
				return "<X>";
			case KeyEvent.KEYCODE_Y:
				return "<Y>";
			case KeyEvent.KEYCODE_Z:
				return "<Z>";
			case KeyEvent.KEYCODE_APOSTROPHE:
				return "<'>";
			case KeyEvent.KEYCODE_AT:
				return "<@>";
			case KeyEvent.KEYCODE_BACK:
				return "<Back>";
			case KeyEvent.KEYCODE_BACKSLASH:
				return "<\\>";
			case KeyEvent.KEYCODE_CALL:
				return "<Call>";
			case KeyEvent.KEYCODE_CAMERA:
				return "<Camera>";
			case KeyEvent.KEYCODE_CLEAR:
				return "<Clear>";
			case KeyEvent.KEYCODE_COMMA:
				return "<,>";
			case KeyEvent.KEYCODE_DEL:
				return "<Del>";
			case KeyEvent.KEYCODE_DPAD_CENTER:
				return "<PadCenter>";
			case KeyEvent.KEYCODE_DPAD_DOWN:
				return "<PadDown>";
			case KeyEvent.KEYCODE_DPAD_LEFT:
				return "<PadLeft>";
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				return "<PadRight>";
			case KeyEvent.KEYCODE_DPAD_UP:
				return "<PadUp>";
			case KeyEvent.KEYCODE_ENDCALL:
				return "<EndCall>";
			case KeyEvent.KEYCODE_ENTER:
				return "<Enter>";
			case KeyEvent.KEYCODE_ENVELOPE:
				return "<Envelope>";
			case KeyEvent.KEYCODE_EQUALS:
				return "<=>";
			case KeyEvent.KEYCODE_EXPLORER:
				return "<Explorer>";
			case KeyEvent.KEYCODE_FOCUS:
				return "<??? 0>";
			case KeyEvent.KEYCODE_GRAVE:
				return "<??? 1>";
			case KeyEvent.KEYCODE_HEADSETHOOK:
				return "<??? 2>";
			case KeyEvent.KEYCODE_HOME:
				return "<Home>";
			case KeyEvent.KEYCODE_LEFT_BRACKET:
				return "<(>";
			case KeyEvent.KEYCODE_MENU:
				return "<Menu>";
			case KeyEvent.KEYCODE_MINUS:
				return "<->";
			case KeyEvent.KEYCODE_NOTIFICATION:
				return "<??? 3>";
			case KeyEvent.KEYCODE_NUM:
				return "<Num>";
			case KeyEvent.KEYCODE_PERIOD:
				return "<??? 4>";
			case KeyEvent.KEYCODE_PLUS:
				return "<+>";
			case KeyEvent.KEYCODE_POUND:
				return "<??? 5>";
			case KeyEvent.KEYCODE_POWER:
				return "<Power>";
			case KeyEvent.KEYCODE_RIGHT_BRACKET:
				return "<)>";
			case KeyEvent.KEYCODE_SEMICOLON:
				return "<;>";
			case KeyEvent.KEYCODE_SLASH:
				return "</>";
			case KeyEvent.KEYCODE_SOFT_LEFT:
				return "<??? 6>";
			case KeyEvent.KEYCODE_SOFT_RIGHT:
				return "<??? 7>";
			case KeyEvent.KEYCODE_SPACE:
				return "<Space>";
			case KeyEvent.KEYCODE_STAR:
				return "<*>";
			case KeyEvent.KEYCODE_SYM:
				return "<Sym>";
			case KeyEvent.KEYCODE_TAB:
				return "<Tab>";
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				return "<VolumeDown>";
			case KeyEvent.KEYCODE_VOLUME_UP:
				return "<VolumeUp>";
			case KeyEvent.KEYCODE_UNKNOWN:
			default:
				return "<Unknown>";
		}
	}

	private ZLAndroidKeyUtil() {
	}
}
