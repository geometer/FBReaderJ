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

package org.geometerplus.android.fbreader.util;

import java.util.*;

import android.app.Activity;
import android.app.Service;
import android.content.*;
import android.os.IBinder;

import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.image.ZLImageSelfSynchronizableProxy;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLBitmapImage;

import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;
import org.geometerplus.fbreader.formats.external.PluginImage;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.formatPlugin.CoverReader;

public class AndroidImageSynchronizer implements ZLImageProxy.Synchronizer {
	private volatile boolean myIsInitialized;

	public final Map<ExternalFormatPlugin,CoverReader> Readers =
		new HashMap<ExternalFormatPlugin,CoverReader>();

	private final Context myContext;
	private final List<ServiceConnection> myConnections = new LinkedList<ServiceConnection>();

	public AndroidImageSynchronizer(Activity activity) {
		myContext = activity;
	}

	public AndroidImageSynchronizer(Service service) {
		myContext = service;
	}

	@Override
	public void startImageLoading(ZLImageProxy image, Runnable postAction) {
		final ZLAndroidImageManager manager = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
		manager.startImageLoading(this, image, postAction);
	}

	@Override
	public void synchronize(ZLImageProxy image) {
		if (image instanceof ZLImageSelfSynchronizableProxy) {
			((ZLImageSelfSynchronizableProxy)image).synchronize();
		} else if (image instanceof PluginImage) {
			final PluginImage pluginImage = (PluginImage)image;
			final CoverReader reader = Readers.get(pluginImage.Plugin);
			if (reader != null) {
				try {
					pluginImage.setRealImage(new ZLBitmapImage(reader.readBitmap(pluginImage.File.getPath())));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void clear() {
		for (ServiceConnection connection : myConnections) {
			myContext.unbindService(connection);
		}
		myConnections.clear();
	}

	public synchronized void initialize() {
		if (myIsInitialized) {
			return;
		}
		myIsInitialized = true;

		for (final ExternalFormatPlugin plugin : PluginCollection.Instance().getExternalPlugins()) {
			createConnection(plugin);
		}
	}

	private void createConnection(final ExternalFormatPlugin plugin) {
		final ServiceConnection connection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				Readers.put(plugin, CoverReader.Stub.asInterface(binder));
			}

			public void onServiceDisconnected(ComponentName className) {
				Readers.remove(plugin);
			}
		};
		myConnections.add(connection);
		myContext.bindService(
			PluginUtil.createIntent(plugin, PluginUtil.ACTION_CONNECT_COVER_SERVICE),
			connection,
			Context.BIND_AUTO_CREATE
		);
	}
}
