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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Utilities for context menu.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class ContextMenuUtils {

    /**
     * Shows context menu.
     * 
     * @param context
     *            {@link Context}
     * @param iconId
     *            resource icon ID of the dialog.
     * @param title
     *            title of the dialog.
     * @param itemIds
     *            array of resource IDs of strings.
     * @param listener
     *            {@link OnMenuItemClickListener}
     */
    public static void showContextMenu(Context context, int iconId, String title, final Integer[] itemIds,
            final OnMenuItemClickListener listener) {
        final MenuItemAdapter _adapter = new MenuItemAdapter(context, itemIds);

        View view = LayoutInflater.from(context).inflate(R.layout.afc_context_menu_view, null);
        ListView listview = (ListView) view.findViewById(R.id.afc_context_menu_view_listview_menu);
        listview.setAdapter(_adapter);

        final AlertDialog _dlg = Dlg.newDlg(context);

        // don't use Cancel button
        _dlg.setButton(DialogInterface.BUTTON_NEGATIVE, null, (DialogInterface.OnClickListener) null);
        _dlg.setCanceledOnTouchOutside(true);

        if (iconId > 0)
            _dlg.setIcon(iconId);
        _dlg.setTitle(title);
        _dlg.setView(view);

        if (listener != null) {
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    _dlg.dismiss();
                    listener.onClick(itemIds[position]);
                }// onItemClick()
            });
        }// if listener != null

        _dlg.show();
    }// showContextMenu()

    /**
     * Shows context menu.
     * 
     * @param context
     *            {@link Context}
     * @param iconId
     *            resource icon ID of the dialog.
     * @param titleId
     *            resource ID of the title of the dialog. {@code 0} will be
     *            ignored.
     * @param itemIds
     *            array of resource IDs of strings.
     * @param listener
     *            {@link OnMenuItemClickListener}
     */
    public static void showContextMenu(Context context, int iconId, int titleId, Integer[] itemIds,
            OnMenuItemClickListener listener) {
        showContextMenu(context, iconId, titleId > 0 ? context.getString(titleId) : null, itemIds, listener);
    }// showContextMenu()

    // ==========
    // INTERFACES

    /**
     * @author Hai Bison
     * @since v4.3 beta
     * 
     */
    public static interface OnMenuItemClickListener {

        /**
         * This method will be called after the menu dismissed.
         * 
         * @param resId
         *            the resource ID of the title of the menu item.
         */
        void onClick(int resId);
    }// OnMenuItemClickListener
}
