package org.geometerplus.android.fbreader.network;


import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yotadevices.yotaphone2.fbreader.UIUtils;

import org.geometerplus.android.fbreader.covers.CoverManager;
import org.geometerplus.android.fbreader.network.action.NetworkBookActions;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.zlibrary.ui.android.R;

public class YotaNetworkLibraryAdapter extends NetworkLibraryAdapter {
	private final static int ROOT_ITEM = 1;
	private final static int SUB_TREE = 2;
	private final static int[] COLORS = {R.color.yota_networklibrary_color1, R.color.yota_networklibrary_color2,
			R.color.yota_networklibrary_color3, R.color.yota_networklibrary_color4};
	YotaNetworkLibraryAdapter(NetworkLibraryActivity activity) {
		super(activity);
	}

	@Override
	public View getView(int position, View view, final ViewGroup parent) {
		final NetworkTree tree = (NetworkTree)getItem(position);
		if (tree == null) {
			throw new IllegalArgumentException("tree == null");
		}
		if (view == null) {
			if (tree.Level > 1) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
				view.setPadding((int) UIUtils.convertDpToPixel(15, getActivity()), 0,
						(int)UIUtils.convertDpToPixel(15, getActivity()), 0);
				if (myCoverManager == null) {
					view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					final int coverHeight = view.getMeasuredHeight();
					final TreeActivity activity = getActivity();
					myCoverManager = new CoverManager(activity, activity.ImageSynchronizer, coverHeight * 15 / 32, coverHeight);
					view.requestLayout();
				}
				view.setTag(SUB_TREE);
			}
			else {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.yota_networklibrary_item, parent, false);
				view.setTag(ROOT_ITEM);
			}
		} else {
			int tag = (Integer)view.getTag();
			if (tree.Level == 1 && tag != ROOT_ITEM) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.yota_networklibrary_item, parent, false);
				view.setTag(ROOT_ITEM);
			}
			if (tree.Level > 1 && tag != SUB_TREE) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
				view.setPadding((int) UIUtils.convertDpToPixel(15, getActivity()), 0,
						(int)UIUtils.convertDpToPixel(15, getActivity()), 0);
				view.setTag(SUB_TREE);
				if (myCoverManager == null) {
					view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					final int coverHeight = view.getMeasuredHeight();
					final TreeActivity activity = getActivity();
					myCoverManager = new CoverManager(activity, activity.ImageSynchronizer, coverHeight * 15 / 32, coverHeight);
					view.requestLayout();
				}
			}
		}

		if (tree.Level > 1) {
			setupCover(ViewUtil.findImageView(view, R.id.library_tree_item_icon), tree);

			final ImageView statusView = ViewUtil.findImageView(view, R.id.library_tree_item_status);
			final int status = (tree instanceof NetworkBookTree)
					? NetworkBookActions.getBookStatus(
					((NetworkBookTree) tree).Book,
					((NetworkLibraryActivity) getActivity()).BookCollection,
					((NetworkLibraryActivity) getActivity()).Connection
			)
					: 0;
			if (status != 0) {
				statusView.setVisibility(View.VISIBLE);
				statusView.setImageResource(status);
			} else {
				statusView.setVisibility(View.GONE);
			}
			statusView.requestLayout();
			TextView itemName = ViewUtil.findTextView(view, R.id.library_tree_item_name);
			itemName.setText(tree.getName());
			itemName.setTypeface(Typeface.create("PT Sans Yota", Typeface.BOLD));
			TextView summary = ViewUtil.findTextView(view, R.id.library_tree_item_childrenlist);
			summary.setText(tree.getSummary());
			summary.setTypeface(Typeface.create("PT Sans Yota", Typeface.NORMAL));

		} else {
			int colorpos = position % (COLORS.length);
			final Drawable d = getActivity().getResources().getDrawable(COLORS[colorpos]);
			View root = view.findViewById(R.id.root_layout);
			root.setBackground(d);
			TextView itemName = ViewUtil.findTextView(view, R.id.library_tree_item_name);
			itemName.setText(tree.getName());
			itemName.setTypeface(Typeface.create("serif", Typeface.BOLD));
			TextView summary = ViewUtil.findTextView(view, R.id.library_tree_item_childrenlist);
			summary.setText(tree.getSummary());
			summary.setTypeface(Typeface.create("serif", Typeface.ITALIC));
		}

		return view;

	}
}
