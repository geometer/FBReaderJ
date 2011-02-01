/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.util.List;

import org.geometerplus.android.fbreader.tree.ZLAndroidTree;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.library.AuthorTree;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BookTree;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.TagTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

abstract public class GalleryLibraryBaseActivity extends BaseGalleryActivity
	implements MenuItem.OnMenuItemClickListener, HasLibraryConstants, LibraryBaseAdapter.HasAdapter {

	protected Book mySelectedBook;

	@Override
	public LibraryBaseAdapter getAdapter() {
		return (LibraryBaseAdapter) myGallery.getAdapter();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == CHILD_LIST_REQUEST && returnCode == RESULT_DO_INVALIDATE_VIEWS) {
			((BaseAdapter)myGallery.getAdapter()).notifyDataSetChanged();
			setResult(RESULT_DO_INVALIDATE_VIEWS);
		}
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(LibraryCommon.BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	protected static final String ACTION_FOUND = "fbreader.library.intent.FOUND";

	protected boolean runSearch(Intent intent) {
	   	final String pattern = intent.getStringExtra(SearchManager.QUERY);
		if (pattern == null || pattern.length() == 0) {
			return false;
		}
		LibraryCommon.BookSearchPatternOption.setValue(pattern);
		return LibraryCommon.LibraryInstance.searchBooks(pattern).hasChildren();
	}

	protected void showNotFoundToast() {
		UIUtil.showErrorMessage(this, "bookNotFound");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	LibraryUtil.addMenuItem(menu, 1, myResource, "localSearch", R.drawable.ic_menu_search);
    	LibraryUtil.addMenuItem(menu, 0, myResource, "view", R.drawable.ic_menu_sorting);
    	return true;
    }
	
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                return onSearchRequested();
            default:
                return true;
        }
    }
    
	protected final class GalleryLibraryAdapter extends LibraryBaseAdapter 
		implements OnItemSelectedListener{

		public GalleryLibraryAdapter(List<FBTree> items) {
			super(items);
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			view.setSelected(false);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
		
		@Override
		public int getFirstSelectedItemIndex() {
			int index = 0;
			for (FBTree t : myItems) {
				if (isTreeSelected(t)) {
					return index;
				}
				++index;
			}
			return -1;
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final LibraryTree tree = (LibraryTree)getItem(position);
			if (tree instanceof BookTree) {
				createBookContextMenu(menu, ((BookTree)tree).Book);
			}
		}
		
		private int maxHeight = 0;
		private int maxWidth = 0;
		private int paddingTop = 0;
		private int orientation = -1;

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			final FBTree tree = getItem(position);
		
			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if (orientation != display.getOrientation()){
            	orientation = display.getOrientation();
            	switch (display.getOrientation()) {
					case 0:
						maxWidth = display.getWidth() / 2;
						maxHeight = maxWidth * 4 / 3;
					break;

					case 1:
						maxWidth = (int) (display.getWidth() / 3);
						maxHeight = maxWidth * 4 / 3;
						paddingTop = (display.getHeight() - maxHeight) / 4;
					break;
            	}
            }
            
            String summary = tree.getName();
            ZLImage cover = tree.getCover();
            int idIcon = 0; 
     		if (tree instanceof ZLAndroidTree) {
				idIcon = ((ZLAndroidTree)tree).getCoverResourceId();
			} else {
				if (tree instanceof AuthorTree) {
					idIcon = R.drawable.ic_list_library_author;
				} else if (tree instanceof TagTree) {
					idIcon = R.drawable.ic_list_library_tag;
				} else if (tree instanceof BookTree) {
					idIcon = R.drawable.ic_list_library_book;
				} else {
					idIcon = R.drawable.ic_list_library_books;
				}
			}
     		
			View view = GalleryAdapterUtil.getView(convertView, 
					parent, summary, cover, idIcon, maxHeight, maxWidth, paddingTop);
			return view;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final FBTree tree = getAdapter().getItem(position); 	
		if (tree instanceof BookTree) {
			return onContextItemSelected(item.getItemId(), ((BookTree)tree).Book);
		}
		return super.onContextItemSelected(item);
	}

	// FIXME 
	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		getAdapter().notifyDataSetChanged();	
	}

	protected boolean isTreeSelected(FBTree tree) {
		return LibraryUtil.isTreeSelected(tree, mySelectedBook);
	}

	protected class StartTreeActivityRunnable extends AStartTreeActivityRunnable {
		public StartTreeActivityRunnable(String treePath, String parameter) {
			super(treePath, parameter);
		}

		public void run() {
			startActivityForResult(
				new Intent(GalleryLibraryBaseActivity.this, GalleryLibraryTreeActivity.class)
					.putExtra(SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
					.putExtra(TREE_PATH_KEY, myTreePath)
					.putExtra(PARAMETER_KEY, myParameter),
				CHILD_LIST_REQUEST
			);
		}
	}

	protected class OpenTreeRunnable extends AOpenTreeRunnable {
		public OpenTreeRunnable(Library library, String treePath) {
			this(library, treePath, null);
		}
		public OpenTreeRunnable(Library library, String treePath, String parameter) {
			this(library, new StartTreeActivityRunnable(treePath, parameter));
		}
		public OpenTreeRunnable(Library library, Runnable postRunnable) {
			super(library, postRunnable, GalleryLibraryBaseActivity.this);
		}
	}
	
}
