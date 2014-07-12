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

import java.util.LinkedList;
import java.util.List;

import android.content.*;

import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.formats.external.ExternalFormatPlugin;

public final class PluginConnectionPool {
	private final Context myContext;
	private final List<ServiceConnection> myConnections = new LinkedList<ServiceConnection>();

	public PluginConnectionPool(Context context) {
		myContext = context;
	}

	public synchronized void clear() {
		for (ServiceConnection connection : myConnections) {
			myContext.unbindService(connection);
		}
		myConnections.clear();
	}

	public synchronized PluginMetainfoReaderImpl createMetainfoReader() {
		final PluginMetainfoReaderImpl reader = new PluginMetainfoReaderImpl();
		for (final ExternalFormatPlugin plugin : PluginCollection.Instance().getExternalPlugins()) {
			final ServiceConnection connection = reader.createConnection(plugin);
			myConnections.add(connection);
			final Intent i = PluginUtil.createIntent(plugin, PluginUtil.ACTION_CONNECT_COVER_SERVICE);
			myContext.bindService(i, connection, Context.BIND_AUTO_CREATE);
		}
		return reader;
	}
}
