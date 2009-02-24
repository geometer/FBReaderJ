/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.swing.dialogs;

import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

public class ZLComboOptionView extends ZLSwingOptionView {
	private JComboBox myComboBox;
	private JLabel myLabel;
	private JPanel myPanel;
	
	public ZLComboOptionView(String name, ZLComboOptionEntry option,
			ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _onAccept() {
		((ZLComboOptionEntry) myOption).onAccept((String) myComboBox.getSelectedItem());
	}

	protected void createItem() {
		final ZLComboOptionEntry option = (ZLComboOptionEntry)myOption;
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
			hide(myComboBox);
		}
	}

	protected void show() {		
		if (myPanel != null) {
			show(myPanel);
		} else {
			show(myComboBox);
		}
	}

	protected void _setActive(boolean active) {
		myComboBox.setEnabled(active);
	}

	public void reset() {
		ZLComboOptionEntry o = (ZLComboOptionEntry) myOption;
		o.onReset();
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
