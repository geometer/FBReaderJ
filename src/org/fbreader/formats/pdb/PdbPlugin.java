package org.fbreader.formats.pdb;

import java.io.IOException;
import java.io.InputStream;

import org.fbreader.description.BookDescriptionUtil;
import org.fbreader.formats.FormatPlugin;
import org.fbreader.option.FBOptions;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.options.ZLStringOption;

public abstract  class PdbPlugin extends FormatPlugin {

	protected PdbPlugin() {
		super();
	}

	public  static String fileType(final ZLFile file) {
		final String extension = file.getExtension().toLowerCase();//ZLUnicodeUtil.toLower(file.getExtension());
		if ((extension != "prc") && (extension != "pdb") && (extension != "mobi")) {
			return "";
		}

		String fileName = file.getPath();
		int index = fileName.indexOf(':');
		ZLFile baseFile = (index == -1) ? file : new ZLFile(fileName.substring(0, index));
		boolean upToDate = BookDescriptionUtil.checkInfo(baseFile);

		ZLStringOption palmTypeOption = new ZLStringOption(FBOptions.BOOKS_CATEGORY, file.getPath(), "PalmType", "");
		String palmType = palmTypeOption.getValue();
		if ((palmType.length() != 8) || !upToDate) {
			InputStream stream = null;
			byte[] id = new byte[8];
			try {
				stream = file.getInputStream();
				if (stream == null || !((PdbStream)stream).open()) {
					return "";
				}
				stream.skip(60);
				stream.read(id, 0, 8);
				stream.close();
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		    }
		    palmType = new String(id).substring(8);
			if (!upToDate) {
				BookDescriptionUtil.saveInfo(baseFile);
			}
			palmTypeOption.setValue(palmType);
		}
		return palmType;
	}
	
	public	String getIconName() {
		return "pdb";
	}
	
	public boolean providesMetaInfo() {
		return false;
	}
}
