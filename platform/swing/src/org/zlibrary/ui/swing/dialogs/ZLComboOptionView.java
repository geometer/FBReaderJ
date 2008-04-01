package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;

public class ZLComboOptionView extends ZLSwingOptionView {
	private JComboBox myComboBox;
	private JLabel myLabel;
	
	public ZLComboOptionView(String name, ZLComboOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, option, tab);
	}

	protected void _onAccept() {
		((ZLComboOptionEntry) myOption).onAccept((String) myComboBox.getSelectedItem());
	}

	protected void createItem() {
		final ZLComboOptionEntry option = (ZLComboOptionEntry) myOption;
		final ArrayList values = (option).getValues();
		final String initialValue = (option).initialValue();
		int index = 0;
		for (int i = 0; i < values.size(); ++i) {
			if (values.get(i).equals(initialValue)) {
				index = i;
				break;
			}
		}
		myComboBox = new JComboBox(values.toArray());
		myComboBox.setSelectedIndex(index);
		myComboBox.setEditable((option).isEditable());
		myComboBox.addItemListener(new MyItemListener());
		if (option.useOnValueEdited()) {
			myComboBox.getEditor().getEditorComponent().addKeyListener(new MyKeyListener());
		}	
		if (myName == null  || "".equals(myName)) {
			myLabel = null;
			myTab.insertWidget(myComboBox);
		} else {
			myComboBox.setMinimumSize(new Dimension(0, myComboBox.getPreferredSize().height));
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			myComboBox.setMaximumSize(new Dimension(myComboBox.getMaximumSize().width, myComboBox.getPreferredSize().height));
			panel1.add(myComboBox);
			myLabel = new JLabel(myName);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add(myLabel, BorderLayout.LINE_END);
			JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
			panel.add(panel2);
			panel.add(panel1);
			myTab.insertWidget(panel);
		}
	}

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
	
	private class MyItemListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			((ZLComboOptionEntry) myOption).onValueSelected(myComboBox.getSelectedIndex());
		}
		
	}
	
	private class MyKeyListener extends KeyAdapter {
		public void keyTyped(KeyEvent e) {
			((ZLComboOptionEntry) myOption).onValueEdited((String) myComboBox.getSelectedItem());
		}
	}
	
}
