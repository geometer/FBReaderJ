/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

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

		private int myProCounter;
	    private int myContraCounter;
	    private	ArrayList<String> myDictionary = new ArrayList<String>();

	    public ZLLanguagePatternBasedMatcher(ZLFile file, ZLLanguageDetector.LanguageInfo info)  {
			super(info);
			try{
			InputStream dictionaryStream = file.getInputStream();
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
	};
}
