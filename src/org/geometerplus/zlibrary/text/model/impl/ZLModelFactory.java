/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.text.model.impl;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.ZLTextPlainModel;
import org.geometerplus.zlibrary.text.model.ZLTextTreeModel;
import org.geometerplus.zlibrary.text.model.ZLTextTreeParagraph;


public class ZLModelFactory {
	//models
	static public ZLTextPlainModel createPlainModel(int dataBlockSize) {
		return new ZLTextPlainModelImpl(dataBlockSize);
	}
	
	/*static public ZLTextPlainModel createPlainModel() {
		return new ZLTextPlainModelImpl();
	}*/
	
	static public ZLTextTreeModel createZLTextTreeModel() {
		return new ZLTextTreeModelImpl();
	}

	//paragraphs
	static public ZLTextParagraph createParagraph() {
		return new ZLTextParagraphImpl(new ZLTextPlainModelImpl(4096));
	}
	
	static public ZLTextParagraph createSpecialParagragraph(byte kind) {
		return new ZLTextSpecialParagraphImpl(kind, new ZLTextPlainModelImpl(4096));
	}
	
	static public ZLTextTreeParagraph createTreeParagraph(ZLTextTreeParagraph parent) {
		return new ZLTextTreeParagraphImpl(parent, new ZLTextPlainModelImpl(4096));
	}
	
	static public ZLTextTreeParagraph createTreeParagraph() {
		return new ZLTextTreeParagraphImpl(null, new ZLTextPlainModelImpl(4096));
	}

	//entries
	static public ZLTextForcedControlEntry createForcedControlEntry() {
		return new ZLTextForcedControlEntryImpl();
	}
}
