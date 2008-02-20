package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;

public class ZLSwingOptionsDialog extends ZLOptionsDialog {
//	private final JFrame myFrame;
	private final boolean myShowApplyButton;
	private final JDialog myDialog;
	private String mySelectedTabKey;
	private final HashMap<ZLSwingDialogContent, String> myTabToKeyMap = new HashMap<ZLSwingDialogContent, String>(); //?
	
	protected ZLSwingOptionsDialog(JFrame frame, ZLResource resource, ZLRunnable applyAction, boolean showApplyButton) {
		super(resource, applyAction);
	//	myFrame = frame;
		myDialog = new JDialog(frame);
		myDialog.setTitle(getCaption());
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
		myDialog.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button1 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.OK_BUTTON);
		JButton button2 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.CANCEL_BUTTON);
		buttonPanel.add(button1);
		buttonPanel.add(button2);
		if (myShowApplyButton) {
			JButton button3 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.APPLY_BUTTON);
			buttonPanel.add(button3);
		}
		myDialog.add(buttonPanel, BorderLayout.SOUTH);
		myDialog.pack();
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setModal(true);
		myDialog.setVisible(true);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void selectTab(String key) {
		// TODO Auto-generated method stub
		
	}

}
