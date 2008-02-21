package org.zlibrary.core.util;

import java.io.IOException;

public class File {
	public static final String separator = "/";
	public static final String pathSeparator = ":";

	public static File[] listRoots() {
		// TODO: implement
		return new File[0];
	}

	public File(String name) {
		// TODO: implement
	}

	public String getPath() {
		// TODO: implement
		return null;
	}

	public String getCanonicalPath() throws IOException {
		// TODO: implement
		return null;
	}

	public String getName() {
		// TODO: implement
		return null;
	}

	public void mkdirs() {
		// TODO: implement
	}

	public boolean delete() {
		// TODO: implement
		return false;
	}

	public boolean exists() {
		// TODO: implement
		return false;
	}

	public long lastModified() {
		// TODO: implement
		return 0;
	}

	public long length() {
		// TODO: implement
		return 0;
	}

	public boolean isDirectory() {
		// TODO: implement
		return false;
	}

	public String[] list() {
		// TODO: implement
		return null;
	}

	public File[] listFiles() {
		// TODO: implement
		return new File[0];
	}

	public String getParent() {
		// TODO: implement
		return null;
	}
}
