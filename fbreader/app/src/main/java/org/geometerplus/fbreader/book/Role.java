/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.book;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public final class Role {
	public static final Role NULL = new Role("");//null and empty string are same

	private final String Code;

	public Role(String code) {
		Code = code != null? code : "";
	}
	
	public String getCode() {
		return "".equals(Code) ? null : Code;
	}
	
	public String getName() {
		ZLResource resource = ZLResource.resource("authorRoles").getResource(Code);
		return resource.getValue();
	}
	
	@Override
	public int hashCode() {
		return Code.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Role)) {
			return false;
		}
		Role r = (Role)o;
		return Code.equals(r.Code);
	}
}
