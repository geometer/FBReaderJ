package org.geometerplus.fbreader.encoding;

public abstract class ZLEncodingConverterProvider {
	protected ZLEncodingConverterProvider() {}

	public abstract boolean providesConverter(String encoding);
	public abstract ZLEncodingConverter createConverter(String encoding);

}
