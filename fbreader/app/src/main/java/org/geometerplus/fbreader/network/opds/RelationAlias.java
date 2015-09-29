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

package org.geometerplus.fbreader.network.opds;

class RelationAlias implements Comparable<RelationAlias> {
	final String Alias;
	final String Type;

	// `alias` and `type` parameters must be either null or interned String.
	RelationAlias(String alias, String type) {
		Alias = alias;
		Type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RelationAlias)) {
			return false;
		}
		RelationAlias r = (RelationAlias) o;
		return Alias == r.Alias && Type == r.Type;
	}

	@Override
	public int hashCode() {
		return (Alias == null ? 0 : Alias.hashCode()) +
			(Type == null ? 0 : Type.hashCode());
	}

	public int compareTo(RelationAlias r) {
		if (Alias != r.Alias) {
			if (Alias == null) {
				return -1;
			} else if (r.Alias == null) {
				return 1;
			}
			return Alias.compareTo(r.Alias);
		}
		if (Type != r.Type) {
			if (Type == null) {
				return -1;
			} else if (r.Type == null) {
				return 1;
			}
			return Type.compareTo(r.Type);
		}
		return 0;
	}

	@Override
	public String toString() {
		return "Alias(" + Alias + "; " + Type + ")";
	}
}

