package org.fbreader.bookmodel;

import java.util.HashMap;
import java.util.Map;

import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.impl.ZLModelFactory;

public class BookModel {
	private ZLTextPlainModel myBookModel = (new ZLModelFactory()).createPlainModel();
	private Map<String, ZLTextPlainModel> myFootnotes = 
		new HashMap<String, ZLTextPlainModel>();
	
	public ZLTextPlainModel getBookModel() {
		return myBookModel;
	}
	
	public ZLTextPlainModel getFootnoteModel(String id) {
		if (!myFootnotes.containsKey(id)) {
			myFootnotes.put(id, (new ZLModelFactory()).createPlainModel()); 
		}
		return myFootnotes.get(id); 
	}
	
	public Map<String, ZLTextPlainModel> getFootnotes() {
		return myFootnotes;
	}
}
