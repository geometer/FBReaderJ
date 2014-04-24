/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.ui;

import group.pals.android.lib.ui.filechooser.IFileAdapter;
import group.pals.android.lib.ui.filechooser.IFileDataModel;
import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.FilterMode;
import group.pals.android.lib.ui.filechooser.utils.history.History;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Utilities for editor context menu.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class ViewFilesContextMenuUtils {

    /**
     * Shows history contents to the user. He can clear all items.
     * 
     * @param context
     *            {@link Context}
     * @param fileProvider
     *            {@link IFileProvider}
     * @param history
     *            {@link History} of {@link IFile}.
     * @param currentLocation
     *            current location, will not be shown.
     * @param listener
     *            will be notified after the user closed the dialog, or when the
     *            user selects an item.
     */
    public static void doShowHistoryContents(final Context context, final IFileProvider fileProvider,
            final History<IFile> history, IFile currentLocation, final TaskListener listener) {
        if (history.isEmpty())
            return;

        final AlertDialog _dialog = Dlg.newDlg(context);
        // don't use Cancel button
        _dialog.setButton(DialogInterface.BUTTON_NEGATIVE, null, (DialogInterface.OnClickListener) null);
        _dialog.setIcon(android.R.drawable.ic_dialog_info);
        _dialog.setTitle(R.string.afc_title_history);

        List<IFileDataModel> data = new ArrayList<IFileDataModel>();
        ArrayList<IFile> items = history.items();
        for (int i = items.size() - 1; i >= 0; i--) {
            IFile f = items.get(i);
            if (f == currentLocation)
                continue;

            // check for duplicates
            boolean duplicated = false;
            for (int j = 0; j < data.size(); j++) {
                if (f.equalsToPath(data.get(j).getFile())) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                data.add(new IFileDataModel(f));
        }

        final IFileAdapter _adapter = new IFileAdapter(context, data, FilterMode.DirectoriesOnly, false);

        ListView listView = (ListView) LayoutInflater.from(context).inflate(R.layout.afc_listview_files, null);
        listView.setBackgroundResource(0);
        listView.setFastScrollEnabled(true);
        listView.setAdapter(_adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    _dialog.dismiss();
                    listener.onFinish(true, _adapter.getItem(position).getFile());
                }
            }
        });// OnItemClickListener

        _dialog.setView(listView);
        _dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.afc_cmd_clear),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        history.clear();
                    }
                });
        _dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null)
                    listener.onFinish(true, null);
            }
        });
        _dialog.show();
    }// doShowHistoryContents()
}
