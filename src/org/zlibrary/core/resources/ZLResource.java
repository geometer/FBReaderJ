package org.zlibrary.core.resources;

import org.zlibrary.core.library.ZLibrary;

abstract public class ZLResource {
	public final String Name;
	
	// this static fields and set-methods were created so as to run tests
	protected static String ourApplicationDirectory = ZLibrary.JAR_DATA_PREFIX + "data/resources/application/";
	protected static String ourZLibraryDirectory = ZLibrary.JAR_DATA_PREFIX + "data/resources/zlibrary/";
	
	public static void setApplicationDirectory(String dir) {
		ourApplicationDirectory = dir;
	}
	
	public static void setZLibraryDirectory(String dir) {
		ourZLibraryDirectory = dir;
	}
	
	public static ZLResource resource(String key) {
		ZLTreeResource.buildTree();
		if (ZLTreeResource.ourRoot == null) {
			return ZLMissingResource.Instance;
		}
		return ZLTreeResource.ourRoot.getResource(key);
	}

	protected ZLResource(String name) {
		Name = name;
	}

	abstract public boolean hasValue();
	abstract public String getValue();
	abstract public ZLResource getResource(String key);
}
