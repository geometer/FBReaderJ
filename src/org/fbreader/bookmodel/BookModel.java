package org.fbreader.bookmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.impl.ZLModelFactory;

public class BookModel {
	private final ZLModelFactory myModelFactory = new ZLModelFactory();
	private final ZLTextPlainModel myBookModel = myModelFactory.createPlainModel();
	private final ContentsModel myContentsModel = new ContentsModel();
	private final Map<String,ZLTextPlainModel> myFootnotes = new HashMap<String,ZLTextPlainModel>();
	private final Map<String,Label> myInternalHyperlinks = new HashMap<String,Label>();
	private final Map<String,ZLImage> myImageMap = new HashMap<String,ZLImage>(); 
	
	private class Label {
		final int ParagraphNumber;
		final ZLTextModel Model;
		
		Label(ZLTextModel model, int paragraphNumber) {
			ParagraphNumber = paragraphNumber;
			Model = model;
		}
	}
	
	public ZLTextPlainModel getBookModel() {
		return myBookModel;
	}
	
	public ContentsModel getContentsModel() {
		return myContentsModel;
	}
	
	public ZLTextPlainModel getFootnoteModel(String id) {
		if (!myFootnotes.containsKey(id)) {
			myFootnotes.put(id, myModelFactory.createPlainModel()); 
		}
		return myFootnotes.get(id); 
	}
	
	public Map<String, ZLTextPlainModel> getFootnotes() {
		return Collections.unmodifiableMap(myFootnotes);
	}
	
	public void addHyperlinkLabel(String label, ZLTextModel model, int paragraphNumber) {
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

	public Map<String,ZLImage> getImageMap() {
		return myImageMap;
	}

	public void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}
}
