package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionView;

public class ZLComboOptionView extends ZLOptionView {
	private final JComboBox myComboBox;
	private final JLabel myLabel;
	
	public ZLComboOptionView(String name, String tooltip, ZLComboOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, tooltip, option);
		final ArrayList values = option.getValues();
		final String initialValue = option.initialValue();
		int index = 0;
		for (int i = 0; i < values.size(); ++i) {
			if (values.get(i).equals(initialValue)) {
				index = i;
				break;
			}
		}
		myComboBox = new JComboBox(values.toArray());
		myComboBox.setSelectedIndex(index);
		myComboBox.setEditable(option.isEditable());
		if (name == null) {
			myLabel = null;
			tab.insertWidget(myComboBox);
		} else {
			myLabel = new JLabel(name);
			JPanel panel = new JPanel(new BorderLayout());
			JPanel panel2 = new JPanel();
			panel2.add(myLabel);
			panel2.add(myComboBox);
			panel.add(panel2, BorderLayout.LINE_END);
			tab.insertWidget(panel);
		}
	}

	protected void _onAccept() {
		((ZLComboOptionEntry) myOption).onAccept((String) myComboBox.getSelectedItem());
	}

	protected void createItem() {}

	protected void hide() {
		myComboBox.setVisible(false);
		if (myLabel != null) {
			myLabel.setVisible(false);
		}
	}

	protected void show() {
		myComboBox.setVisible(true);
		if (myLabel != null) {
			myLabel.setVisible(true);
		}
	}

	protected void _setActive(boolean active) {
		myComboBox.setEnabled(active);
	}

	public void reset() {
		ZLComboOptionEntry o = (ZLComboOptionEntry) myOption;
		final ArrayList values = o.getValues();
		final String initialValue = o.initialValue();
		int index = 0;
		for (int i = 0; i < values.size(); ++i) {
			if (values.get(i).equals(initialValue)) {
				index = i;
				break;
			}
		}
		myComboBox.setModel(new DefaultComboBoxModel(values.toArray()));
		myComboBox.setSelectedIndex(index);
	}

}
