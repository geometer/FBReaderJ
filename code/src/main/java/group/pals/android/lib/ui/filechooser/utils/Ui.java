/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

/**
 * UI utilities.
 * 
 * @author Hai Bison
 * 
 */
public class Ui {

    public static void hideSoftKeyboard(Context context, IBinder iBinder) {
        /*
         * hide soft keyboard
         * http://stackoverflow.com/questions/1109022/how-to-close
         * -hide-the-android-soft-keyboard
         */
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(iBinder, 0);
    }
}
