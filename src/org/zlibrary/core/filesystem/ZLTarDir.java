package org.zlibrary.core.filesystem;

import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

class ZLTarDir extends ZLDir {
	ZLTarDir(String path) {		
		super(path);
	}

	public String getDelimiter() {
		return ":";
	};
	
	private static ArrayList EMPTY = new ArrayList();
	public ArrayList collectSubDirs() {
		return EMPTY;
	};
	
	public ArrayList collectFiles() {		
		ArrayList names = new ArrayList();

		try {
			InputStream stream = new ZLFile(getPath()).getInputStream();
			if (stream != null) {
				ZLTarHeader header = new ZLTarHeader();
				while (header.read(stream)) {
					if (header.IsRegularFile) {
						names.add(header.Name);
					}
					final int lenToSkip = (header.Size + 0x1ff) & -0x200;
					if (lenToSkip < 0) {
						break;
					}
					if (stream.skip(lenToSkip) != lenToSkip) {
						break;
					}
					header.erase();
				}
			}
		} catch (IOException e) {
		}

		return names;
	};
}
