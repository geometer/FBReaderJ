/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network;

import java.util.*;

/**
 * Mutable class that serves as a buffer stores network operation results.
 */
public class NetworkOperationData {

	public final NetworkLink Link;
	public LinkedList<NetworkLibraryItem> Items = new LinkedList<NetworkLibraryItem>();
	public String ResumeURI;
	public int ResumeCount;

	public NetworkOperationData(NetworkLink link) {
		Link = link;
	}

	public void clear() {
		Items.clear();
		ResumeURI = null;
	}
}
