/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.prefs;

import group.pals.android.lib.ui.filechooser.FileChooserActivity.ViewType;
import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.SortType;
import android.content.Context;

/**
 * Display preferences.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class DisplayPrefs extends Prefs {

    /**
     * Delay time for waiting for other threads inside a thread... This is in
     * milliseconds.
     */
    public static final int _DelayTimeWaitingThreads = 10;

    /**
     * Default history capacity. Because we need to check duplicates before
     * showing history list, this value should be small.
     */
    public static final int _DefHistoryCapacity = 51;

    /**
     * Gets view type.
     * 
     * @param c
     *            {@link Context}
     * @return {@link ViewType}
     */
    public static ViewType getViewType(Context c) {
        return ViewType.List.ordinal() == p(c).getInt(c.getString(R.string.afc_pkey_display_view_type),
                c.getResources().getInteger(R.integer.afc_pkey_display_view_type_def)) ? ViewType.List : ViewType.Grid;
    }

    /**
     * Sets view type.
     * 
     * @param c
     *            {@link Context}
     * @param v
     *            {@link ViewType}, if {@code null}, default value will be used.
     */
    public static void setViewType(Context c, ViewType v) {
        String key = c.getString(R.string.afc_pkey_display_view_type);
        if (v == null)
            p(c).edit().putInt(key, c.getResources().getInteger(R.integer.afc_pkey_display_view_type_def)).commit();
        else
            p(c).edit().putInt(key, v.ordinal()).commit();
    }

    /**
     * Gets sort type.
     * 
     * @param c
     *            {@link Context}
     * @return {@link SortType}
     */
    public static SortType getSortType(Context c) {
        for (SortType s : SortType.values())
            if (s.ordinal() == p(c).getInt(c.getString(R.string.afc_pkey_display_sort_type),
                    c.getResources().getInteger(R.integer.afc_pkey_display_sort_type_def)))
                return s;
        return SortType.SortByName;
    }

    /**
     * Sets {@link SortType}
     * 
     * @param c
     *            {@link Context}
     * @param v
     *            {@link SortType}, if {@code null}, default value will be used.
     */
    public static void setSortType(Context c, SortType v) {
        String key = c.getString(R.string.afc_pkey_display_sort_type);
        if (v == null)
            p(c).edit().putInt(key, c.getResources().getInteger(R.integer.afc_pkey_display_sort_type_def)).commit();
        else
            p(c).edit().putInt(key, v.ordinal()).commit();
    }

    /**
     * Gets sort ascending.
     * 
     * @param c
     *            {@link Context}
     * @return {@code true} if sort is ascending, {@code false} otherwise.
     */
    public static boolean isSortAscending(Context c) {
        return p(c).getBoolean(c.getString(R.string.afc_pkey_display_sort_ascending),
                c.getResources().getBoolean(R.bool.afc_pkey_display_sort_ascending_def));
    }

    /**
     * Sets sort ascending.
     * 
     * @param c
     *            {@link Context}
     * @param v
     *            {@link Boolean}, if {@code null}, default value will be used.
     */
    public static void setSortAscending(Context c, Boolean v) {
        if (v == null)
            v = c.getResources().getBoolean(R.bool.afc_pkey_display_sort_ascending_def);
        p(c).edit().putBoolean(c.getString(R.string.afc_pkey_display_sort_ascending), v).commit();
    }

    /**
     * Checks setting of showing time for old days in this year. Default is
     * {@code false}.
     * 
     * @param c
     *            {@link Context}.
     * @return {@code true} or {@code false}.
     * @since v4.7 beta
     */
    public static boolean isShowTimeForOldDaysThisYear(Context c) {
        return p(c).getBoolean(c.getString(R.string.afc_pkey_display_show_time_for_old_days_this_year),
                c.getResources().getBoolean(R.bool.afc_pkey_display_show_time_for_old_days_this_year_def));
    }

    /**
     * Enables or disables showing time of old days in this year.
     * 
     * @param c
     *            {@link Context}.
     * @param v
     *            your preferred flag. If {@code null}, default will be used (
     *            {@code false}).
     * @since v4.7 beta
     */
    public static void setShowTimeForOldDaysThisYear(Context c, Boolean v) {
        if (v == null)
            v = c.getResources().getBoolean(R.bool.afc_pkey_display_show_time_for_old_days_this_year_def);
        p(c).edit().putBoolean(c.getString(R.string.afc_pkey_display_show_time_for_old_days_this_year), v).commit();
    }

    /**
     * Checks setting of showing time for old days in last year and older.
     * Default is {@code false}.
     * 
     * @param c
     *            {@link Context}.
     * @return {@code true} or {@code false}.
     * @since v4.7 beta
     */
    public static boolean isShowTimeForOldDays(Context c) {
        return p(c).getBoolean(c.getString(R.string.afc_pkey_display_show_time_for_old_days),
                c.getResources().getBoolean(R.bool.afc_pkey_display_show_time_for_old_days_def));
    }

    /**
     * Enables or disables showing time of old days in last year and older.
     * 
     * @param c
     *            {@link Context}.
     * @param v
     *            your preferred flag. If {@code null}, default will be used (
     *            {@code false}).
     * @since v4.7 beta
     */
    public static void setShowTimeForOldDays(Context c, Boolean v) {
        if (v == null)
            v = c.getResources().getBoolean(R.bool.afc_pkey_display_show_time_for_old_days_def);
        p(c).edit().putBoolean(c.getString(R.string.afc_pkey_display_show_time_for_old_days), v).commit();
    }

    /**
     * Checks if remembering last location is enabled or not.
     * 
     * @param c
     *            {@link Context}.
     * @return {@code true} if remembering last location is enabled.
     * @since v4.7 beta
     */
    public static boolean isRememberLastLocation(Context c) {
        return p(c).getBoolean(c.getString(R.string.afc_pkey_display_remember_last_location),
                c.getResources().getBoolean(R.bool.afc_pkey_display_remember_last_location_def));
    }

    /**
     * Enables or disables remembering last location.
     * 
     * @param c
     *            {@link Context}.
     * @param v
     *            your preferred flag. If {@code null}, default will be used (
     *            {@code true}).
     * @since v4.7 beta
     */
    public static void setRememberLastLocation(Context c, Boolean v) {
        if (v == null)
            v = c.getResources().getBoolean(R.bool.afc_pkey_display_remember_last_location_def);
        p(c).edit().putBoolean(c.getString(R.string.afc_pkey_display_remember_last_location), v).commit();
    }

    /**
     * Gets last location.
     * 
     * @param c
     *            {@link Context}.
     * @return the last location, or {@code null} if not available.
     * @since v4.7 beta
     */
    public static String getLastLocation(Context c) {
        return p(c).getString(c.getString(R.string.afc_pkey_display_last_location), null);
    }

    /**
     * Sets last location.
     * 
     * @param c
     *            {@link Context}.
     * @param v
     *            the last location.
     */
    public static void setLastLocation(Context c, String v) {
        p(c).edit().putString(c.getString(R.string.afc_pkey_display_last_location), v).commit();
    }

    /*
     * HELPER CLASSES
     */

    /**
     * File time display options.
     * 
     * @see DisplayPrefs#isShowTimeForOldDaysThisYear(Context)
     * @see DisplayPrefs#isShowTimeForOldDays(Context)
     * @author Hai Bison
     * @since v4.9 beta
     * 
     */
    public static class FileTimeDisplay {

        private boolean mShowTimeForOldDaysThisYear;
        private boolean mShowTimeForOldDays;

        /**
         * Creates new instance.
         * 
         * @param showTimeForOldDaysThisYear
         * @param showTimeForOldDays
         */
        public FileTimeDisplay(boolean showTimeForOldDaysThisYear, boolean showTimeForOldDays) {
            mShowTimeForOldDaysThisYear = showTimeForOldDaysThisYear;
            mShowTimeForOldDays = showTimeForOldDays;
        }// FileTimeDisplay()

        public boolean isShowTimeForOldDaysThisYear() {
            return mShowTimeForOldDaysThisYear;
        }// isShowTimeForOldDaysThisYear()

        public FileTimeDisplay setShowTimeForOldDaysThisYear(boolean v) {
            mShowTimeForOldDaysThisYear = v;
            return this;
        }// setShowTimeForOldDaysThisYear()

        public boolean isShowTimeForOldDays() {
            return mShowTimeForOldDays;
        }// isShowTimeForOldDays()

        public FileTimeDisplay setShowTimeForOldDays(boolean v) {
            mShowTimeForOldDays = v;
            return this;
        }// setShowTimeForOldDays()
    }// FileTimeDisplay
}
