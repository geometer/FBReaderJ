/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.xhtml;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

import org.geometerplus.fbreader.bookmodel.BookReader;

class XHTMLTagItemAction extends XHTMLTagAction {
	private final char[] BULLET = { '\u2022', '\240' };

	protected void doAtStart(XHTMLReader reader, ZLStringMap xmlattributes) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.endParagraph();
		// TODO: increase left indent
		modelReader.beginParagraph();
		// TODO: replace bullet sign by number inside OL tag
		modelReader.addData(BULLET);
	}

	protected void doAtEnd(XHTMLReader reader) {
		reader.getModelReader().endParagraph();
	}
}
