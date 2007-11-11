package org.zlibrary.core.resources;

import java.util.Map;

public class ZLTreeResource {
	public static ZLTreeResource ourRoot;

	/*public static void buildTree() {
		
	}
	public static void loadData(String language) {
		String filePath = ZLibrary.FileNameDelimiter + "resources" + ZLibrary.FileNameDelimiter + language + ".xml";
		new ZLResourceTreeReader(ourRoot).readDocument(ZLApplication.ZLibraryDirectory() + filePath);
		new ZLResourceTreeReader(ourRoot).readDocument(ZLApplication.ApplicationDirectory() + filePath);

	}

	private ZLTreeResource(String name);
	private	ZLTreeResource(String name, String value);
	private	void setValue(String value);
	private	boolean hasValue();
	private	String value();

	public ZLResource &operator [] (String key);

	private boolean myHasValue;
	private	String myValue;
	private	Map<String, ZLTreeResource> myChildren;*/
}

/*void ZLTreeResource::loadData(const std::string &language) {
	std::string filePath = ZLibrary::FileNameDelimiter + "resources" + ZLibrary::FileNameDelimiter + language + ".xml";
	ZLResourceTreeReader(ourRoot).readDocument(ZLApplication::ZLibraryDirectory() + filePath);
	ZLResourceTreeReader(ourRoot).readDocument(ZLApplication::ApplicationDirectory() + filePath);
}

void ZLTreeResource::buildTree() {
	if (ourRoot.isNull()) {
		ourRoot = new ZLTreeResource(std::string());
		loadData("en");
		const std::string language = ZLibrary::Language();
		if (language != "en") {
			loadData(language);
		}
	}
}

ZLTreeResource::ZLTreeResource(const std::string &name) : ZLResource(name), myHasValue(false) {
}

ZLTreeResource::ZLTreeResource(const std::string &name, const std::string &value) : ZLResource(name), myHasValue(true), myValue(value) {
}

void ZLTreeResource::setValue(const std::string &value) {
	myHasValue = true;
	myValue = value;
}

bool ZLTreeResource::hasValue() const {
	return myHasValue;
}

const std::string &ZLTreeResource::value() const {
	return myHasValue ? myValue : ZLMissingResource::ourValue;
}

const ZLResource &ZLTreeResource::operator [] (const std::string &key) const {
	std::map<std::string,shared_ptr<ZLTreeResource> >::const_iterator it = myChildren.find(key);
	if (it != myChildren.end()) {
		return *it->second;
	} else {
		return ZLMissingResource::instance();
	}
}*/
