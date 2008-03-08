package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.zlibrary.core.dialogs.ZLOptionView;
import org.zlibrary.core.dialogs.ZLStringOptionEntry;

public class ZLStringOptionView extends ZLOptionView {

	private final JLabel myLabel;
	private final JTextField myTextField;
	
	public ZLStringOptionView(String name, String tooltip, ZLStringOptionEntry option, ZLSwingDialogContent tab) {
		super(name, tooltip, option);
		myTextField = new JTextField(option.initialValue());
		if (name == null) {
			myLabel = null;
			tab.insertWidget(myTextField);
		} else {
			myLabel = new JLabel(name);
			JPanel panel = new JPanel(new BorderLayout());
			JPanel panel2 = new JPanel();
			panel2.add(myLabel);
			panel2.add(myTextField);
			panel.add(panel2, BorderLayout.LINE_END);
			tab.insertWidget(panel);
		}
	}

	protected void _onAccept() {
		((ZLStringOptionEntry) myOption).onAccept(myTextField.getText());
	}

	protected void createItem() {}

	protected void hide() {
		myTextField.setVisible(false);
		if (myLabel != null) {
			myLabel.setVisible(false);
		}
	}

	protected void show() {
		myTextField.setVisible(true);
		if (myLabel != null) {
			myLabel.setVisible(true);
		}
	}

	protected void _setActive(boolean active) {
		myTextField.setEditable(active);
	}

	public void reset() {
		myTextField.setText(((ZLStringOptionEntry) myOption).initialValue());
	}
}
