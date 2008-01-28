package org.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.description.BookDescription;
import org.fbreader.formats.FormatPlugin;
import org.zlibrary.core.filesystem.ZLFile;

public class FB2Plugin extends FormatPlugin {
	private final static String AUTO = "auto";

	public FB2Plugin() {}
	
	public boolean providesMetaInfo() {
		 return true;
	}
	
	public boolean acceptsFile(ZLFile file) {
		return file.getExtension().equals("fb2");
	}
	
	public boolean readDescription(String path, BookDescription description) {
		return new FB2DescriptionReader(description).readDescription(path);
	}
	
	public boolean readModel(BookDescription description, BookModel model) {
		// this code fixes incorrect config entry created by fbreader of version <= 0.6.1
		// makes no sense if old fbreader was not used
		if (!description.getEncoding().equals(AUTO)) {
			new BookDescription.BookInfo(description.getFileName()).EncodingOption.setValue(AUTO);
		}
        // FB2BookReader
		return true;//TODO new FB2Reader(model).readBook(description.getFileName());

	}
	
	public String getIconName() {
		final String ICON_NAME = "fb2";
		return ICON_NAME;
	}
	
}
