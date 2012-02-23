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

import java.util.List;
import java.util.Collections;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.*;

public abstract class BookModel {
	public static BookModel createModel(Book book) {
		final FormatPlugin plugin = PluginCollection.Instance().getPlugin(book.File);
		if (plugin == null) {
			return null;
		}

		System.err.println("using plugin: " + plugin.supportedFileType() + "/" + plugin.type());

		final BookModel model;
		switch (plugin.type()) {
			case NATIVE:
				model = new NativeBookModel(book);
				// TODO: a hack; should be moved into plugin code
				if ("epub".equalsIgnoreCase(book.File.getExtension())) {
					model.setLabelResolver(new LabelResolver() {
						public List<String> getCandidates(String id) {
							final int index = id.indexOf("#");
							return index > 0
								? Collections.<String>singletonList(id.substring(0, index))
								: Collections.<String>emptyList();
						}
					});
				}
				break;
			case JAVA:
				model = new JavaBookModel(book);
				break;
			default:
				return null;
		}

		if (plugin.readModel(model)) {
			return model;
		}
		return null;
	}

	public final Book Book;
	public final TOCTree TOCTree = new TOCTree();

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
}
