/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

public class Converter {

    /**
     * Converts {@code size} (in bytes) to string. This tip is from:
     * http://stackoverflow.com/a/5599842/942821
     * 
     * @param size
     *            the size in bytes.
     * @return e.g.:<br>
     *         - 128 B<br>
     *         - 1.5 KB<br>
     *         - 10 MB<br>
     *         - ...
     */
    public static String sizeToStr(double size) {
        if (size <= 0)
            return "0 B";
        final String[] _units = new String[] { "B", "KB", "MB", "GB", "TB" };
        final Short _blockSize = 1024;

        int digitGroups = (int) (Math.log10(size) / Math.log10(_blockSize));
        if (digitGroups >= _units.length)
            digitGroups = _units.length - 1;
        size = size / Math.pow(_blockSize, digitGroups);

        return String.format(String.format("%s %%s", digitGroups == 0 ? "%,.0f" : "%,.2f"), size, _units[digitGroups]);
    }// sizeToStr()
}
