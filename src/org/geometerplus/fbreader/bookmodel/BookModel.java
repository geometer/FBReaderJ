/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import org.geometerplus.zlibrary.core.fonts.*;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.formats.*;

public final class BookModel {
	public static BookModel createModel(Book book, FormatPlugin plugin) throws BookReadingException {
		if (plugin instanceof BuiltinFormatPlugin) {
			final BookModel model = new BookModel(book);
			((BuiltinFormatPlugin)plugin).readModel(model);
			return model;
		}

		throw new BookReadingException(
			"unknownPluginType", null, new String[] { String.valueOf(plugin) }
		);
	}

	public final Book Book;
	public final TOCTree TOCTree = new TOCTree();
	public final FontManager FontManager = new FontManager();

	protected CachedCharStorage myInternalHyperlinks;
	protected final HashMap<String,ZLImage> myImageMap = new HashMap<String,ZLImage>();
	protected ZLTextModel myBookTextModel;
	protected final HashMap<String,ZLTextModel> myFootnotes = new HashMap<String,ZLTextModel>();

	public static final class Label {
		public final String ModelId;
		public final int ParagraphIndex;

		public Label(String modelId, int paragraphIndex) {
			ModelId = modelId;
			ParagraphIndex = paragraphIndex;
		}
	}

	protected BookModel(Book book) {
		Book = book;
	}

	public interface LabelResolver {
		List<String> getCandidates(String id);
	}

	private LabelResolver myResolver;

	public void setLabelResolver(LabelResolver resolver) {
		myResolver = resolver;
	}

	public Label getLabel(String id) {
		Label label = getLabelInternal(id);
		if (label == null && myResolver != null) {
			for (String candidate : myResolver.getCandidates(id)) {
				label = getLabelInternal(candidate);
				if (label != null) {
					break;
				}
			}
		}
		return label;
	}

	public void registerFontFamilyList(String[] families) {
		FontManager.index(Arrays.asList(families));
	}

	public void registerFontEntry(String family, FontEntry entry) {
		FontManager.Entries.put(family, entry);
	}

	public void registerFontEntry(String family, FileInfo normal, FileInfo bold, FileInfo italic, FileInfo boldItalic) {
		registerFontEntry(family, new FontEntry(family, normal, bold, italic, boldItalic));
	}

	public ZLTextModel createTextModel(
		String id, String language, int paragraphsNumber,
		int[] entryIndices, int[] entryOffsets,
		int[] paragraphLenghts, int[] textSizes, byte[] paragraphKinds,
		String directoryName, String fileExtension, int blocksNumber
	) {
		return new ZLTextPlainModel(
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

	public ZLTextModel getTextModel() {
		return myBookTextModel;
	}

	public ZLTextModel getFootnoteModel(String id) {
		return myFootnotes.get(id);
	}

	public void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}

	public void initInternalHyperlinks(String directoryName, String fileExtension, int blocksNumber) {
		myInternalHyperlinks = new CachedCharStorage(directoryName, fileExtension, blocksNumber);
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

	private Label getLabelInternal(String id) {
		final int len = id.length();
		final int size = myInternalHyperlinks.size();

		for (int i = 0; i < size; ++i) {
			final char[] block = myInternalHyperlinks.block(i);
			for (int offset = 0; offset < block.length; ) {
				final int labelLength = (int)block[offset++];
				if (labelLength == 0) {
					break;
				}
				final int idLength = (int)block[offset + labelLength];
				if (labelLength != len || !id.equals(new String(block, offset, labelLength))) {
					offset += labelLength + idLength + 3;
					continue;
				}
				offset += labelLength + 1;
				final String modelId = (idLength > 0) ? new String(block, offset, idLength) : null;
				offset += idLength;
				final int paragraphNumber = (int)block[offset] + (((int)block[offset + 1]) << 16);
				return new Label(modelId, paragraphNumber);
			}
		}
		return null;
	}
}
