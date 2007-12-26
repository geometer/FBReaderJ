package org.fbreader.bookmodel;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.impl.ZLModelFactory;

public class BookModel {
	private final ZLModelFactory myModelFactory = new ZLModelFactory();
	private final ZLTextPlainModel myBookTextModel = myModelFactory.createPlainModel();
	private final ContentsModel myContentsModel = new ContentsModel();
	private final TreeMap<String,ZLTextPlainModel> myFootnotes = new TreeMap<String,ZLTextPlainModel>();
	private final TreeMap<String,Label> myInternalHyperlinks = new TreeMap<String,Label>();
	private final TreeMap<String,ZLImage> myImageMap = new TreeMap<String,ZLImage>(); 
	
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

	public ZLTextPlainModel getBookTextModel() {
		return myBookTextModel;
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

	public Map<String,ZLImage> getImageMap() {
		return myImageMap;
	}

	void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}
}
