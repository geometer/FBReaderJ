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

import org.geometerplus.fbreader.formats.ExternalFormatPlugin;
import org.geometerplus.fbreader.formats.PluginImage;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.formatPlugin.CoverReader;

public class AndroidImageSynchronizer implements ZLImageProxy.Synchronizer {
	private static final class Connection implements ServiceConnection {
		private final ExternalFormatPlugin myPlugin;
		private volatile CoverReader Reader;
		private final List<Runnable> myPostActions = new LinkedList<Runnable>();

		Connection(ExternalFormatPlugin plugin) {
			myPlugin = plugin;
		}

		synchronized void runOrAddAction(Runnable action) {
			if (Reader != null) {
				action.run();
			} else {
				myPostActions.add(action);
			}
		}

		public synchronized void onServiceConnected(ComponentName className, IBinder binder) {
			Reader = CoverReader.Stub.asInterface(binder);
			for (Runnable action : myPostActions) {
				try {
					action.run();
				} catch (Throwable t) {
				}
			}
			myPostActions.clear();
		}

		public synchronized void onServiceDisconnected(ComponentName className) {
			Reader = null;
		}
	}

	private final Context myContext;
	private final Map<ExternalFormatPlugin,Connection> myConnections =
		new HashMap<ExternalFormatPlugin,Connection>();

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
	public void synchronize(ZLImageProxy image, final Runnable postAction) {
		if (image.isSynchronized()) {
			// TODO: also check if image is under synchronization
			if (postAction != null) {
				postAction.run();
			}
		} else if (image instanceof ZLImageSelfSynchronizableProxy) {
			((ZLImageSelfSynchronizableProxy)image).synchronize();
			if (postAction != null) {
				postAction.run();
			}
		} else if (image instanceof PluginImage) {
			final PluginImage pluginImage = (PluginImage)image;
			final Connection connection = getConnection(pluginImage.Plugin);
			connection.runOrAddAction(new Runnable() {
				public void run() {
					try {
						pluginImage.setRealImage(new ZLBitmapImage(connection.Reader.readBitmap(pluginImage.File.getPath(), Integer.MAX_VALUE, Integer.MAX_VALUE)));
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (postAction != null) {
						postAction.run();
					}
				}
			});
		} else {
			throw new RuntimeException("Cannot synchronize " + image.getClass());
		}
	}

	public synchronized void clear() {
		for (ServiceConnection connection : myConnections.values()) {
			myContext.unbindService(connection);
		}
		myConnections.clear();
	}

	private synchronized Connection getConnection(ExternalFormatPlugin plugin) {
		Connection connection = myConnections.get(plugin);
		if (connection == null) {
			connection = new Connection(plugin);
			myConnections.put(plugin, connection);
			myContext.bindService(
				PluginUtil.createIntent(plugin, PluginUtil.ACTION_CONNECT_COVER_SERVICE),
				connection,
				Context.BIND_AUTO_CREATE
			);
		}
		return connection;
	}
}
