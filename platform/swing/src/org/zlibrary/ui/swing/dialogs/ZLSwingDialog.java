package org.zlibrary.ui.swing.dialogs;

import org.zlibrary.core.dialogs.ZLDialog;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JButton;

public class ZLSwingDialog extends ZLDialog {
	private final JDialog myDialog;

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
		myDialog.add(new JButton(key));
	}

	public boolean run() {
		myDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myWidthOption.setValue(myDialog.getWidth());
				myHeightOption.setValue(myDialog.getHeight());
			}
		});
		myDialog.setLayout(new BorderLayout());

		myDialog.pack();

		myDialog.setSize(myWidthOption.getValue(), myHeightOption.getValue());
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setModal(true);
		myDialog.setVisible(true);
		return true;
	}
}
