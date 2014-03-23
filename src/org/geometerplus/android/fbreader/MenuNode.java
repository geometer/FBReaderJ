/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class MenuNode implements Serializable {
	public final String Code;

	private MenuNode(String code) {
		Code = code;
	}

	public abstract MenuNode findByCode(String code);

	public static final class Item extends MenuNode {
		public final Integer IconId;
	
		Item(String code, Integer iconId) {
			super(code);
			IconId = iconId;
		}

		Item(String code) {
			this(code, null);
		}

		public MenuNode findByCode(String code) {
			return Code.equals(code) ? this : null;
		}
	}

	public static class Submenu extends MenuNode {
		public final ArrayList<MenuNode> Children = new ArrayList<MenuNode>();

		Submenu(String code) {
			super(code);
		}

		public MenuNode findByCode(String code) {
			if (Code.equals(code)) {
				return this;
			}
			for (MenuNode node : Children) {
				MenuNode candidate = node.findByCode(code);
				if (candidate != null) {
					return candidate;
				}
			}
			return null;
		}
	}
}
