package org.zlibrary.core.resources;

abstract public class ZLResource {
	private String myName;

	public static ZLResource resource(String key) {
        return null;//todo
	}
    
	public static ZLResource resource(ZLResourceKey key) {
    	return resource(key.Name);
    }

	protected ZLResource(String name) {
		this.myName = name;
	}

	public String name() {
		return myName;
	}
	abstract public boolean hasValue();
	
	abstract public String value();

	public ZLResource getResource(ZLResourceKey key) {
		return getResource(key.Name);
	}
	
	abstract public ZLResource getResource(String key);
}
/*const ZLResource &ZLResource::operator [] (const ZLResourceKey &key) const {
	return (*this)[key.Name];
}

const ZLResource &ZLResource::resource(const std::string &key) {
	ZLTreeResource::buildTree();
	if (ZLTreeResource::ourRoot.isNull()) {
		return ZLMissingResource::instance();
	}
	return (*ZLTreeResource::ourRoot)[key];
}

*/
