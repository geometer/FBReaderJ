package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
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
		myTextField.setCaretPosition(0);
		myTextField.setMargin(new Insets(0, 5, 0, 0));
		if (name == null || "".equals(name)) {
			myLabel = null;
			tab.insertWidget(myTextField);
		} else {
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			myTextField.setMaximumSize(new Dimension(myTextField.getMaximumSize().width, myTextField.getPreferredSize().height));
			panel1.add(myTextField);
			myLabel = new JLabel(name);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add(myLabel, BorderLayout.LINE_END);
			JPanel panel = new JPanel(new GridLayout(1, 2, 5, 0));
			panel.add(panel2);
			panel.add(panel1);
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
