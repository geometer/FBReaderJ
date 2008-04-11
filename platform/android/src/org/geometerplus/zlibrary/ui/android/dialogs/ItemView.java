package org.geometerplus.zlibrary.ui.android.dialogs;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLTreeNode;

import org.geometerplus.zlibrary.ui.android.library.R;

class ItemView extends LinearLayout {
	static private final HashMap ourIconMap = new HashMap();

	private final ZLTreeNode myNode;

	ItemView(Context context, ZLTreeNode node) {
		super(context);

		myNode = node;

		final ImageView imageView = new ImageView(context);
		String iconName = node.pixmapName();
		Drawable icon = (Drawable)ourIconMap.get(iconName);
		if (icon == null) {
			try {
				int resourceId = R.drawable.class.getField("filetree__" + iconName).getInt(null);
				icon = context.getResources().getDrawable(resourceId);
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {
			}
		}
		if (icon != null) {
			imageView.setImageDrawable(icon);
		}
		imageView.setPadding(0, 2, 5, 0);
		addView(imageView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		final TextView textView = new TextView(context);
		textView.setPadding(0, 2, 0, 0);
		textView.setTextSize(18);
		textView.setText(node.displayName());
		addView(textView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	ZLTreeNode getNode() {
		return myNode;
	}
}
