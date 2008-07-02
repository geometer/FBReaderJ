/*
 * Copyright (C) 2004-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;

import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;

class Reference {
	public final String Title;
	public final String HRef;

	public Reference(String title, String href) {
		Title = title;
		HRef = href;
	}
}

class OEBBookReader extends ZLXMLReaderAdapter {
	private final BookReader myModelReader;
	private final HashMap myIdToHref = new HashMap();
	private final ArrayList myHtmlFileNames = new ArrayList();
	private final ArrayList myTourTOC = new ArrayList();
	private final ArrayList myGuideTOC = new ArrayList();

	private String myFilePrefix;

	OEBBookReader(BookModel model) {
		myModelReader = new BookReader(model);
	}

	private static String htmlDirectoryPrefix(String fileName) {
		ZLFile file = new ZLFile(fileName);
		String shortName = file.getName(false);
		String path = file.getPath();
		int index = -1;
		if ((path.length() > shortName.length()) &&
				(path.charAt(path.length() - shortName.length() - 1) == ':')) {
			index = shortName.lastIndexOf('/');
		}
		return path.substring(0, path.length() - shortName.length() + index + 1);
	}
	
	boolean readBook(String fileName) {
		myFilePrefix = htmlDirectoryPrefix(fileName);

		myIdToHref.clear();
		myHtmlFileNames.clear();
		myTourTOC.clear();
		myGuideTOC.clear();
		myState = READ_NONE;

		if (!read(fileName)) {
			return false;
		}

		myModelReader.setMainTextModel();
		myModelReader.pushKind(FBTextKind.REGULAR);

		final int len = myHtmlFileNames.size();
		for (int i = 0; i < len; ++i) {
			final String name = (String)myHtmlFileNames.get(i);
			new XHTMLReader(myModelReader).readFile(myFilePrefix, name, name);
		}

		final ArrayList toc = myTourTOC.isEmpty() ? myGuideTOC : myTourTOC;
		final int tocLen = toc.size();
		for (int i = 0; i < tocLen; ++i) {
			final Reference ref = (Reference)toc.get(i);
			final BookModel.Label label = myModelReader.Model.getLabel(ref.HRef);
			if (label != null) {
				final int index = label.ParagraphIndex;
				if (index != -1) {
					myModelReader.beginContentsParagraph(index);
					myModelReader.addContentsData(ref.Title.toCharArray());
					myModelReader.endContentsParagraph();
				}
			}
		}

		return true;
	}

	private static final String MANIFEST = "manifest";
	private static final String SPINE = "spine";
	private static final String GUIDE = "guide";
	private static final String TOUR = "tour";
	private static final String SITE = "site";
	private static final String REFERENCE = "reference";
	private static final String ITEMREF = "itemref";
	private static final String ITEM = "item";

	private static final String COVER_IMAGE = "other.ms-coverimage-standard";

	private static final int READ_NONE = 0;
	private static final int READ_MANIFEST = 1;
	private static final int READ_SPINE = 2;
	private static final int READ_GUIDE = 3;
	private static final int READ_TOUR = 4;
	
	private int myState;

	public boolean startElementHandler(String tag, ZLStringMap xmlattributes) {
		tag = tag.toLowerCase().intern();
		if (MANIFEST == tag) {
			myState = READ_MANIFEST;
		} else if (SPINE == tag) {
			myState = READ_SPINE;
		} else if (GUIDE == tag) {
			myState = READ_GUIDE;
		} else if (TOUR == tag) {
			myState = READ_TOUR;
		} else if ((myState == READ_MANIFEST) && (ITEM == tag)) {
			final String id = xmlattributes.getValue("id");
			final String href = xmlattributes.getValue("href");
			if ((id != null) && (href != null)) {
				myIdToHref.put(id, href);
			}
		} else if ((myState == READ_SPINE) && (ITEMREF == tag)) {
			final String id = xmlattributes.getValue("idref");
			if (id != null) {
				final String fileName = (String)myIdToHref.get(id);
				if (fileName != null) {
					myHtmlFileNames.add(fileName);
				}
			}
		} else if ((myState == READ_GUIDE) && (REFERENCE == tag)) {
			final String type = xmlattributes.getValue("type");
			final String title = xmlattributes.getValue("title");
			final String href = xmlattributes.getValue("href");
			if (href != null) {
				if (title != null) {
					myGuideTOC.add(new Reference(title, href));
				}
				if ((type != null) && (COVER_IMAGE.equals(type))) {
					myModelReader.setMainTextModel();
					myModelReader.addImageReference(href, (short)0);
					myModelReader.addImage(href, new ZLFileImage("image/auto", myFilePrefix + href));
				}
			}
		} else if ((myState == READ_TOUR) && (SITE == tag)) {
			final String title = xmlattributes.getValue("title");
			final String href = xmlattributes.getValue("href");
			if ((title != null) && (href != null)) {
				myTourTOC.add(new Reference(title, href));
			}
		}
		return false;
	}

	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase().intern();
		if ((MANIFEST == tag) || (SPINE == tag) || (GUIDE == tag) || (TOUR == tag)) {
			myState = READ_NONE;
		}
		return false;
	}
}
