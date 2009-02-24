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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.geometerplus.zlibrary.core.dialogs.*;

public class ZLStringOptionView extends ZLSwingOptionView {
	private JLabel myLabel;
	private JTextField myTextField;
	private JPanel myPanel;
	
	public ZLStringOptionView(String name, ZLStringOptionEntry option, ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _onAccept() {
		((ZLStringOptionEntry)myOption).onAccept(myTextField.getText());
	}

	protected void createItem() {
		myTextField = new JTextField(((ZLStringOptionEntry)myOption).initialValue());
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
		myTextField.setText(((ZLStringOptionEntry)myOption).initialValue());
	}
	
	private class MyKeyListener extends KeyAdapter {
		public void keyTyped(KeyEvent e) {
			ZLStringOptionEntry o = (ZLStringOptionEntry)myOption;
			if (o.useOnValueEdited()) {
				o.onValueEdited(myTextField.getText());
			}
		}
	}
}
