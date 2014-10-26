package com.yotadevices.yotaphone2.fbreader.actions;

import android.content.Intent;

import com.yotadevices.yotaphone2.fbreader.FBBSAction;
import com.yotadevices.yotaphone2.fbreader.FBReaderYotaService;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class ShowBSLibraryAction extends FBBSAction {
	public ShowBSLibraryAction(FBReaderYotaService bsActivity, FBReaderApp fbreader) {
		super(bsActivity, fbreader);
	}

	@Override
	protected void run(Object... params) {
		Intent i = new Intent();
		i.setClassName("com.yotadevices.yotaphone2.yotareader.collection",
				"com.yotadevices.yotaphone2.yotareader.collection.CollectionBSActivity");
		mBSActivity.startService(i);
	}
}
