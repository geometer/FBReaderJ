package org.geometerplus.fbreader.formats.util;

import java.io.InputStream;

public class EncodingDetector {
	public class Language {
		public static final int	OTHER = 1;
		public static final int	RUSSIAN = 2;
		public static final int	CHINESE = 3;
		public static final int	CZECH = 4;
	};
	
	private final static int BUFSIZE = 65536;


	public static String detect(InputStream stream, String language) {
		String encodingString = "ISO-8859-1";
		/*if (stream.open()) {
			String languageString = "none";
			switch (language) {
				case Language.RUSSIAN:
					languageString = "ru";
					break;
				case Language.CHINESE:
					languageString = "zh";
					break;
				case Language.CZECH:
					languageString = "cs";
					break;
				case Language.OTHER:
					break;
			}
			/*EncaAnalyser analyser = enca_analyser_alloc(languageString.c_str());
			if (analyser != 0) {
				unsigned char *buffer = new unsigned char[BUFSIZE];
				size_t buflen = stream.read((char*)buffer, BUFSIZE);

				enca_set_filtering(analyser, 0);
				EncaEncoding encoding = enca_analyse(analyser, buffer, buflen);
				const char *e = enca_charset_name(encoding.charset, ENCA_NAME_STYLE_MIME);
				if (e != 0) {
					encodingString = e;
				}
				if (encodingString == "GB2312") {
					encodingString = "GBK";
				}
				enca_analyser_free(analyser);

				delete[] buffer;
			}

			stream.close();
		}*/

		return encodingString;

	}

	private EncodingDetector() {}

}
