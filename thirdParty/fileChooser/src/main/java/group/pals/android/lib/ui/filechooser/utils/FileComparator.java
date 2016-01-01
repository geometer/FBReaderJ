/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.io.localfile.ParentFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.util.Comparator;

import org.fbreader.util.NaturalOrderComparator;

/**
 * {@link IFile} comparator.<br>
 * Rules:<br>
 * - directories first;<br>
 * - other properties are based on parameters given in constructor, see
 * {@link #FileComparator(IFileProvider.SortType, IFileProvider.SortOrder)};
 * 
 * @author Hai Bison
 * @since v1.91
 */
public class FileComparator implements Comparator<IFile> {
	private static final NaturalOrderComparator ourNaturalOrderComparator =
		new NaturalOrderComparator();

    private final IFileProvider.SortType mSortType;
    private final IFileProvider.SortOrder mSortOrder;

    /**
     * Creates new {@link FileComparator}
     * 
     * @param sortType
     *            see {@link IFileProvider.SortType}
     * @param sortOrder
     *            see {@link IFileProvider.SortOrder}
     */
    public FileComparator(IFileProvider.SortType sortType, IFileProvider.SortOrder sortOrder) {
        mSortType = sortType;
        mSortOrder = sortOrder;
    }

    @Override
    public int compare(IFile lhs, IFile rhs) {
        if ((lhs.isDirectory() && rhs.isDirectory()) || (lhs.isFile() && rhs.isFile())) {
            // default is to compare by name (case insensitive)
            int res = ourNaturalOrderComparator.compare(lhs.getSecondName(), rhs.getSecondName());
            
            switch (mSortType) {
            case SortByName:
                break;// SortByName

            case SortBySize:
                if (lhs.length() > rhs.length())
                    res = 1;
                else if (lhs.length() < rhs.length())
                    res = -1;
                break;// SortBySize

            case SortByDate:
                if (lhs.lastModified() > rhs.lastModified())
                    res = 1;
                else if (lhs.lastModified() < rhs.lastModified())
                    res = -1;
                break;// SortByDate
            }
            //Do not affect the sort on parent item (directs to parent dir)
            if(lhs.getSecondName().equals(ParentFile.parentSecondName) || rhs.getSecondName().equals(ParentFile.parentSecondName)){
                return 1;
            }else{
                return mSortOrder == IFileProvider.SortOrder.Ascending ? res : -res;
            }
        }

        return lhs.isDirectory() ? -1 : 1;
    }// compare
}
