package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private final HashMap<String, JPanel> myPanelToKeyMap = new HashMap<String, JPanel>();
	private boolean myReturnValue = false;
	
	private final ZLIntegerRangeOption myWidthOption;
	private	final ZLIntegerRangeOption myHeightOption;
	
	protected ZLSwingOptionsDialog(JFrame frame, ZLResource resource, ZLRunnable applyAction, boolean showApplyButton) {
		super(resource, applyAction);
		myDialog = new JDialog(frame);
		myDialog.setTitle(getCaption());
		myShowApplyButton = showApplyButton;
		myWidthOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTION_GROUP_NAME, "Width", 10, 2000, 485);
		myHeightOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTION_GROUP_NAME, "Height", 10, 2000, 332);
	}

	@Override
	public ZLDialogContent createTab(String key) {
		ZLSwingDialogContent tab = new ZLSwingDialogContent(getTabResource(key));
		JPanel contentPanel = tab.getContentPanel();
		JPanel panel = new JPanel(new BorderLayout()); 
		panel.add(contentPanel, BorderLayout.PAGE_START);
		myPanelToKeyMap.put(tab.getKey(), panel);
		myTabbedPane.addTab(tab.getDisplayName(), panel);
		myTabs.add(tab);
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
				myWidthOption.setValue(myDialog.getWidth());
				myHeightOption.setValue(myDialog.getHeight());
			}
		});
		myDialog.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button1 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.OK_BUTTON);
		JButton button2 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.CANCEL_BUTTON);
		CancelAction cancelAction = new CancelAction(button2.getText());
		button1.setAction(new OKAction(button1.getText()));
		button2.setAction(cancelAction);
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
			button3.setAction(new ApplyAction(button3.getText()));
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
		myTabbedPane.addChangeListener(new MyChangeListener());
		if (myPanelToKeyMap.get(mySelectedTabKey) != null) {
			myTabbedPane.setSelectedComponent(myPanelToKeyMap.get(mySelectedTabKey));
		}	
		
		button1.requestFocusInWindow();
		
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		myDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		myDialog.getRootPane().getActionMap().put("ESCAPE", cancelAction);
			
		myDialog.pack();
		
		myDialog.setSize(myWidthOption.getValue(), myHeightOption.getValue());
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setModal(true);
		myDialog.setVisible(true);
		return myReturnValue;
	}

	protected void selectTab(String key) {
		mySelectedTabKey = key;
	}

	private void exitDialog() {
		myWidthOption.setValue(myDialog.getWidth());
		myHeightOption.setValue(myDialog.getHeight());
		myDialog.dispose();
	}
	
	private class MyChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			Component component = myTabbedPane.getSelectedComponent();
			for (Map.Entry entry : myPanelToKeyMap.entrySet()) {
				if (entry.getValue().equals(component)) {
					mySelectedTabKey = (String) entry.getKey();
					break;
				}
			}
		}		
	}
	
	private class ApplyAction extends AbstractAction {
		public ApplyAction(String text) {
			putValue(NAME, text);
		}
		
		public void actionPerformed(ActionEvent e) {
			myReturnValue = false;
			accept();
		}
	}
	
	private class OKAction extends AbstractAction {
		public OKAction(String text) {
			putValue(NAME, text);
		}
		
		public void actionPerformed(ActionEvent e) {
			myReturnValue = true;
			exitDialog();
		}
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction(String text) {
			putValue(NAME, text);
		}
		
		public void actionPerformed(ActionEvent e) {
			exitDialog();
		}		
	}
	
}
