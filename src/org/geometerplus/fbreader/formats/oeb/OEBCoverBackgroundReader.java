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

package org.geometerplus.fbreader.formats.oeb;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.formats.util.MiscUtil;

class OEBCoverBackgroundReader extends ZLXMLReaderAdapter implements XMLNamespaces {
	private ZLFileImage myImage;
	private String myPathPrefix;

	private static final int READ_NOTHING = 0;
	private static final int READ_METADATA = 1;
	private static final int READ_MANIFEST = 2;
	private static final int READ_GUIDE = 3;
	private int myReadState = READ_NOTHING;

	private String myCoverId;

	public ZLFileImage readCover(ZLFile file) {
		myPathPrefix = MiscUtil.htmlDirectoryPrefix(file);
		myReadState = READ_NOTHING;
		myCoverId = null;
		myImage = null;
		readQuietly(file);
		return myImage;
	}

	private static final String GUIDE = "guide";
	private static final String MANIFEST = "manifest";

	@Override
	public boolean processNamespaces() {
		return true;
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase();
		switch (myReadState) {
			case READ_NOTHING:
				if (GUIDE.equals(tag)) {
					myReadState = READ_GUIDE;
				} else if (MANIFEST.equals(tag) && myCoverId != null) {
					myReadState = READ_MANIFEST;
				} else if (testTag(OpenPackagingFormat, "metadata", tag)) {
					myReadState = READ_METADATA;
				}
				break;
			case READ_GUIDE:
				if ("reference".equals(tag)) {
					final String type = attributes.getValue("type");
					if ("cover" == type) {
						final String href = attributes.getValue("href");
						if (href != null) {
							final ZLFile coverFile = ZLFile.createFileByPath(
								myPathPrefix + MiscUtil.decodeHtmlReference(href)
							);
							myImage = XHTMLImageFinder.getCoverImage(coverFile);
							return true;
						}
					} else if ("other.ms-coverimage-standard".equals(type)) {
						myImage = imageByHref(attributes.getValue("href"));
						if (myImage != null) {
							return true;
						}
					}
				}
				break;
			case READ_METADATA:
				if (testTag(OpenPackagingFormat, "meta", tag)) {
					final String name = attributes.getValue("name");
					if ("cover".equals(name)) {
						myCoverId = attributes.getValue("content");
					}
				}
				break;
			case READ_MANIFEST:
				if ("item".equals(tag) && myCoverId.equals(attributes.getValue("id"))) {
					myImage = imageByHref(attributes.getValue("href"));
					if (myImage != null) {
						return true;
					}
				}
				break;
		}
		return false;
	}

	private ZLFileImage imageByHref(String href) {
		if (href == null) {
			return null;
		}
		return new ZLFileImage(
			MimeType.IMAGE_AUTO,
			ZLFile.createFileByPath(myPathPrefix + MiscUtil.decodeHtmlReference(href))
		);
	}

	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase();
		switch (myReadState) {
			case READ_GUIDE:
				if (GUIDE.equals(tag)) {
					myReadState = READ_NOTHING;
				}
				break;
			case READ_MANIFEST:
				if (MANIFEST.equals(tag)) {
					myReadState = READ_NOTHING;
				}
				break;
			case READ_METADATA:
				if (testTag(OpenPackagingFormat, "metadata", tag)) {
					myReadState = READ_NOTHING;
				}
				break;
		}
		return false;
	}
}
