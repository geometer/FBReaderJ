/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.Arrays;
import java.util.List;

import org.geometerplus.zlibrary.core.fonts.*;
import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.formats.BuiltinFormatPlugin;
import org.geometerplus.fbreader.formats.FormatPlugin;

public abstract class BookModel {
	public static BookModel createModel(Book book) throws BookReadingException {
		final FormatPlugin plugin = book.getPlugin();

		System.err.println("using plugin: " + plugin.supportedFileType() + "/" + plugin.type());

		if (plugin instanceof BuiltinFormatPlugin) {
			final BookModel model = new NativeBookModel(book);
			((BuiltinFormatPlugin)plugin).readModel(model);
			return model;
		}

		throw new BookReadingException(
			"unknownPluginType", null, new String[] { plugin.type().toString() }
		);
	}

	public final Book Book;
	public final TOCTree TOCTree = new TOCTree();
	public final FontManager FontManager = new FontManager();

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
	protected abstract Label getLabelInternal(String id);

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
}
