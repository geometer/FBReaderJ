package org.geometerplus.fbreader.formats.oeb;

import java.util.ArrayList;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.zlibrary.core.filesystem.*;

public class OEBPlugin extends FormatPlugin {
	public boolean providesMetaInfo() {
		 return true;
	}
	
	public boolean acceptsFile(ZLFile file) {
		final String extension = file.getExtension().intern();
		return (extension == "opf") ||
					 (extension == "oebzip") ||
					 (extension == "epub");
	}

	private String getOpfFileName(String oebFileName) {
		final ZLFile oebFile = new ZLFile(oebFileName);
		if (oebFile.getExtension() == "opf") {
			return oebFileName;
		}

		oebFile.forceArchiveType(ZLFile.ArchiveType.ZIP);
		final ZLDir zipDir = oebFile.getDirectory(false);
		if (zipDir == null) {
			return null;
		}

		final ArrayList fileNames = zipDir.collectFiles();
		final int len = fileNames.size();
		for (int i = 0; i < len; ++i) {
			final String shortName = (String)fileNames.get(i);
			if (shortName.endsWith(".opf")) {
				return zipDir.getItemPath(shortName);
			}
		}
		return null;
	}

	public boolean readDescription(String path, BookDescription description) {
		path = getOpfFileName(path);
		if (path == null) {
			return false;
		}
		return false;
		//return new FB2DescriptionReader(description).readDescription(path);
	}
	
	public boolean readModel(BookDescription description, BookModel model) {
		final String path = getOpfFileName(description.getFileName());
		if (path == null) {
			return false;
		}
		return false;
		//return new FB2Reader(model).readBook(description.getFileName());
	}
	
	public String getIconName() {
		return "oeb";
	}
}
