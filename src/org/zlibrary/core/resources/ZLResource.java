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
    
	public static ZLResource resource(ZLResourceKey key) {
		return resource(key.Name);
	}

	protected ZLResource(String name) {
		myName = name;
	}

	public final String getName() {
		return myName;
	}
	
	abstract public boolean hasValue();
	
	abstract public String value();

	public ZLResource getResource(ZLResourceKey key) {
		return getResource(key.Name);
	}
	
	abstract public ZLResource getResource(String key);
}
