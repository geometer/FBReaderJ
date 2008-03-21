package org.fbreader.formats.html;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.description.BookDescription;
import org.fbreader.description.BookDescription.WritableBookDescription;
import org.fbreader.formats.FormatPlugin;
import org.fbreader.formats.fb2.FB2DescriptionReader;
import org.fbreader.formats.fb2.FB2Reader;
import org.zlibrary.core.filesystem.ZLFile;


public class HtmlPlugin extends FormatPlugin {
	private final static String AUTO = "auto";
	
	@Override
	public boolean acceptsFile(ZLFile file) {
		return "htm".equals(file.getExtension()) 
			|| "html".equals(file.getExtension());
	}

	@Override
	public String getIconName() {
		final String ICON_NAME = "html";
		return ICON_NAME;
	}

	@Override
	public boolean providesMetaInfo() {
		return true;
	}

	@Override
	public boolean readDescription(String path, BookDescription description) {
		return new HtmlDescriptionReader(description).readDescription(path);
        // always true =)
		//return true;
	}

	@Override
	public boolean readModel(BookDescription description, BookModel model) {
		// TODO Auto-generated method stub
		if (!description.getEncoding().equals(AUTO)) {
			new BookDescription.BookInfo(description.getFileName()).EncodingOption.setValue(AUTO);
		}
		return new HtmlReader(model).readBook(description.getFileName());
	}

}
