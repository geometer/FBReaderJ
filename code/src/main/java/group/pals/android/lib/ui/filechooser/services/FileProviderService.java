/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Base service for a file provider.
 * 
 * @author Hai Bison
 * @since v3.1
 */
public abstract class FileProviderService extends Service implements IFileProvider {

    /*-------------------------------------------------------------------
     * Service
     */

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public IFileProvider getService() {
            return FileProviderService.this;
        }
    }// LocalBinder

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /*-------------------------------------------------------------------
     * IFileProvider
     */

    private boolean mDisplayHiddenFiles = false;
    private String mRegexFilenameFilter = null;
    private FilterMode mFilterMode = FilterMode.FilesOnly;
    private int mMaxFileCount = 1024;
    private SortType mSortType = SortType.SortByName;
    private SortOrder mSortOrder = SortOrder.Ascending;

    @Override
    public void setDisplayHiddenFiles(boolean display) {
        mDisplayHiddenFiles = display;
    };

    @Override
    public boolean isDisplayHiddenFiles() {
        return mDisplayHiddenFiles;
    }

    @Override
    public void setRegexFilenameFilter(String regex) {
        mRegexFilenameFilter = regex;
    };

    @Override
    public String getRegexFilenameFilter() {
        return mRegexFilenameFilter;
    }

    @Override
    public void setFilterMode(FilterMode fm) {
        mFilterMode = fm;
    }

    @Override
    public FilterMode getFilterMode() {
        return mFilterMode;
    }

    @Override
    public void setSortType(SortType st) {
        mSortType = st;
    }

    @Override
    public SortType getSortType() {
        return mSortType;
    }

    @Override
    public void setSortOrder(SortOrder so) {
        mSortOrder = so;
    }

    @Override
    public SortOrder getSortOrder() {
        return mSortOrder;
    }

    @Override
    public void setMaxFileCount(int max) {
        mMaxFileCount = max;
    };

    @Override
    public int getMaxFileCount() {
        return mMaxFileCount;
    }
}
