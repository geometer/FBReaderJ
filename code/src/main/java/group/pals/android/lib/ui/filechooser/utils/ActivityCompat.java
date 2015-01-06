/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import android.app.Activity;
import android.os.Build;

/**
 * Helper for accessing features in {@link Activity} introduced in newer API
 * levels in a backwards compatible fashion.<br>
 * <br>
 * <b>Note:</b> You must check API level first with
 * {@link Build.VERSION#SDK_INT} and {@link Build.VERSION_CODES}.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class ActivityCompat {

    /**
     * @see {@link Activity#invalidateOptionsMenu()}
     * @param a
     *            {@link Activity}
     */
    public static void invalidateOptionsMenu(Activity a) {
        a.invalidateOptionsMenu();
    }
}
