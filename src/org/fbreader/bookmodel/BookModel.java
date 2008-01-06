package org.fbreader.bookmodel;

import java.util.HashMap;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.image.ZLImageMap;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.impl.ZLTextPlainModelImpl;

public final class BookModel {
	private final ZLTextPlainModelImpl myBookTextModel = new ZLTextPlainModelImpl(65536);
	private final ContentsModel myContentsModel = new ContentsModel();
	private final HashMap<String,ZLTextPlainModelImpl> myFootnotes = new HashMap<String,ZLTextPlainModelImpl>();
	private final HashMap<String,Label> myInternalHyperlinks = new HashMap<String,Label>();

	private static final class ImageMap extends HashMap<String,ZLImage> implements ZLImageMap {
		public ZLImage getImage(String id) {
			return get(id);
		}
	};
	private final ImageMap myImageMap = new ImageMap(); 
	
	private final String myFileName;

	private class Label {
		final int ParagraphNumber;
		final ZLTextModel Model;
		
		Label(ZLTextModel model, int paragraphNumber) {
			ParagraphNumber = paragraphNumber;
			Model = model;
		}
	}
	
	public BookModel(String fileName) {
		myFileName = fileName;
	}

	public String getFileName() {
		return myFileName;
	}

	public ZLTextPlainModelImpl getBookTextModel() {
		return myBookTextModel;
	}
	
	public ContentsModel getContentsModel() {
		return myContentsModel;
	}
	
	public ZLTextPlainModelImpl getFootnoteModel(String id) {
		final HashMap<String,ZLTextPlainModelImpl> footnotes = myFootnotes;
		ZLTextPlainModelImpl model = footnotes.get(id);
		if (model == null) {
			model = new ZLTextPlainModelImpl(4096); 
			footnotes.put(id, model); 
		}
		return model;
	}
	
	void addHyperlinkLabel(String label, ZLTextModel model, int paragraphNumber) {
		myInternalHyperlinks.put(label, new Label(model, paragraphNumber));
	}
	
	//tmp	
	public ZLTextParagraph getParagraphByLink(String link) {
		Label label = myInternalHyperlinks.get(link);
		if (label != null) {
			return label.Model.getParagraph(label.ParagraphNumber);
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
