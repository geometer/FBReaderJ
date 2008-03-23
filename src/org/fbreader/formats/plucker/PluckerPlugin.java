package org.fbreader.formats.plucker;

import java.io.IOException;
import java.io.InputStream;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.description.BookDescription;
import org.fbreader.formats.pdb.PdbPlugin;
import org.zlibrary.core.filesystem.ZLFile;

public class PluckerPlugin extends PdbPlugin {
	public boolean providesMetaInfo() {
		return false;
	}
	
	public	boolean acceptsFile(ZLFile file) {		
		return PdbPlugin.fileType(file).equals("DataPlkr");
	}
	
	public	boolean readDescription(String path, BookDescription description) {
		ZLFile file = new ZLFile(path);

		InputStream stream = null;
		try {
			stream = file.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		detectEncodingAndLanguage(description, stream);
		if (description.getEncoding().length() == 0) {
			return false;
		}

		return true;
	}
	
	public	boolean readModel(BookDescription description, BookModel model)  {
		try {
			return new PluckerBookReader(description.getFileName(), model, description.getEncoding()).readDocument();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public	String getIconName() {
		return "plucker";
	}


}
