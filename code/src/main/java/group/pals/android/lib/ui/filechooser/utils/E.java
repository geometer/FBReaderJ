/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Something funny :-)
 * 
 * @author Hai Bison
 * 
 */
public class E {

    /**
     * Shows it!
     * 
     * @param context
     *            {@link Context}
     */
    public static void show(Context context) {
        String msg = null;
        try {
            msg = String.format("Hi  :-)\n\n" + "%s v%s\n" + "…by Hai Bison Apps\n\n" + "http://www.haibison.com\n\n"
                    + "Hope you enjoy this library.", "android-filechooser", "5.0");
        } catch (Exception e) {
            msg = "Oops… You've found a broken Easter egg, try again later  :-(";
        }

        AlertDialog dlg = Dlg.newDlg(context);
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, null, (DialogInterface.OnClickListener) null);
        dlg.setTitle("…");
        dlg.setMessage(msg);
        dlg.show();
    }// show()
}
