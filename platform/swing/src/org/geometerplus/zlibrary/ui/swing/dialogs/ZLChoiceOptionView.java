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

import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.geometerplus.zlibrary.core.dialogs.ZLChoiceOptionEntry;

public class ZLChoiceOptionView extends ZLSwingOptionView {
	private final ButtonGroup myButtonGroup = new ButtonGroup();
	private final JPanel myButtonPanel = new JPanel(new GridLayout(0, 1, 10, 5));
	private final ArrayList<ButtonModel> myButtonModels = new ArrayList<ButtonModel>();
	
	public ZLChoiceOptionView(String name, ZLChoiceOptionEntry option,
			ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _onAccept() {
		((ZLChoiceOptionEntry) myOption).onAccept(myButtonModels.indexOf(myButtonGroup.getSelection()));
	}

	protected void _setActive(boolean active) {
		for (Enumeration buttons = myButtonGroup.getElements(); buttons.hasMoreElements(); ) {
			((JRadioButton)buttons.nextElement()).setEnabled(active);
		}
		// TODO: make border grayed
		myButtonPanel.setEnabled(active);
	}

	protected void createItem() {
		myButtonPanel.setBorder(new TitledBorder(myName));
		final int choiceNumber = ((ZLChoiceOptionEntry) myOption).choiceNumber();
		for (int i = 0; i < choiceNumber; i++) {
			JRadioButton button = new JRadioButton(((ZLChoiceOptionEntry) myOption).getText(i));
			myButtonGroup.add(button);
			myButtonPanel.add(button);
			myButtonModels.add(button.getModel());
		}
		myButtonGroup.setSelected(myButtonModels.get(((ZLChoiceOptionEntry) myOption).initialCheckedIndex()), true);
		myTab.insertWidget(myButtonPanel);
	}

	protected void hide() {
		hide(myButtonPanel);
	}

	protected void show() {
		show(myButtonPanel);
	}
}
