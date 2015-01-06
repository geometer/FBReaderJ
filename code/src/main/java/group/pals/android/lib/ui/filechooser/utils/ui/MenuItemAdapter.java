/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.ui;

import group.pals.android.lib.ui.filechooser.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for context menu.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class MenuItemAdapter extends BaseAdapter {

    private final Context mContext;
    private final Integer[] mItems;
    private final int mPadding;
    private final int mItemPaddingLeft;

    /**
     * Creates new instance.<br>
     * 
     * @param context
     *            {@link Context}
     * @param itemIds
     *            array of resource IDs of titles to be used.
     */
    public MenuItemAdapter(Context context, Integer[] itemIds) {
        mContext = context;
        mItems = itemIds;

        mPadding = mContext.getResources().getDimensionPixelSize(R.dimen.afc_5dp);
        mItemPaddingLeft = mContext.getResources()
                .getDimensionPixelSize(R.dimen.afc_context_menu_item_padding_left);
    }// MenuItemAdapter()

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.afc_context_menu_tiem, null);
        }

        ((TextView) convertView).setText(mItems[position]);
        convertView.setPadding(mItemPaddingLeft, mPadding, mPadding, mPadding);

        return convertView;
    }
}