/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.services;

import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.io.IFileFilter;

import java.util.List;

/**
 * Interface for {@link IFile} providers.<br>
 * <br>
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public interface IFileProvider {

    /**
     * {@link IFile} sorting parameters.<br>
     * Includes:<br>
     * - {@link #SortByName}<br>
     * - {@link #SortBySize}<br>
     * - {@link #SortByDate}
     * 
     * @author Hai Bison
     * @since v2.1 alpha
     */
    public static enum SortType {
        /**
         * Sort by name, (directories first, case-insensitive)
         */
        SortByName,
        /**
         * Sort by size (directories first)
         */
        SortBySize,
        /**
         * Sort by date (directories first)
         */
        SortByDate
    }// _SortType

    /**
     * {@link IFile} sorting parameters.<br>
     * Includes:<br>
     * - {@link #Ascending}<br>
     * - {@link #Descending}
     * 
     * @author Hai Bison
     * @since v2.1 alpha
     */
    public static enum SortOrder {
        /**
         * Sort ascending.
         */
        Ascending(true),
        /**
         * Sort descending.
         */
        Descending(false);

        final boolean mAsc;

        SortOrder(boolean asc) {
            mAsc = asc;
        }

        public boolean isAsc() {
            return mAsc;
        }
    }// _SortOrder

    /**
     * The filter of {@link IFile}.<br>
     * Includes:<br>
     * - {@link #FilesOnly}<br>
     * - {@link #DirectoriesOnly}<br>
     * - {@link #FilesAndDirectories}
     * 
     * @author Hai Bison
     * @since v2.1 alpha
     */
    public static enum FilterMode {
        /**
         * User can choose files only
         */
        FilesOnly,
        /**
         * User can choose directories only
         */
        DirectoriesOnly,
        /**
         * User can choose files or directories
         */
        FilesAndDirectories,
        AnyDirectories
    }// _FilterMode

    /**
     * Sets {@code true} if you want to display hidden files.
     * 
     * @param display
     */
    void setDisplayHiddenFiles(boolean display);

    /**
     * 
     * @return {@code true} if hidden files are displayed
     */
    boolean isDisplayHiddenFiles();

    /**
     * Sets regular expression for filter filename.
     * 
     * @param regex
     */
    void setRegexFilenameFilter(String regex);

    /**
     * 
     * @return the regular expression for file name filter
     */
    String getRegexFilenameFilter();

    /**
     * Sets filter mode.
     * 
     * @param fm
     *            {@link FilterMode}
     */
    void setFilterMode(FilterMode fm);

    /**
     * 
     * @return the {@link FilterMode}
     */
    FilterMode getFilterMode();

    /**
     * Sets sort type.
     * 
     * @param st
     *            {@link SortType}
     */
    void setSortType(SortType st);

    /**
     * 
     * @return the {@link SortType}
     */
    SortType getSortType();

    /**
     * Sets sort order.
     * 
     * @param so
     *            {@link SortOrder}
     */
    void setSortOrder(SortOrder so);

    /**
     * 
     * @return {@link SortOrder}
     */
    SortOrder getSortOrder();

    /**
     * Sets max file count allowed to be listed.
     * 
     * @param max
     */
    void setMaxFileCount(int max);

    /**
     * 
     * @return the max file count allowed to be listed
     */
    int getMaxFileCount();

    /**
     * Gets default path of file provider.
     * 
     * @return {@link IFile}
     */
    IFile defaultPath();

    /**
     * Gets path from pathname.
     * 
     * @param pathname
     *            a {@link String}
     * @return the path from {@code pathname}
     */
    IFile fromPath(String pathname);

    /**
     * Lists files inside {@code dir}, the result should be sorted with
     * {@link SortType} and {@link SortOrder}
     * 
     * @deprecated
     * 
     * @param dir
     *            the root directory which needs to list files
     * @param hasMoreFiles
     *            since Java does not allow variable parameters, so we use this
     *            trick. To use this parameter, set its size to {@code 1}. If
     *            the {@code dir} has more files than max file count allowed,
     *            the element returns {@code true}, otherwise it is
     *            {@code false}
     * @return an array of files, or {@code null} if an exception occurs.
     * @throws a
     *             {@link Exception}
     */
    IFile[] listFiles(IFile dir, boolean[] hasMoreFiles) throws Exception;

    /**
     * Lists files inside {@code dir}, the result should be sorted with
     * {@link SortType} and {@link SortOrder}
     * 
     * @param dir
     *            the root directory which needs to list files
     * @param hasMoreFiles
     *            since Java does not allow variable parameters, so we use this
     *            trick. To use this parameter, set its size to {@code 1}. If
     *            the {@code dir} has more files than max file count allowed,
     *            the element returns {@code true}, otherwise it is
     *            {@code false}
     * @return an array of files, or {@code null} if an exception occurs.
     * @throws a
     *             {@link Exception}
     * @since v4.0 beta
     */
    List<IFile> listAllFiles(IFile dir, boolean[] hasMoreFiles) throws Exception;

    /**
     * Lists all files inside {@code dir}, <b><i>no</b></i> filter.
     * 
     * @param dir
     *            the root directory which needs to list files
     * @return a list of files, or {@code null} if an exception occurs.
     * @throws a
     *             {@link Exception}
     * @since v4.0 beta
     */
    List<IFile> listAllFiles(IFile dir) throws Exception;

    /**
     * Gets a list of the files in the directory represented by {@code dir}.
     * This list is then filtered through a {@link IFileFilter} and matching
     * files are returned as a list of files. Returns {@code null} if
     * {@code dir} is not a directory. If {@code filter} is {@code null} then
     * all files match.
     * 
     * @param dir
     *            an {@link IFile}
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return a list of files or {@code null}.
     * @since v4.3 beta
     */
    List<IFile> listAllFiles(IFile dir, IFileFilter filter);

    /**
     * Filters {@code pathname} based on this file provider configurations.
     * 
     * @param pathname
     *            {@link IFile}
     * @return {@code true} if {@code pathname} passed all filter
     *         configurations, {@code false} otherwise
     * @since v4.3 beta
     */
    boolean accept(IFile pathname);
}
