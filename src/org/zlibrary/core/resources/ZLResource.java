package org.zlibrary.core.resources;

abstract public class ZLResource {
	private String myName;

	public static ZLResource resource(String key) {
		ZLTreeResource.buildTree();
		if (ZLTreeResource.ourRoot == null) {
			return ZLMissingResource.instance();
		}
		return ZLTreeResource.ourRoot.getResource(key);
	}

	protected ZLResource(String name) {
		myName = name;
	}

	public final String getName() {
		return myName;
	}
	
	abstract public boolean hasValue();
	abstract public String getValue();
	abstract public ZLResource getResource(String key);
}
