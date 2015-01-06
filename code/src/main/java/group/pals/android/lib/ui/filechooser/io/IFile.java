/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.io;

import android.os.Parcelable;

/**
 * Interface for "file" used in this library. In case you want to use this
 * library for your own file system, have your "file" implement this interface.
 * 
 * @author Hai Bison
 * @since v3.2
 */
public interface IFile extends Parcelable {

    /**
     * Returns the absolute pathname string of this abstract pathname.
     * 
     * @return The absolute pathname string denoting the same file or directory
     *         as this abstract pathname
     * @throws SecurityException
     *             If a required system property value cannot be accessed.
     */
    String getAbsolutePath() throws SecurityException;

    /**
     * Returns the name of the file or directory denoted by this abstract
     * pathname.
     * 
     * @return The name of the file or directory denoted by this abstract
     *         pathname, or the empty string if this pathname's name sequence is
     *         empty
     */
    String getName();
    String getSecondName();
    /**
     * Tests whether the file denoted by this abstract pathname is a directory.
     * 
     * @return {@code true} if and only if the file denoted by this abstract
     *         pathname exists and is a directory; {@code false} otherwise
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    boolean isDirectory() throws SecurityException;

    /**
     * Tests whether the file denoted by this abstract pathname is a normal
     * file.
     * 
     * @return {@code true} if and only if the file denoted by this abstract
     *         pathname exists and is a normal file; {@code false} otherwise
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    boolean isFile() throws SecurityException;

    /**
     * Returns the length of the file denoted by this abstract pathname.
     * 
     * @return The length, in bytes, of the file denoted by this abstract
     *         pathname, or {@code 0L} if the file does not exist. Some
     *         operating systems may return {@code 0L} for pathnames denoting
     *         system-dependent entities such as devices or pipes.
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    long length() throws SecurityException;

    /**
     * Returns the time that the file denoted by this abstract pathname was last
     * modified.
     * 
     * @return A long value representing the time the file was last modified,
     *         measured in milliseconds since the epoch (00:00:00 GMT, January
     *         1, 1970), or {@code 0L} if the file does not exist or if an I/O
     *         error occurs
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    long lastModified() throws SecurityException;

    /**
     * Returns the abstract pathname of this abstract pathname's parent, or
     * {@code null} if this pathname does not name a parent directory.<br>
     * <br>
     * The <i>parent</i> of an abstract pathname consists of the pathname's
     * prefix, if any, and each name in the pathname's name sequence except for
     * the last. If the name sequence is empty then the pathname does not name a
     * parent directory.
     * 
     * @return The abstract pathname of the parent directory named by this
     *         abstract pathname, or {@code null} if this pathname does not name
     *         a parent
     */
    IFile parentFile();

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     * 
     * @return {@code true} if this file exists, {@code false} otherwise.
     */
    public boolean exists();

    /**
     * Creates the directory named by the trailing filename of this file. Does
     * not create the complete path required to create this directory.<br>
     * Note that this method does not throw any exception on failure. Callers
     * must check the return value.
     * 
     * @return {@code true} if the necessary directories have been created,
     *         {@code false} if the target directory already exists or one of
     *         the directories can not be created.
     * @since v4.0 beta
     */
    boolean mkdir();

    /**
     * Deletes this file. Directories must be empty before they will be deleted.<br>
     * Note that this method does not throw any exception on failure. Callers
     * must check the return value.
     * 
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     */
    boolean delete();

    /**
     * Compares to another file by its path name
     * 
     * @param file
     *            {@link IFile}
     * @return {@code true} if this has same path name as {@code file}
     * @since v4.0 beta
     */
    boolean equalsToPath(IFile file);

    /**
     * Clones an instance of this file.
     * 
     * @return {@link IFile}
     * @since v4.3 beta
     */
    IFile clone();

    /**
     * Indicates whether the current context is allowed to read from this file.
     * 
     * @return {@code true} if this file can be read, {@code false} otherwise.
     * @since v4.3 beta
     */
    boolean canRead();
}
