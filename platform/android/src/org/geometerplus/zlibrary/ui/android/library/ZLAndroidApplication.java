/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.library;

import java.util.HashMap;

import android.app.Application;
//import android.hardware.SensorManager;
//import android.hardware.SensorListener;

public class ZLAndroidApplication extends Application {
	public void onCreate() {
		super.onCreate();
		android.webkit.MimeTypeMap map = android.webkit.MimeTypeMap.getSingleton();
		android.util.Log.w("epub", "" + map.hasExtension("epub"));
		android.util.Log.w("oeb", "" + map.hasExtension("oeb"));
		android.util.Log.w("fb2", "" + map.hasExtension("fb2"));
		android.util.Log.w("fb2.zip", "" + map.hasExtension("fb2.zip"));
		/*
		SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(new SensorListener() {
			public void onAccuracyChanged(int sensor, int accuracy) {
				android.util.Log.w("onAccuracyChanged", "" + sensor + ": " + accuracy);
			}

			public void onSensorChanged(int sensor, float[] values) {
				String sValues = "";
				for (float v : values) {
					sValues += v;
					sValues += ';';
				}
				android.util.Log.w("onSensorChanged", "" + sensor + ": " + sValues);
			}
		//}, SensorManager.SENSOR_ORIENTATION | SensorManager.SENSOR_ACCELEROMETER);
		}, SensorManager.SENSOR_ACCELEROMETER);
		*/
	}

	public void onTerminate() {
		super.onTerminate();
	}

	public void putData(Object key, Object value) {
		myData.put(key, value);
	}

	public void removeData(Object key) {
		myData.remove(key);
	}

	public Object getData(Object key) {
		return myData.get(key);
	}

	private final HashMap myData = new HashMap();
}
