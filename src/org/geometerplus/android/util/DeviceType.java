/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.util;

import android.os.Build;

public enum DeviceType {
	GENERIC,
	YOTA_PHONE,
	KINDLE_FIRE_1ST_GENERATION,
	KINDLE_FIRE_2ND_GENERATION,
	KINDLE_FIRE_HD,
	NOOK,
	NOOK12,
	EKEN_M001,
	PAN_DIGITAL,
	SAMSUNG_GT_S5830;

	private static DeviceType ourInstance;
	public static DeviceType Instance() {
		if (ourInstance == null) {
			if ("YotaPhone".equals(Build.BRAND)) {
				ourInstance = YOTA_PHONE;
			} else if ("GT-S5830".equals(Build.MODEL)) {
				ourInstance = SAMSUNG_GT_S5830;
			} else if ("Amazon".equals(Build.MANUFACTURER)) {
				if ("Kindle Fire".equals(Build.MODEL)) {
					ourInstance = KINDLE_FIRE_1ST_GENERATION;
				} else if ("KFOT".equals(Build.MODEL)) {
					ourInstance = KINDLE_FIRE_2ND_GENERATION;
				} else {
					ourInstance = KINDLE_FIRE_HD;
				}
			} else if (Build.DISPLAY != null && Build.DISPLAY.contains("simenxie")) {
				ourInstance = EKEN_M001;
			} else if ("PD_Novel".equals(Build.MODEL)) {
				ourInstance = PAN_DIGITAL;
			} else if ("BarnesAndNoble".equals(Build.MANUFACTURER) &&
					   "zoom2".equals(Build.DEVICE) &&
					   ("NOOK".equals(Build.MODEL) ||
						"unknown".equals(Build.MODEL) ||
						"BNRV350".equals(Build.MODEL) ||
						"BNRV300".equals(Build.MODEL))) {
				if ("1.2.0".equals(Build.VERSION.INCREMENTAL) ||
					"1.2.1".equals(Build.VERSION.INCREMENTAL)) {
					ourInstance = NOOK12;
				} else {
					ourInstance = NOOK;
				}
			} else {
				ourInstance = GENERIC;
			}
		}
		return ourInstance;
	}

	public boolean hasNoHardwareMenuButton() {
		return this == EKEN_M001 || this == PAN_DIGITAL;
	}

	public boolean hasButtonLightsBug() {
		return this == SAMSUNG_GT_S5830;
	}

	public boolean isEInk() {
		return this == NOOK || this == NOOK12;
	}

	public boolean hasStandardSearchDialog() {
		return this != NOOK && this != NOOK12;
	}
}
