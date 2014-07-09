/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.formatPlugin;

import java.util.Map;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.android.image.ZLBitmapImage;
import org.geometerplus.fbreader.book.MetaInfoUtil;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;
import org.geometerplus.android.fbreader.formatPlugin.metainfoservice.MetaInfoReader;

class PluginMetainfoReaderImpl implements MetaInfoUtil.PluginMetaInfoReader {
	public final Map<ExternalFormatPlugin,MetaInfoReader> Readers =
		new HashMap<ExternalFormatPlugin,MetaInfoReader>();

	@TargetApi(8)
	@Override
	public ZLBitmapImage readImage(ZLFile f, ExternalFormatPlugin plugin) {
		final MetaInfoReader reader = Readers.get(plugin);
		try {
			return reader != null ? new ZLBitmapImage(reader.readBitmap(f.getPath())) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ServiceConnection createConnection(final ExternalFormatPlugin plugin, final Runnable onConnected) {
		return new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				Readers.put(plugin, MetaInfoReader.Stub.asInterface(binder));
				if (onConnected != null) {
					onConnected.run();
				}
			}

			public void onServiceDisconnected(ComponentName className) {
				Readers.remove(plugin);
			}
		};
	}
}
