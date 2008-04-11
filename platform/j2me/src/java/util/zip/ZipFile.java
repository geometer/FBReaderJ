package java.util.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.geometerplus.zlibrary.core.util.File;

public class ZipFile {
	public ZipFile(String name) throws IOException {
		// TODO: implement
	}

	public ZipFile(File file) throws IOException, ZipException {
		// TODO: implement
	}

	public Enumeration entries() {
		// TODO: implement
		return null;
	}

	public ZipEntry getEntry(String entryName) {
		// TODO: implement
		return null;
	}

	public InputStream getInputStream(ZipEntry entry) {
		// TODO: implement
		return null;
	}
}
