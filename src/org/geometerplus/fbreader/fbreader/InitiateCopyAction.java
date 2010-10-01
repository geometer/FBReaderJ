package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import android.widget.Toast;

public class InitiateCopyAction extends FBAction {

	InitiateCopyAction(FBReader fbreader) {
		super(fbreader);
		// TODO Auto-generated constructor stub
	}

	public boolean isEnabled() {
		return Reader.Model != null;
	}

	public void run() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		view.setMode(ZLView.MODE_SELECT);
		Toast.makeText(ZLAndroidApplication.Instance().myMainActivity,
			ZLResource.resource("infoMessage").getResource("selectionStarted").getValue(),
			Toast.LENGTH_SHORT).show();
	}
}
