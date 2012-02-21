/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

public abstract class BookModel {
	public static BookModel createModel(Book book) {
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(book.File);
		if (plugin == null) {
			return null;
		}

		final BookModel model;
		if (plugin.type() == FormatPlugin.Type.NATIVE) {
			model = new NativeBookModel(book);
		} else {
			model = new JavaBookModel(book);
		}

		if (plugin.readModel(model)) {
			return model;
		}
		return null;
	}

	protected final ZLImageMap myImageMap = new ZLImageMap();

	public final Book Book;
	public final TOCTree TOCTree = new TOCTree();

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

	public abstract ZLTextModel getTextModel();
	public abstract ZLTextModel getFootnoteModel(String id);

	protected final CharStorage myInternalHyperlinks = new CachedCharStorage(32768, Paths.cacheDirectory(), "links");
	protected char[] myCurrentLinkBlock;
	protected int myCurrentLinkBlockOffset;

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
				final int paragraphNumber = (int)block[offset] + (((int)block[offset + 1]) << 16);
				return new Label(modelId, paragraphNumber);
			}
		}
		return null;
	}
}
