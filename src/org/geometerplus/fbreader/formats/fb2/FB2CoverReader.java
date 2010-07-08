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

package org.geometerplus.fbreader.formats.fb2;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.constants.XMLNamespace;


public class FB2CoverReader extends ZLXMLReaderAdapter {

	private boolean myReadCoverPage;
	private String myImageReference;
	private Base64EncodedImage myImage;


	public ZLImage readCover(ZLFile file) {
		myReadCoverPage = false;
		myImageReference = null;
		read(file);
		return myImage;
	}


	private String myXLinkPrefix;

	@Override
	public boolean processNamespaces() {
		return true;
	}

	public void namespaceMapChangedHandler(HashMap<String,String> namespaceMap) {
		myXLinkPrefix = null;
		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			if (XMLNamespace.XLink.equals(entry.getValue())) {
				myXLinkPrefix = entry.getKey() + ":";
				break;
			}
		}
	}


	@Override
	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		switch (FB2Tag.getTagByName(tagName)) {
		case FB2Tag.COVERPAGE:
			myReadCoverPage = true;
			break;
		case FB2Tag.IMAGE:
			if (myReadCoverPage) {
				if (myXLinkPrefix == null) {
					break;
				}
				final String href = attributes.getValue((myXLinkPrefix + "href").intern());
				if (href != null && href.length() > 1 && href.charAt(0) == '#') {
					myImageReference = href.substring(1);
				}
			}
			break;
		case FB2Tag.BINARY:
			if (myImageReference != null) {
				final String id = attributes.getValue("id");
				final String contentType = attributes.getValue("content-type");
				if (id != null && contentType != null && myImageReference.equals(id)) {
					// FIXME: make different Base64EncodedImage constructor to use another cache for covers
					myImage = new Base64EncodedImage((contentType != null) ? contentType : "image/auto");
				}
			}
			break;
		}
		return false;
	}

	@Override
	public boolean endElementHandler(String tag) {
		switch (FB2Tag.getTagByName(tag)) {
		case FB2Tag.COVERPAGE:
			myReadCoverPage = false;
			break;
		case FB2Tag.DESCRIPTION:
			if (myImageReference == null) {
				return true;
			}
			break;
		case FB2Tag.BINARY:
			if (myImage != null) {
				myImage.close();
				return true;
			}
			break;
		}	
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int length) {
		if (length > 0 && myImage != null) {
			myImage.addData(data, start, length);
		}
	}
}
