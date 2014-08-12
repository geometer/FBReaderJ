package com.yotadevices.yotaphone2.fbreader;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.ui.android.R;

public class FileChooseActivity extends TreeActivity<LibraryTree> implements IBookCollection.Listener {
    private FileRootTree mRootTree;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        new FileChooserAdapter(this);
        deleteRootTree();

        ActionBar ab = getActionBar();
        ab.setIcon(R.drawable.r_color_logo);

        final BookCollectionShadow collection = new BookCollectionShadow();
        collection.bindToService(this, new Runnable() {
            public void run() {
                setProgressBarIndeterminateVisibility(!collection.status().IsCompleted);
                mRootTree = new FileRootTree(collection);
                collection.addListener(FileChooseActivity.this);
                init(getIntent());
            }
        });
    }

    private synchronized void deleteRootTree() {
        if (mRootTree != null) {
            mRootTree.Collection.removeListener(this);
            ((BookCollectionShadow)mRootTree.Collection).unbind();
            mRootTree = null;
        }
    }

    @Override
    protected LibraryTree getTreeByKey(FBTree.Key key) {
        return key != null ? mRootTree.getLibraryTree(key) : mRootTree;
    }

    @Override
    public boolean isTreeSelected(FBTree tree) {
        return false;
    }

    @Override
    protected void onDestroy() {
        deleteRootTree();
        super.onDestroy();
    }

    @Override
    public void onBookEvent(BookEvent event, Book book) {

    }

    @Override
    public void onBuildEvent(IBookCollection.Status status) {

    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long rowId) {
        final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
        final Book book = tree.getBook();
        if (book != null) {
            showBookInfo(book);
        } else {
            openTree(tree);
        }
    }

    private void showBookInfo(Book book) {
        final Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
        FBReaderIntents.putBookExtra(intent, book);
        OrientationUtil.startActivity(this, intent);
    }

}
