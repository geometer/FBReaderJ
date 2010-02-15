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

package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class ZLTextAlignmentOptionEntry extends ZLComboOptionEntry {
	private static final String KEY_LEFT = "left";
	private static final String KEY_RIGHT = "right";
	private static final String KEY_CENTER = "center";
	private static final String KEY_JUSTIFY = "justify";
	private static final String KEY_UNCHANGED = "unchanged";
	
	private static final ArrayList ourValues4 = new ArrayList();
	private static final ArrayList ourValues5 = new ArrayList();

	private	final ZLResource myResource;
	private	final ZLIntegerOption myOption;
	private	final boolean myAllowUndefined;
		
	public ZLTextAlignmentOptionEntry(ZLIntegerOption option, final ZLResource resource, boolean allowUndefined) {
		myAllowUndefined = allowUndefined;
		myOption = option;
		myResource = resource;
		if (ourValues5.isEmpty()) {
			ourValues5.add(resource.getResource(KEY_UNCHANGED).getValue());
			String value = resource.getResource(KEY_LEFT).getValue();
			ourValues5.add(value);
			ourValues4.add(value);
			value = resource.getResource(KEY_RIGHT).getValue();
			ourValues5.add(value);
			ourValues4.add(value);
			value = resource.getResource(KEY_CENTER).getValue();
			ourValues5.add(value);
			ourValues4.add(value);
			value = resource.getResource(KEY_JUSTIFY).getValue();
			ourValues5.add(value);
			ourValues4.add(value);
		}
	}	
		
	public ArrayList getValues() {
		return myAllowUndefined ? ourValues5 : ourValues4;
	}

	public String initialValue() {
		int value = myOption.getValue();
		if ((value < 0) || (value >= 5)) {
			value = 0;
		}
		return (String)ourValues5.get(value);
	}

	public void onAccept(String value) {
		for (int i = 0; i < 5; ++i) {
			if (ourValues5.get(i).equals(value)) {
				myOption.setValue(i);
				break;
			}
		}
	}
}
