package org.geometerplus.android.fbreader.library;

import org.geometerplus.fbreader.library.BooksDatabase;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class LibraryCommon {
	static BooksDatabase DatabaseInstance;
	static Library LibraryInstance;
	static ViewType ViewTypeInstance;
	static final ZLStringOption BookSearchPatternOption = 
		new ZLStringOption("BookSearch", "Pattern", "");	// for LibraryBaseActivity
	static SortType SortTypeInstance; 						// for FileManager
}

interface HasBaseConstants {
	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";
	static final int OPEN_BOOK_ITEM_ID = 0;
	static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	static final int ADD_TO_FAVORITES_ITEM_ID = 2;
	static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
	static final int DELETE_BOOK_ITEM_ID = 4;

	static final int CHILD_LIST_REQUEST = 0;
	static final int BOOK_INFO_REQUEST = 1;
	static final int RESULT_DONT_INVALIDATE_VIEWS = 0;
	static final int RESULT_DO_INVALIDATE_VIEWS = 1;	
}

interface HasLibraryConstants {
	static final String TREE_PATH_KEY = "TreePath";
	static final String PARAMETER_KEY = "Parameter";

	static final String PATH_FAVORITES = "favorites";
	static final String PATH_SEARCH_RESULTS = "searchResults";
	static final String PATH_RECENT = "recent";
	static final String PATH_BY_AUTHOR = "byAuthor";
	static final String PATH_BY_TITLE = "byTitle";
	static final String PATH_BY_TAG = "byTag";
}

enum SortType{
	BY_NAME{
		public String getName() {
			return myResource.getResource("byName").getValue();
		}
	},
	BY_DATE{
		public String getName() {
			return myResource.getResource("byDate").getValue();
		}
	};

	private static ZLResource myResource = ZLResource.resource("libraryView").getResource("sortingBox");
	
	public abstract String getName();
	
	public static String[] toStringArray(){
		SortType[] sourse = values();
		String[] result = new String[sourse.length];
		for (int i = 0; i < sourse.length; i++){
			result[i] = sourse[i].getName();
		}
		return result;
	}
}

enum ViewType{

	SIMPLE{
		public String getName() {
			return myResource.getResource("simple").getValue();
		}
	},
	SKETCH{
		public String getName() {
			return myResource.getResource("sketch").getValue();
		}
	};
	
	private static ZLResource myResource = ZLResource.resource("libraryView").getResource("viewBox");
	
	public abstract String getName();

	public static String[] toStringArray(){
		ViewType[] sourse = values();
		String[] result = new String[sourse.length];
		for (int i = 0; i < sourse.length; i++){
			result[i] = sourse[i].getName();
		}
		return result;
	}
}