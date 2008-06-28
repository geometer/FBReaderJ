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

package org.geometerplus.fbreader.bookmodel;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.formats.*;

public final class BookModel {
	public final BookDescription Description;
	public final ZLTextPlainModel BookTextModel = new ZLTextPlainModel(65536);
	public final ContentsModel ContentsModel = new ContentsModel();

	private final HashMap myFootnotes = new HashMap();
	private final HashMap myInternalHyperlinks = new HashMap();
	private final ArrayList myBookTextModels;

	private final ZLImageMap myImageMap = new ZLImageMap(); 
	
	public class Label {
		public final int ParagraphIndex;
		public final ZLTextModel Model;
		
		public final int ModelIndex;
		
		Label(ZLTextModel model, int paragraphIndex) {
			ParagraphIndex = paragraphIndex;
			Model = model;
			ModelIndex = myBookTextModels.indexOf(model);
		}
	}
	
	public BookModel(final BookDescription description) {
		myBookTextModels = new ArrayList();
		myBookTextModels.add(BookTextModel);
		Description = description;
		ZLFile file = new ZLFile(description.FileName);
		FormatPlugin plugin = PluginCollection.instance().getPlugin(file, false);
		if (plugin != null) {
			plugin.readModel(description, this);
		}
	}

	ZLTextPlainModel getFootnoteModel(String id) {
		final HashMap footnotes = myFootnotes;
		ZLTextPlainModel model = (ZLTextPlainModel)footnotes.get(id);
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
	
	//
	public ZLTextPlainModel addBookTextModel() {
		ZLTextPlainModel bookTextModel = new ZLTextPlainModel(65536);
		myBookTextModels.add(bookTextModel);
		return bookTextModel;
	}
	
	public ArrayList getBookTextModels() {
		return myBookTextModels;
	}
}
