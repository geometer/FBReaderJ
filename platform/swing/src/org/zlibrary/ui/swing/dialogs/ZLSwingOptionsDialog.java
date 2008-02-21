package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.*;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;

public class ZLSwingOptionsDialog extends ZLOptionsDialog {
	private final boolean myShowApplyButton;
	private final JDialog myDialog;
	private final JTabbedPane myTabbedPane = new JTabbedPane();
	private String mySelectedTabKey;
	private final HashMap<ZLSwingDialogContent, String> myTabToKeyMap = new HashMap<ZLSwingDialogContent, String>(); //?
	
	protected ZLSwingOptionsDialog(JFrame frame, ZLResource resource, ZLRunnable applyAction, boolean showApplyButton) {
		super(resource, applyAction);
		myDialog = new JDialog(frame);
		myDialog.setTitle(getCaption());
		myShowApplyButton = showApplyButton;
		// TODO Auto-generated constructor stub
	}

	@Override
	public ZLDialogContent createTab(String key) {
		ZLSwingDialogContent tab = new ZLSwingDialogContent(getTabResource(key));
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400, 300));
//		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		myTabbedPane.addTab(tab.getDisplayName(), panel);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getSelectedTabKey() {
		return mySelectedTabKey;
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
		if (button1.getPreferredSize().width < button2.getPreferredSize().width) {
			button1.setPreferredSize(button2.getPreferredSize());
		} else {
			button2.setPreferredSize(button1.getPreferredSize());
		}
		if (myShowApplyButton) {
			JButton button3 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.APPLY_BUTTON);
			buttonPanel.add(button3);
			if (button3.getPreferredSize().width < button2.getPreferredSize().width) {
				button3.setPreferredSize(button2.getPreferredSize());
			} else {
				button2.setPreferredSize(button3.getPreferredSize());
				button1.setPreferredSize(button3.getPreferredSize());
			}
		}
		myDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		myTabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				
		myDialog.getContentPane().add(myTabbedPane, BorderLayout.NORTH);
		
		myDialog.pack();
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setResizable(false);
//		myDialog.setSize(800, 600);
		myDialog.setModal(true);
		myDialog.setVisible(true);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void selectTab(String key) {
		mySelectedTabKey = key;
	}

}
