package org.zlibrary.core.resources;

abstract public class ZLResource {
	public final String Name;

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
