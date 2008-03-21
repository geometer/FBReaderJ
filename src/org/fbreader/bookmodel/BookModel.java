package org.fbreader.bookmodel;

import java.util.*;
import org.zlibrary.core.util.*;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.image.ZLImageMap;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.impl.ZLModelFactory;
import org.zlibrary.text.model.impl.ZLTextPlainModelImpl;

import org.fbreader.description.BookDescription;
import org.fbreader.formats.FormatPlugin;
import org.fbreader.formats.FormatPlugin.PluginCollection;

public final class BookModel {
	private final ZLTextPlainModelImpl myBookTextModel = new ZLTextPlainModelImpl(65536);
	private final ContentsModel myContentsModel = new ContentsModel();
	private final HashMap myFootnotes = new HashMap();
	private final HashMap myInternalHyperlinks = new HashMap();

	private final BookDescription myDescription;
	
	private final ZLImageMap myImageMap = new ZLImageMap(); 
	
	private final String myFileName;

	private class Label {
		final int ParagraphNumber;
		final ZLTextModel Model;
		
		Label(ZLTextModel model, int paragraphNumber) {
			ParagraphNumber = paragraphNumber;
			Model = model;
		}
	}
	/**
	 * @deprecated
	 * */
	//old version for compilation
	public BookModel(String fileName) {
		myFileName = fileName;
		myDescription = null;
	}
	
	public BookModel(final BookDescription description) {
		myFileName = description.getFileName();
		myDescription = description;
		ZLFile file = new ZLFile(description.getFileName());
		System.out.println(description.getFileName());
		FormatPlugin plugin = PluginCollection.instance().getPlugin(file, false);
		if (plugin != null) {
			plugin.readModel(description, this);
		}
	}

	//method changed!!!
	public String getFileName() {
		//return myDescription.getFileName();
		return myFileName;
	}
	
	public BookDescription getDescription() { 
		return myDescription; 
	}

	public ZLTextPlainModelImpl getBookTextModel() {
		return myBookTextModel;
	}
	
	public ContentsModel getContentsModel() {
		return myContentsModel;
	}
	
	public ZLTextPlainModelImpl getFootnoteModel(String id) {
		final HashMap footnotes = myFootnotes;
		ZLTextPlainModelImpl model = (ZLTextPlainModelImpl)footnotes.get(id);
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
		Label label = (Label)myInternalHyperlinks.get(link);
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
