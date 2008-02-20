package org.zlibrary.ui.swing.dialogs;

import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;

public class ZLSwingOptionsDialog extends ZLOptionsDialog {
	private final JFrame myFrame;
	private final boolean myShowApplyButton;
	private final JDialog myDialog = new JDialog();
	private String mySelectedTabKey;
	private final HashMap<ZLSwingDialogContent, String> myTabToKeyMap = new HashMap<ZLSwingDialogContent, String>(); //?
	
	protected ZLSwingOptionsDialog(JFrame frame, ZLResource resource, ZLRunnable applyAction, boolean showApplyButton) {
		super(resource, applyAction);
		myFrame = frame;
		myShowApplyButton = showApplyButton;
		// TODO Auto-generated constructor stub
	}

	@Override
	public ZLDialogContent createTab(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSelectedTabKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean runInternal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void selectTab(String key) {
		// TODO Auto-generated method stub
		
	}

}
