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

public class ATOMEntry extends ATOMCommonAttributes {
	public ATOMId Id;

	public LinkedList<ATOMAuthor> Authors = new LinkedList<ATOMAuthor>();
	public LinkedList<ATOMCategory> Categories = new LinkedList<ATOMCategory>();
	public LinkedList<ATOMContributor> Contributors = new LinkedList<ATOMContributor>();
	public LinkedList<ATOMLink> Links = new LinkedList<ATOMLink>();
	public ATOMPublished Published;
	//public String Rights;  // TODO: implement ATOMTextConstruct
	//public final ATOMSource Source; // TODO: implement ATOMSource
	public CharSequence Summary; // TODO: implement ATOMTextConstruct
	public CharSequence Content; // TODO: implement ATOMContent
	public CharSequence Title;   // TODO: implement ATOMTextConstruct
	public ATOMUpdated Updated;

	protected ATOMEntry(ZLStringMap source) {
		super(source);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("[")
			.append(super.toString())
			.append(",\nId=").append(Id)
			.append(",\nAuthors:[\n");

		boolean first = true;
		for (ATOMAuthor author : Authors) {
			if (!first) buf.append(",\n");
			first = false;
			buf.append(author.toString());
		}
		buf.append("],\nCategories:[\n");
		first = true;
		for (ATOMCategory category : Categories) {
			if (!first) buf.append(",\n");
			first = false;
			buf.append(category.toString());
		}
		buf.append("],\nLinks:[\n");
		first = true;
		for (ATOMLink link : Links) {
			if (!first) buf.append(",\n");
			first = false;
			buf.append(link.toString());
		}
		return buf
			.append("]")
			.append(",\nPublished=").append(Published)
			//.append(",\nRights=").append(Rights)
			.append(",\nSummary=").append(Summary)
			.append(",\nTitle=").append(Title)
			.append(",\nUpdated=").append(Updated)
			.append("]")
			.toString();
	}
}
