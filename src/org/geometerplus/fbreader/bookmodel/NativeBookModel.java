/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.library.Book;

public class NativeBookModel extends BookModelImpl {
	private ZLTextModel myBookTextModel;

	NativeBookModel(Book book) {
		super(book);
	}

	public void initImageMap(
		String[] ids, int[] indices, int[] offsets,
		String directoryName, String fileExtension, int blocksNumber
	) {
		myImageMap = new ZLCachedImageMap(
			ids, indices, offsets, directoryName, fileExtension, blocksNumber
		);
	}

	public void initInternalHyperlinks(String directoryName, String fileExtension, int blocksNumber) {
		myInternalHyperlinks = new CachedCharStorageRO(directoryName, fileExtension, blocksNumber);
	}

	public void initTOC(ZLTextModel contentsModel, int[] childrenNumbers, int[] referenceNumbers) {
		final StringBuilder buffer = new StringBuilder();

		final ArrayList<Integer> positions = new ArrayList<Integer>();
		TOCTree tree = TOCTree;

		final int size = contentsModel.getParagraphsNumber();
		for (int pos = 0; pos < size; ++pos) {
			positions.add(pos);
			ZLTextParagraph par = contentsModel.getParagraph(pos);

			buffer.delete(0, buffer.length());
			ZLTextParagraph.EntryIterator it = par.iterator();
			while (it.hasNext()) {
				it.next();
				if (it.getType() == ZLTextParagraph.Entry.TEXT) {
					buffer.append(it.getTextData(), it.getTextOffset(), it.getTextLength());
				}
			}

			tree = new TOCTree(tree);
			tree.setText(buffer.toString());
			tree.setReference(myBookTextModel, referenceNumbers[pos]);

			while (positions.size() > 0 && tree != TOCTree) {
				final int lastIndex = positions.size() - 1;
				final int treePos = positions.get(lastIndex);
				if (tree.subTrees().size() < childrenNumbers[treePos]) {
					break;
				}
				tree = tree.Parent;
				positions.remove(lastIndex);
			}
		}

		if (tree != TOCTree || positions.size() > 0) {
			throw new RuntimeException("Invalid state after TOC building:\n"
				+ "tree.Level = " + tree.Level + "\n"
				+ "positions.size() = " + positions.size());
		}
	}

	public ZLTextModel createTextModel(
		String id, String language, int paragraphsNumber,
		int[] entryIndices, int[] entryOffsets,
		int[] paragraphLenghts, int[] textSizes, byte[] paragraphKinds,
		String directoryName, String fileExtension, int blocksNumber
	) {
		if (myImageMap == null) {
			throw new RuntimeException("NativeBookModel should be initialized with initImageMap method");
		}
		return new ZLTextNativeModel(
			id, language, paragraphsNumber,
			entryIndices, entryOffsets,
			paragraphLenghts, textSizes, paragraphKinds,
			directoryName, fileExtension, blocksNumber, myImageMap
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
