/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.*;

public final class BookModel {
	public final Book Book;
	public final ZLTextPlainModel BookTextModel = new ZLTextPlainModel(65536, "/sdcard/Books/.FBReader", "cache");
	public final TOCTree TOCTree = new TOCTree();

	private final HashMap<String,ZLTextPlainModel> myFootnotes = new HashMap<String,ZLTextPlainModel>();
	private final HashMap myInternalHyperlinks = new HashMap();

	private final ZLImageMap myImageMap = new ZLImageMap(); 
	
	public class Label {
		public final ZLTextModel Model;
		public final int ParagraphIndex;
		
		Label(ZLTextModel model, int paragraphIndex) {
			Model = model;
			ParagraphIndex = paragraphIndex;
		}
	}
	
	public BookModel(final Book book) {
		Book = book;
		FormatPlugin plugin = PluginCollection.instance().getPlugin(book.File);
		if (plugin != null) {
			plugin.readModel(this);
		}
	}

	ZLTextPlainModel getFootnoteModel(String id) {
		final HashMap<String,ZLTextPlainModel> footnotes = myFootnotes;
		ZLTextPlainModel model = footnotes.get(id);
		if (model == null) {
			model = new ZLTextPlainModel(4096); 
			footnotes.put(id, model); 
		}
		return model;
	}
	
	void addHyperlinkLabel(String label, ZLTextModel model, int paragraphNumber) {
		myInternalHyperlinks.put(label, new Label(model, paragraphNumber));
	}

	public Label getLabel(String id) {
		return (Label)myInternalHyperlinks.get(id);
	}
	
	//tmp	
	public ZLTextParagraph getParagraphByLink(String link) {
		Label label = (Label)myInternalHyperlinks.get(link);
		if (label != null) {
			return label.Model.getParagraph(label.ParagraphIndex);
		}
		return null;
	}

	public ZLImageMap getImageMap() {
		return myImageMap;
	}

	void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}
}
