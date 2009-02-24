/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.xmlconfig;

import org.geometerplus.zlibrary.core.config.*;

public class ZLXMLConfigManager extends ZLConfigManager {
	private String myDirectoryPath;

	// TODO: remove this constructor
	public ZLXMLConfigManager(String inputPath, String outputPath) {
		myDirectoryPath = outputPath;
		ZLConfigImpl config = new ZLConfigImpl();
		if (inputPath != null) {
			new ZLConfigReader(config, inputPath).read();
		}
		setConfig(config);
	}

	public ZLXMLConfigManager(String directoryPath) {
		this(directoryPath, directoryPath);
	}

	protected void shutdown() {
		saveAll();
	}

	public void saveAll() {
		if (myDirectoryPath != null) {
			new ZLConfigWriter((ZLConfigImpl)getConfig(), myDirectoryPath).write();
		}
	}

	public void saveDelta() {
		if (myDirectoryPath != null) {
			new ZLConfigWriter((ZLConfigImpl)getConfig(), myDirectoryPath).writeDelta();
		}
	}
}
