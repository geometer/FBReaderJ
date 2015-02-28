/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.api;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class MenuNode implements Cloneable, Serializable {
	public static final long serialVersionUID = 42L;

	public final String Code;
	public String OptionalTitle;

	private MenuNode(String code) {
		Code = code;
	}

	public abstract MenuNode clone();

	public static final class Item extends MenuNode {
		public static final long serialVersionUID = 43L;

		public final Integer IconId;

		public Item(String code, Integer iconId) {
			super(code);
			IconId = iconId;
		}

		public Item(String code) {
			this(code, null);
		}

		public Item clone() {
			return new Item(Code, IconId);
		}
	}

	public static class Submenu extends MenuNode {
		public static final long serialVersionUID = 44L;

		public final ArrayList<MenuNode> Children = new ArrayList<MenuNode>();

		public Submenu(String code) {
			super(code);
		}

		public Submenu clone() {
			final Submenu copy = new Submenu(Code);
			for (MenuNode node : Children) {
				copy.Children.add(node.clone());
			}
			return copy;
		}
	}
}
