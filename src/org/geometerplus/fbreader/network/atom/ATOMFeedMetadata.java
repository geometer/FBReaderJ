/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.network.atom;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class ATOMFeedMetadata extends ATOMCommonAttributes {
	public ATOMId Id;

	public LinkedList<ATOMAuthor> Authors = new LinkedList<ATOMAuthor>();
	public LinkedList<ATOMCategory> Categories = new LinkedList<ATOMCategory>();
	//public LinkedList<ATOMContributor> Contributors = new LinkedList<ATOMContributor>();
	//public ATOMGenerator Generator;
	public ATOMIcon Icon;
	public LinkedList<ATOMLink> Links = new LinkedList<ATOMLink>();
	//public ATOMLogo Logo;
	//public String Rights;   // TODO: implement ATOMTextConstruct
	public CharSequence Subtitle; // TODO: implement ATOMTextConstruct
	public CharSequence Title;    // TODO: implement ATOMTextConstruct
	public ATOMUpdated Updated;

	protected ATOMFeedMetadata(ZLStringMap source) {
		super(source);
	}
}
