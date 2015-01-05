/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.prefs;

import group.pals.android.lib.ui.filechooser.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Convenient class for working with preferences.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 */
public class Prefs {

    /**
     * This unique ID is used for storing preferences.
     * 
     * @since v4.9 beta
     */
    public static final String _Uid = "9795e88b-2ab4-4b81-a548-409091a1e0c6";

    /**
     * Generates global preference filename of this library.
     * 
     * @param context
     *            {@link Context} - will be used to obtain the application
     *            context.
     * @return the global preference filename.
     */
    public static final String genPreferenceFilename(Context context) {
        return String.format("%s_%s", context.getString(R.string.afc_lib_name), _Uid);
    }

    /**
     * Gets new {@link SharedPreferences}
     * 
     * @param context
     *            {@link Context}
     * @return {@link SharedPreferences}
     */
    public static SharedPreferences p(Context context) {
        // always use application context
        return context.getApplicationContext().getSharedPreferences(genPreferenceFilename(context),
                Context.MODE_MULTI_PROCESS);
    }

    /**
     * Setup {@code pm} to use global unique filename and global access mode.
     * You must use this method if you let the user change preferences via UI
     * (such as {@link PreferenceActivity}, {@link PreferenceFragment}...).
     * 
     * @param c
     *            {@link Context}.
     * @param pm
     *            {@link PreferenceManager}.
     * @since v4.9 beta
     */
    public static void setupPreferenceManager(Context c, PreferenceManager pm) {
        pm.setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        pm.setSharedPreferencesName(genPreferenceFilename(c));
    }// setupPreferenceManager()
}
