package org.zlibrary.ui.swing.dialogs;

import org.zlibrary.core.dialogs.ZLDialog;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ZLSwingDialog extends ZLDialog {
	private final JDialog myDialog;
	private final JPanel buttonPanel = new JPanel();
	
	private boolean myReturnValue = false;

	public final ZLIntegerRangeOption myWidthOption;
	public final ZLIntegerRangeOption myHeightOption;

	public ZLSwingDialog(JFrame frame, ZLResource resource) {
		super();
		myTab = new ZLSwingDialogContent(resource);
		myDialog = new JDialog(frame);
		myDialog.setTitle(resource.getResource(ZLDialogManager.DIALOG_TITLE).getValue());
		final String optionGroupName = resource.Name;
		myWidthOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, optionGroupName, "Width", 10, 2000, 485);	
		myHeightOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, optionGroupName, "Height", 10, 2000, 332);	
	}

	public void addButton(final String key, boolean accept) {
		JButton button;
		if (accept) {
			button = new JButton(new OKAction(ZLDialogManager.getButtonText(key).replace("&", "")));
		} else {
			button = new JButton(new CancelAction(ZLDialogManager.getButtonText(key).replace("&", "")));
		}
		buttonPanel.add(button);
	}

	public boolean run() {
		myDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myWidthOption.setValue(myDialog.getWidth());
				myHeightOption.setValue(myDialog.getHeight());
			}
		});
		myDialog.setLayout(new BorderLayout());
		myDialog.add(((ZLSwingDialogContent) myTab).getContentPanel(), BorderLayout.PAGE_START);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buttonPanel, BorderLayout.EAST);
		myDialog.add(panel, BorderLayout.PAGE_END);
		myDialog.pack();

		myDialog.setSize(myWidthOption.getValue(), myHeightOption.getValue());
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setModal(true);
		myDialog.setVisible(true);
		return myReturnValue;
	}
	
	private void exitDialog() {
		myWidthOption.setValue(myDialog.getWidth());
		myHeightOption.setValue(myDialog.getHeight());
		myDialog.dispose();
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
