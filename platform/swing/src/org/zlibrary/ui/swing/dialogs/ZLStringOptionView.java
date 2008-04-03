package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.zlibrary.core.dialogs.ZLStringOptionEntry;
import org.zlibrary.core.dialogs.ZLTextOptionEntry;

public class ZLStringOptionView extends ZLSwingOptionView {
	private JLabel myLabel;
	private JTextField myTextField;
	private JPanel myPanel;
	
	public ZLStringOptionView(String name, ZLStringOptionEntry option,
			ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _onAccept() {
		((ZLStringOptionEntry) myOption).onAccept(myTextField.getText());
	}

	protected void createItem() {
		myTextField = new JTextField(((ZLTextOptionEntry) myOption).initialValue());
		myTextField.setCaretPosition(0);
		myTextField.setMargin(new Insets(0, 5, 0, 0));
		myTextField.addKeyListener(new MyKeyListener());
		if (myName == null || "".equals(myName)) {
			myLabel = null;
			myTab.insertWidget(myTextField);
		} else {
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			myTextField.setMaximumSize(new Dimension(myTextField.getMaximumSize().width, myTextField.getPreferredSize().height));
			panel1.add(myTextField);
			myLabel = new JLabel(myName);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add(myLabel, BorderLayout.LINE_END);
			myPanel = new JPanel(new GridLayout(1, 2, 10, 0));
			myPanel.add(panel2);
			myPanel.add(panel1);
			myTab.insertWidget(myPanel);
		}
	}

	protected void hide() {	
		if (myPanel != null) {
			hide(myPanel);
		} else {
			hide(myTextField);
		}
	}

	protected void show() {	
		if (myPanel != null) {
			show(myPanel);
		} else {
			show(myTextField);
		}
	}

	protected void _setActive(boolean active) {
		myTextField.setEditable(active);
	}

	public void reset() {
		myTextField.setText(((ZLStringOptionEntry) myOption).initialValue());
	}
	
	private class MyKeyListener extends KeyAdapter {
		public void keyTyped(KeyEvent e) {
			ZLStringOptionEntry o = (ZLStringOptionEntry) myOption;
			if (o.useOnValueEdited()) {
				o.onValueEdited(myTextField.getText());
			}
		}
	}

}
