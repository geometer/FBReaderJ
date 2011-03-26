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

import org.geometerplus.fbreader.bookmodel.*;

class XHTMLTagParagraphWithControlAction extends XHTMLTagAction {
	final byte myControl;

	XHTMLTagParagraphWithControlAction(byte control) {
		myControl = control;
	}

	protected void doAtStart(XHTMLReader reader, ZLStringMap xmlattributes) {
		final BookReader modelReader = reader.getModelReader();
		switch (myControl) {
			case FBTextKind.TITLE:
			case FBTextKind.H1:
			case FBTextKind.H2:
				if (modelReader.Model.BookTextModel.getParagraphsNumber() > 1) {
					modelReader.insertEndOfSectionParagraph();
				}
				modelReader.enterTitle();
				break;
		}
		modelReader.pushKind(myControl);
		modelReader.beginParagraph();
	}

	protected void doAtEnd(XHTMLReader reader) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.endParagraph();
		modelReader.popKind();
		switch (myControl) {
			case FBTextKind.TITLE:
			case FBTextKind.H1:
			case FBTextKind.H2:
				modelReader.exitTitle();
				break;
		}
	}
}
/*
void XHTMLTagParagraphWithControlAction::doAtStart(XHTMLReader &reader, const char**) {
	if ((myControl == TITLE) && (bookReader(reader).model().bookTextModel()->paragraphsNumber() > 1)) {
		bookReader(reader).insertEndOfSectionParagraph();
	}
	bookReader(reader).pushKind(myControl);
	bookReader(reader).beginParagraph();
}
*/
