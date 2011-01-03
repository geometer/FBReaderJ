/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

class URLRewritingRule {

	// rule types:
	public static final int ADD_URL_PARAMETER = 0;

	// apply values:
	public static final int APPLY_ALWAYS = 0;
	public static final int APPLY_EXTERNAL = 1;
	public static final int APPLY_INTERNAL = 2;


	public final int Type;
	public final int Apply;
	public final String Name;
	public final String Value;

	public URLRewritingRule(int type, int apply, String name, String value) {
		Type = type;
		Apply = apply;
		Name = name;
		Value = value;
	}

	@Override
	public String toString() {
		return "Rule: {type=" + Type
			+ "; apply=" + Apply
			+ "; name=" + Name
			+ "; value=" + Value
			+ "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof URLRewritingRule)) {
			return false;
		}
		final URLRewritingRule rule = (URLRewritingRule) o;
		if (Type != rule.Type
				|| Apply != rule.Apply
				|| !ZLMiscUtil.equals(Name, rule.Name)
				|| !ZLMiscUtil.equals(Value, rule.Value)) {
			return false;
		}
		return true;
	}
}
