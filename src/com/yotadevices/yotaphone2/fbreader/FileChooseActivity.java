package com.yotadevices.yotaphone2.fbreader;

import android.os.Bundle;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.library.FileFirstLevelTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.tree.FBTree;

public class FileChooseActivity extends TreeActivity<LibraryTree> implements IBookCollection.Listener {
    private FileFirstLevelTree mRootTree;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        new FileChooserAdapter(this);
        deleteRootTree();

        final BookCollectionShadow collection = new BookCollectionShadow();
        collection.bindToService(this, new Runnable() {
            public void run() {
                setProgressBarIndeterminateVisibility(!collection.status().IsCompleted);
                mRootTree = new FileFirstLevelTree(collection);
                collection.addListener(FileChooseActivity.this);
                init(getIntent());
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
        return null;
    }

    @Override
    public boolean isTreeSelected(FBTree tree) {
        return false;
    }
}
