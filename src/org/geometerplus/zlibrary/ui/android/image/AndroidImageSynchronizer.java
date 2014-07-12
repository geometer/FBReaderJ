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

package org.geometerplus.zlibrary.ui.android.image;

import java.util.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.*;
import android.os.IBinder;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.image.ZLBitmapImage;

import org.geometerplus.fbreader.book.MetaInfoUtil;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.formatPlugin.metainfoservice.MetaInfoReader;

public class AndroidImageSynchronizer implements ZLLoadableImage.Synchronizer, MetaInfoUtil.PluginMetaInfoReader {
	public final Map<ExternalFormatPlugin,MetaInfoReader> Readers =
		new HashMap<ExternalFormatPlugin,MetaInfoReader>();

	private final Context myContext;
	private final List<ServiceConnection> myConnections = new LinkedList<ServiceConnection>();

	public AndroidImageSynchronizer(Activity activity) {
		myContext = activity;
	}

	public AndroidImageSynchronizer(Service service) {
		myContext = service;
	}

	@Override
	public void startImageLoading(ZLLoadableImage image, Runnable postAction) {
		final ZLAndroidImageManager manager = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
		manager.startImageLoading(image, postAction);
	}

	public synchronized void clear() {
		for (ServiceConnection connection : myConnections) {
			myContext.unbindService(connection);
		}
		myConnections.clear();
	}

	public synchronized MetaInfoUtil.PluginMetaInfoReader createMetainfoReader() {
		for (final ExternalFormatPlugin plugin : PluginCollection.Instance().getExternalPlugins()) {
			final ServiceConnection connection = createConnection(plugin);
			myConnections.add(connection);
			final Intent i = PluginUtil.createIntent(plugin, PluginUtil.ACTION_CONNECT_COVER_SERVICE);
			myContext.bindService(i, connection, Context.BIND_AUTO_CREATE);
		}
		return this;
	}

	private ServiceConnection createConnection(final ExternalFormatPlugin plugin) {
		return new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				Readers.put(plugin, MetaInfoReader.Stub.asInterface(binder));
			}

			public void onServiceDisconnected(ComponentName className) {
				Readers.remove(plugin);
			}
		};
	}

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
}
