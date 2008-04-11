package org.geometerplus.zlibrary.core.language;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class ZLLanguageMatcher {

	public static final String UTF8_ENCODING_NAME = "UTF-8";

	public ZLLanguageMatcher(ZLLanguageDetector.LanguageInfo info) {
		myInfo = info;
	}
	
	public abstract void reset();

	public abstract int criterion();
	
	public ZLLanguageDetector.LanguageInfo info() {
		return myInfo;
	}

	protected ZLLanguageDetector.LanguageInfo myInfo;

	//subclasses
	static abstract class ZLWordBasedMatcher extends ZLLanguageMatcher {

		public ZLWordBasedMatcher(ZLLanguageDetector.LanguageInfo info) {
			super(info);
		}

		public	abstract void processWord(String word, int length);
	};

	static class ZLLanguagePatternBasedMatcher extends ZLWordBasedMatcher {

		public ZLLanguagePatternBasedMatcher(String fileName, ZLLanguageDetector.LanguageInfo info)  {
			super(info);
			try{
			InputStream dictionaryStream = new ZLFile(fileName).getInputStream();
			if (dictionaryStream == null) {
				return;
			}
			
			final int BUFFER_SIZE = 20480;
			byte[] buffer = new byte[BUFFER_SIZE];
			final int start = 0;
			final int end = dictionaryStream.read(buffer, 0,BUFFER_SIZE);
			dictionaryStream.close();
			int wordStart = 0;
			for (int ptr = start; ptr != end; ++ptr) {
				if (buffer[ptr] == '\n') {
					String str = new String(buffer,wordStart, ptr - wordStart);
					if (myDictionary.contains(str)) {
					    myDictionary.add(str);	
					} 
					wordStart = ptr + 1;				
				}
			}}catch (IOException e) {
				e.printStackTrace();
			}
			
			reset();
		}

		public void reset() {
			myProCounter = 1;
			myContraCounter = 1;
		}
		
		public	void processWord(String word, int length) {
			if (length < 5) {
				if (myDictionary.contains(word)) {
					++myProCounter;
				} else {
					++myContraCounter;
				}
			}
		}

		public	int criterion() {
			return myProCounter * 2000 / (myProCounter + myContraCounter) - 1000;
		}

		private int myProCounter;
	    private int myContraCounter;
	    private	ArrayList/*<String>*/ myDictionary = new ArrayList();
	};
}
