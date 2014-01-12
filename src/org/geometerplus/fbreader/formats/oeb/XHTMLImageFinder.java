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

class XHTMLImageFinder extends ZLXMLReaderAdapter {
	static ZLFileImage getCoverImage(ZLFile coverFile) {
		if (coverFile == null) {
			return null;
		}

		final String ext = coverFile.getExtension();
		if ("gif".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext)) {
			return new ZLFileImage(MimeType.IMAGE_AUTO, coverFile);
		} else {
			return new XHTMLImageFinder().readImage(coverFile);
		}
	}

	private String myXHTMLPathPrefix;
	private ZLFileImage myImage;

	ZLFileImage readImage(ZLFile file) {
		myXHTMLPathPrefix = MiscUtil.htmlDirectoryPrefix(file);
		myImage = null;
		readQuietly(file);
		return myImage;
	}

	@Override
	public boolean processNamespaces() {
		return true;
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase();
		String href = null;
		if ("img".equals(tag)) {
			href = attributes.getValue("src");
		} else if ("image".equals(tag)) {
			href = getAttributeValue(attributes, XMLNamespaces.XLink, "href");
		}

		if (href != null) {
			myImage = new ZLFileImage(
				MimeType.IMAGE_AUTO,
				ZLFile.createFileByPath(myXHTMLPathPrefix + MiscUtil.decodeHtmlReference(href))
			);
			return true;
		}

		return false;
	}
}
