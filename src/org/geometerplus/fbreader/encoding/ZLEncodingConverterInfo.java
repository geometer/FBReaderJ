package org.geometerplus.fbreader.encoding;

import java.util.ArrayList;
import java.util.Iterator;

public class ZLEncodingConverterInfo {
	public ZLEncodingConverterInfo(String name, String region) {
		myName = name;
		myVisibleName = region + " (" + name + ")";
		addAlias(myName);
	}
	
	public	void addAlias(String alias) {
		myAliases.add(alias);
	}

	public	String name() {
		return myName;
	}
	
	public	String visibleName() {
		return myVisibleName;
	}
	
	public	ZLEncodingConverter createConverter() {
		ZLEncodingCollection collection = ZLEncodingCollection.instance();
		ArrayList<ZLEncodingConverterProvider> providers = collection.providers();
		for (Iterator it = providers.iterator(); it.hasNext(); ) {
			for (Iterator jt = myAliases.iterator(); jt.hasNext(); ) {
				ZLEncodingConverterProvider itp = (ZLEncodingConverterProvider)it.next();
				String str = (String)jt.next();
				if (itp.providesConverter(str)) {
					return itp.createConverter(str);
				}
			}
		}

		return ZLEncodingCollection.instance().defaultConverter();
	}
	
	public	boolean canCreateConverter() {
		ZLEncodingCollection collection = ZLEncodingCollection.instance();
		ArrayList<ZLEncodingConverterProvider>  providers = collection.providers();
		for (Iterator it = providers.iterator(); it.hasNext();) {
			for (Iterator jt = myAliases.iterator(); jt.hasNext(); ) {
				if (((ZLEncodingConverterProvider)it.next()).providesConverter((String)jt.next())) {
					return true;
				}
			}
		}
		return false;	
	}

	private String myName = "";
	private	String myVisibleName = "";
	private	ArrayList<String> myAliases = new ArrayList();

	//private ZLEncodingConverterInfo(const ZLEncodingConverterInfo&);
	//private	ZLEncodingConverterInfo &operator=(const ZLEncodingConverterInfo&);
}
