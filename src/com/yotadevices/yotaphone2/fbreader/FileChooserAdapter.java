package com.yotadevices.yotaphone2.fbreader;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.geometerplus.android.fbreader.covers.CoverManager;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.fbreader.tree.TreeAdapter;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.android.R;

public class FileChooserAdapter extends TreeAdapter {
    private CoverManager mCoverManager;

    protected FileChooserAdapter(TreeActivity activity) {
        super(activity);
    }

    private View createView(View convertView, ViewGroup parent, LibraryTree tree) {
        final View view = (convertView != null) ? convertView :
                LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
        view.setPadding((int)UIUtils.convertDpToPixel(6, getActivity()), 0,
                (int)UIUtils.convertDpToPixel(6, getActivity()), 0);

        final boolean unread =
                tree.getBook() != null && !tree.getBook().labels().contains(Book.READ_LABEL);

        final TextView nameView = ViewUtil.findTextView(view, R.id.library_tree_item_name);
        if (unread) {
            nameView.setText(Html.fromHtml("<b>" + tree.getName()));
        } else {
            nameView.setText(tree.getName());
        }
        nameView.setTypeface(Typeface.create("PT Sans Pro", Typeface.BOLD));

        final TextView summaryView = ViewUtil.findTextView(view, R.id.library_tree_item_childrenlist);
        if (unread) {
            summaryView.setText(Html.fromHtml("<b>" + tree.getSummary()));
        } else {
            summaryView.setText(tree.getSummary());
        }
        summaryView.setTextColor(Color.GRAY);
        nameView.setTypeface(Typeface.create("PT Sans Pro", Typeface.NORMAL));
        return view;
    }

    public View getView(int position, View convertView, final ViewGroup parent) {
        final LibraryTree tree = (LibraryTree)getItem(position);
        final View view = createView(convertView, parent, tree);
        if (getActivity().isTreeSelected(tree)) {
            view.setBackgroundColor(0xff555555);
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        if (mCoverManager == null) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            final int coverHeight = view.getMeasuredHeight();
            final TreeActivity activity = getActivity();
            mCoverManager = new CoverManager(activity, activity.ImageSynchronizer, coverHeight * 15 / 32, coverHeight);
            view.requestLayout();
        }

        final ImageView coverView = ViewUtil.findImageView(view, R.id.library_tree_item_icon);
        if (!mCoverManager.trySetCoverImage(coverView, tree)) {
            coverView.setImageResource(getCoverResourceId(tree));
        }

        return view;
    }

    private int getCoverResourceId(LibraryTree tree) {
        if (tree.getBook() != null) {
            return R.drawable.ic_list_library_book;
        } else if (tree instanceof FileTree) {
            final ZLFile file = ((FileTree)tree).getFile();
            if (file.isArchive()) {
                return R.drawable.ic_list_library_zip;
            } else if (file.isDirectory() && file.isReadable()) {
                return R.drawable.ic_list_library_folder;
            } else {
                return R.drawable.ic_list_library_permission_denied;
            }
        }
        return R.drawable.ic_list_library_books;
    }
}
