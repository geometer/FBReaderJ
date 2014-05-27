/*
 * Copyright (C) 2011-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.bookmodel;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.book.Book;

public class NativeBookModel extends BookModelImpl {
	private ZLTextModel myBookTextModel;

	NativeBookModel(Book book) {
		super(book);
	}

	public void initInternalHyperlinks(String directoryName, String fileExtension, int blocksNumber) {
		myInternalHyperlinks = new CachedCharStorageRO(directoryName, fileExtension, blocksNumber);
	}

	private TOCTree myCurrentTree = TOCTree;

	public void addTOCItem(String text, int reference) {
		myCurrentTree = new TOCTree(myCurrentTree);
		myCurrentTree.setText(text);
		myCurrentTree.setReference(myBookTextModel, reference);
	}

	public void leaveTOCItem() {
		myCurrentTree = myCurrentTree.Parent;
		if (myCurrentTree == null) {
			myCurrentTree = TOCTree;
		}
	}

	public ZLTextModel createTextModel(
		String id, String language, int paragraphsNumber,
		int[] entryIndices, int[] entryOffsets,
		int[] paragraphLenghts, int[] textSizes, byte[] paragraphKinds,
		String directoryName, String fileExtension, int blocksNumber
	) {
		return new ZLTextNativeModel(
			id, language, paragraphsNumber,
			entryIndices, entryOffsets,
			paragraphLenghts, textSizes, paragraphKinds,
			directoryName, fileExtension, blocksNumber, myImageMap, FontManager
		);
	}

	public void setBookTextModel(ZLTextModel model) {
		myBookTextModel = model;
	}

	public void setFootnoteModel(ZLTextModel model) {
		myFootnotes.put(model.getId(), model);
	}

	@Override
	public ZLTextModel getTextModel() {
		return myBookTextModel;
	}

	@Override
	public ZLTextModel getFootnoteModel(String id) {
		return myFootnotes.get(id);
	}
}
