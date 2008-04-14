package org.geometerplus.fbreader.encoding;

public class DummyEncodingConverterProvider extends ZLEncodingConverterProvider {

	public ZLEncodingConverter createConverter() {
		return new DummyEncodingConverter();
	}
	
	public ZLEncodingConverter createConverter(String encoding) {
		return createConverter();
	}

	public boolean providesConverter(String encoding) {
		final String lowerCasedEncoding = encoding.toLowerCase();
		return ("utf-8".equals(lowerCasedEncoding)) || ("us-ascii".equals(lowerCasedEncoding));
	}

	private static class DummyEncodingConverter extends ZLEncodingConverter {

		private DummyEncodingConverter() {}
		
		public boolean fillTable(int[] map) {
			for (int i = 0; i < 255; ++i) {
				map[i] = i;
			}
			return true;
		}

		public void reset() {}
		
		//size=end-start
		public String convert(char [] src, int start, int end) {
			return new String(src, start, end - start);
		}

	}
}
