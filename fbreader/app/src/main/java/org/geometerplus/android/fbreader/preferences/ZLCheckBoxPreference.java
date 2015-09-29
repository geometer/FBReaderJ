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

package org.geometerplus.android.fbreader.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class ZLCheckBoxPreference extends CheckBoxPreference {
	protected final ZLResource Resource;

	protected ZLCheckBoxPreference(Context context, ZLResource resource) {
		super(context);

		Resource = resource;
		setTitle(resource.getValue());
		final ZLResource onResource = resource.getResource("summaryOn");
		if (onResource.hasValue()) {
			setSummaryOn(onResource.getValue());
		}
		final ZLResource offResource = resource.getResource("summaryOff");
		if (offResource.hasValue()) {
			setSummaryOff(offResource.getValue());
		}
	}
}
