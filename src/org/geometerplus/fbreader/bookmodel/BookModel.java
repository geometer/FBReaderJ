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

package org.geometerplus.fbreader.bookmodel;

import java.util.*;

import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.*;
import org.geometerplus.fbreader.Paths;

public final class BookModel {
	public static BookModel createModel(Book book) {
		FormatPlugin plugin = PluginCollection.Instance().getPlugin(book.File);
		if (plugin == null) {
			return null;
		}
		BookModel model = new BookModel(book);
		if (plugin.readModel(model)) {
			return model;
		}
		return null;
	}

	private final ZLImageMap myImageMap = new ZLImageMap(); 
	
	public final Book Book;
	public final ZLTextModel BookTextModel;
	public final TOCTree TOCTree = new TOCTree();

	private final HashMap<String,ZLTextModel> myFootnotes = new HashMap<String,ZLTextModel>();

	public static final class Label {
		public final String ModelId;
		public final int ParagraphIndex;
		
		public Label(String modelId, int paragraphIndex) {
			ModelId = modelId;
			ParagraphIndex = paragraphIndex;
		}
	}
	
	//private static String linksFileName(int index) {
	//	return Constants.CACHE_DIRECTORY + "/links" + index + ".cache";
	//}

	private BookModel(Book book) {
		Book = book;
		BookTextModel = new ZLTextWritablePlainModel(null, book.getLanguage(), 1024, 65536, Paths.cacheDirectory(), "cache", myImageMap);
		//for (int i = 0; i < 50; ++i) {
		//	new File(linksFileName(i)).delete();
		//}
	}

	public ZLTextModel getFootnoteModel(String id) {
		ZLTextModel model = myFootnotes.get(id);
		if (model == null) {
			model = new ZLTextWritablePlainModel(id, Book.getLanguage(), 8, 512, Paths.cacheDirectory(), "cache" + myFootnotes.size(), myImageMap); 
			myFootnotes.put(id, model); 
		}
		return model;
	}
	
	private final CharStorage myInternalHyperlinks = new CachedCharStorage(32768, Paths.cacheDirectory(), "links");
	private char[] myCurrentLinkBlock;
	private int myCurrentLinkBlockOffset;

	void addHyperlinkLabel(String label, ZLTextModel model, int paragraphNumber) {
		final String modelId = model.getId();
		final int labelLength = label.length();
		final int idLength = (modelId != null) ? modelId.length() : 0;
		final int len = 4 + labelLength + idLength;

		char[] block = myCurrentLinkBlock;
		int offset = myCurrentLinkBlockOffset;
		if ((block == null) || (offset + len > block.length)) {
			if (block != null) {
				myInternalHyperlinks.freezeLastBlock();
			}
			block = myInternalHyperlinks.createNewBlock(len);
			myCurrentLinkBlock = block;
			offset = 0;
		}
		block[offset++] = (char)labelLength;
		label.getChars(0, labelLength, block, offset);
		offset += labelLength;
		block[offset++] = (char)idLength;
		if (idLength > 0) {
			modelId.getChars(0, idLength, block, offset);
			offset += idLength;
		}
		block[offset++] = (char)(paragraphNumber >> 16);
		block[offset++] = (char)paragraphNumber;
		myCurrentLinkBlockOffset = offset;
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
				if ((labelLength != len) || !id.equals(new String(block, offset, labelLength))) {
					offset += labelLength + idLength + 3;
					continue;
				}
				offset += labelLength + 1;
				final String modelId = (idLength > 0) ? new String(block, offset, idLength) : null;
				offset += idLength;
				final int paragraphNumber = (((int)block[offset++]) << 16) + (int)block[offset];
				return new Label(modelId, paragraphNumber);
			}
		}
		return null;
	}
	
	void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}
}
