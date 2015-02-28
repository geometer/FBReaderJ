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

package org.geometerplus.zlibrary.core.resources;

final class ZLMissingResource extends ZLResource {
	static final String Value = "????????";
	static final ZLMissingResource Instance = new ZLMissingResource();

	private ZLMissingResource() {
		super(Value);
	}

	@Override
	public ZLResource getResource(String key) {
		return this;
	}

	@Override
	public boolean hasValue() {
		return false;
	}

	@Override
	public String getValue() {
		return Value;
	}

	@Override
	public String getValue(int condition) {
		return Value;
	}
}
