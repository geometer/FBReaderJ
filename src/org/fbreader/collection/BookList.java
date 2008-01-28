package org.fbreader.collection;

import java.util.*;

import org.zlibrary.core.util.*;
import org.zlibrary.core.options.*;

public class BookList {
	private final static String GROUP = "BookList";
	static final String BOOK = "Book";
	static final String SIZE = "Size";
	private final ArrayList myFileNames = new ArrayList();
	
	public BookList() {
		int size = new ZLIntegerOption(ZLOption.STATE_CATEGORY, GROUP, SIZE, 0).getValue();
		for (int i = 0; i < size; ++i) {
			String optionName = BOOK;
			optionName += i;
			ZLStringOption bookOption = new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, optionName, "");
			final String fileName = bookOption.getValue();
			if (fileName.length() != 0) {
				addFileName(fileName);
			}
		}
	}

	public ArrayList fileNames() {
		return myFileNames;
	}
	
	public void addFileName(String fileName) {
		myFileNames.add(fileName);
	}
	
	public void removeFileName(String fileName) {
		myFileNames.remove(fileName);
	}
}

