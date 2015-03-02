package org.geometerplus.android.fbreader.network.action;

import android.app.Activity;
import android.content.Intent;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.OpenFileChooseTree;

public class OpenFileChooseActivity extends Action {
	public final static int FILE_CHOOSER = 100;

	public OpenFileChooseActivity(Activity activity) {
		super(activity, ActionCode.OPEN_FILE_CHOOSE_ACTIVITY, "fileTreeRoot", false);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (tree instanceof OpenFileChooseTree) {
			return true;
		}
		return false;
	}

	@Override
	public void run(NetworkTree tree) {
		Intent i = new Intent();
		i.setClassName("com.yotadevices.yotaphone2.yotareader", "com.yotadevices.yotaphone2.fbreader.FileChooseActivity");
		myActivity.startActivityForResult(i, FILE_CHOOSER);
	}
}
