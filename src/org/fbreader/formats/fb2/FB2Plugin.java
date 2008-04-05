package org.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.description.BookDescription;
import org.fbreader.formats.FormatPlugin;
import org.zlibrary.core.filesystem.ZLFile;

public class FB2Plugin extends FormatPlugin {
	public boolean providesMetaInfo() {
		 return true;
	}
	
	public boolean acceptsFile(ZLFile file) {
		return "fb2".equals(file.getExtension());
	}
	
	public boolean readDescription(String path, BookDescription description) {
		return new FB2DescriptionReader(description).readDescription(path);
	}
	
	public boolean readModel(BookDescription description, BookModel model) {
		return new FB2Reader(model).readBook(description.getFileName());
	}
	
	public String getIconName() {
		final String ICON_NAME = "fb2";
		return ICON_NAME;
	}
}
