package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;

public class ZLSwingOptionsDialog extends ZLOptionsDialog {
	private static final String OPTION_GROUP_NAME = "OptionsDialog";
	private final boolean myShowApplyButton;
	private final JDialog myDialog;
	private final JTabbedPane myTabbedPane = new JTabbedPane();
	private String mySelectedTabKey;
	private final HashMap<JPanel, String> myPanelToKeyMap = new HashMap<JPanel, String>(); //?
	
//	private final ZLIntegerRangeOption myWidthOption;
//	private	final ZLIntegerRangeOption myHeightOption;
	
	protected ZLSwingOptionsDialog(JFrame frame, ZLResource resource, ZLRunnable applyAction, boolean showApplyButton) {
		super(resource, applyAction);
		myDialog = new JDialog(frame);
		myDialog.setTitle(getCaption());
		myShowApplyButton = showApplyButton;
//		myWidthOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTION_GROUP_NAME, "Width", 10, 2000, 500);
//		myHeightOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTION_GROUP_NAME, "Height", 10, 2000, 400);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ZLDialogContent createTab(String key) {
		ZLSwingDialogContent tab = new ZLSwingDialogContent(getTabResource(key));
		JPanel contentPanel = tab.getContentPanel();
		JPanel panel = new JPanel(new BorderLayout()); 
		panel.add(contentPanel, BorderLayout.PAGE_START);
		myPanelToKeyMap.put(panel, tab.getKey());
		myTabbedPane.addTab(tab.getDisplayName(), panel);
		// TODO Auto-generated method stub
		return tab;
	}

	@Override
	protected String getSelectedTabKey() {
		return mySelectedTabKey;
	}

	@Override
	protected boolean runInternal() {
		myDialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
//				System.out.println(myDialog.getWidth() + " width");
//				System.out.println(myDialog.getHeight() + " height");
//				myWidthOption.setValue(myDialog.getWidth());
//				myHeightOption.setValue(myDialog.getHeight());
			}
		});
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
		myDialog.getContentPane().add(myTabbedPane, BorderLayout.CENTER);
		
		myDialog.pack();
		button1.requestFocusInWindow();
		myDialog.setLocationRelativeTo(myDialog.getParent());
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
