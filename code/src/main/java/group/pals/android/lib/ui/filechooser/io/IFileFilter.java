/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.io;

/**
 * An interface for filtering {@link IFile} objects based on their names or
 * other arbitrary conditions.
 * 
 * @author Hai Bison
 * @since v4.3 beta 1
 * 
 */
public interface IFileFilter {

    /**
     * Indicating whether a specific file should be included in a pathname list.
     * 
     * @param pathname
     *            the abstract file to check.
     * @return {@code true} if the file should be included, {@code false}
     *         otherwise.
     */
    boolean accept(IFile pathname);
}
