package org.fbreader.encoding;

import java.util.ArrayList;

public class ZLEncodingSet {
	public ZLEncodingSet(String name) {
		
	}
	public	void addInfo(ZLEncodingConverterInfo info) {
		myInfos.add(info);
	}

	public	String name() {
		return myName;
	}
	
	public	ArrayList/*<ZLEncodingConverterInfo>*/ infos() {
		return myInfos;
	}

	private String myName = "";
	private	ArrayList/*<ZLEncodingConverterInfo>*/ myInfos = new ArrayList();
}
