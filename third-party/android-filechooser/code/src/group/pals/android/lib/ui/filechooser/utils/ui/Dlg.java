/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.ui;

import group.pals.android.lib.ui.filechooser.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Utilities for message boxes.
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public class Dlg {

    /**
     * @see Toast#LENGTH_SHORT
     */
    public static final int _LengthShort = android.widget.Toast.LENGTH_SHORT;
    /**
     * @see Toast#LENGTH_LONG
     */
    public static final int _LengthLong = android.widget.Toast.LENGTH_LONG;

    private static android.widget.Toast mToast;

    /**
     * Shows a toast message.
     * 
     * @param context
     *            {@link Context}
     * @param msg
     *            the message.
     * @param duration
     *            can be {@link #_LengthLong} or {@link #_LengthShort}.
     */
    public static void toast(Context context, CharSequence msg, int duration) {
        if (mToast != null)
            mToast.cancel();
        mToast = android.widget.Toast.makeText(context, msg, duration);
        mToast.show();
    }// mToast()

    /**
     * Shows a toast message.
     * 
     * @param context
     *            {@link Context}
     * @param msgId
     *            the resource ID of the message.
     * @param duration
     *            can be {@link #_LengthLong} or {@link #_LengthShort}.
     */
    public static void toast(Context context, int msgId, int duration) {
        toast(context, context.getString(msgId), duration);
    }// mToast()

    /**
     * Shows an info message.
     * 
     * @param context
     *            {@link Context}
     * @param msg
     *            the message.
     */
    public static void showInfo(Context context, CharSequence msg) {
        AlertDialog dlg = newDlg(context);
        dlg.setIcon(android.R.drawable.ic_dialog_info);
        dlg.setTitle(R.string.afc_title_info);
        dlg.setMessage(msg);
        dlg.show();
    }// showInfo()

    /**
     * Shows an info message.
     * 
     * @param context
     *            {@link Context}
     * @param msgId
     *            the resource ID of the message.
     */
    public static void showInfo(Context context, int msgId) {
        showInfo(context, context.getString(msgId));
    }// showInfo()

    /**
     * Shows an error message.
     * 
     * @param context
     *            {@link Context}
     * @param msg
     *            the message.
     * @param listener
     *            will be called after the user cancelled the dialog.
     */
    public static void showError(Context context, CharSequence msg, DialogInterface.OnCancelListener listener) {
        AlertDialog dlg = newDlg(context);
        dlg.setIcon(android.R.drawable.ic_dialog_alert);
        dlg.setTitle(R.string.afc_title_error);
        dlg.setMessage(msg);
        dlg.setOnCancelListener(listener);
        dlg.show();
    }// showError()

    /**
     * Shows an error message.
     * 
     * @param context
     *            {@link Context}
     * @param msgId
     *            the resource ID of the message.
     * @param listener
     *            will be called after the user cancelled the dialog.
     */
    public static void showError(Context context, int msgId, DialogInterface.OnCancelListener listener) {
        showError(context, context.getString(msgId), listener);
    }// showError()

    /**
     * Shows an unknown error.
     * 
     * @param context
     *            {@link Context}
     * @param t
     *            the {@link Throwable}
     * @param listener
     *            will be called after the user cancelled the dialog.
     */
    public static void showUnknownError(Context context, Throwable t, DialogInterface.OnCancelListener listener) {
        showError(context, String.format(context.getString(R.string.afc_pmsg_unknown_error), t), listener);
    }// showUnknownError()

    /**
     * Shows a confirmation dialog.
     * 
     * @param context
     *            {@link Context}
     * @param msg
     *            the message.
     * @param onYes
     *            will be called if the user selects positive answer (a
     *            <i>Yes</i> or <i>OK</i>).
     * @param onNo
     *            will be called after the user cancelled the dialog.
     */
    public static void confirmYesno(Context context, CharSequence msg, DialogInterface.OnClickListener onYes,
            DialogInterface.OnCancelListener onNo) {
        AlertDialog dlg = newDlg(context);
        dlg.setIcon(android.R.drawable.ic_dialog_alert);
        dlg.setTitle(R.string.afc_title_confirmation);
        dlg.setMessage(msg);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.yes), onYes);
        dlg.setOnCancelListener(onNo);
        dlg.show();
    }

    /**
     * Shows a confirmation dialog.
     * 
     * @param context
     *            {@link Context}
     * @param msg
     *            the message.
     * @param onYes
     *            will be called if the user selects positive answer (a
     *            <i>Yes</i> or <i>OK</i>).
     */
    public static void confirmYesno(Context context, CharSequence msg, DialogInterface.OnClickListener onYes) {
        confirmYesno(context, msg, onYes, null);
    }// confirmYesno()

    /**
     * Creates new {@link AlertDialog}. Set canceled on touch outside to
     * {@code true}.
     * 
     * @param context
     *            {@link Context}
     * @return {@link AlertDialog}
     * @since v4.3 beta
     */
    public static AlertDialog newDlg(Context context) {
        AlertDialog res = newDlgBuilder(context).create();
        res.setCanceledOnTouchOutside(true);
        return res;
    }// newDlg()

    /**
     * Creates new {@link AlertDialog.Builder}.
     * 
     * @param context
     *            {@link Context}
     * @return {@link AlertDialog}
     * @since v4.3 beta
     */
    public static AlertDialog.Builder newDlgBuilder(Context context) {
        return new AlertDialog.Builder(context);
    }// newDlgBuilder()
}
