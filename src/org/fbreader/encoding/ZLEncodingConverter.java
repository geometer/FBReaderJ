package org.fbreader.encoding;

public abstract  class ZLEncodingConverter {
	protected ZLEncodingConverter() {}

	//abstract public	void convert(String dst, const char *srcStart, const char *srcEnd);
	public	void convert(String dst, String src) {
		//convert(dst, src.toCharArray(), src.data() + src.length());
	}
	public	abstract void reset();
	public	abstract boolean fillTable(int[] map);

	//private	ZLEncodingConverter(ZLEncodingConverter zl);
	//private ZLEncodingConverter &operator=(ZLEncodingConverter zl);
}
