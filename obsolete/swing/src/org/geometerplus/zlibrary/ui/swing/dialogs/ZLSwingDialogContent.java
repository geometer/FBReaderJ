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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class ZLSwingDialogContent extends ZLDialogContent {
	private final JPanel myContentPanel = new JPanel();
	private boolean myAddTwoOptions = false;
	private final GridBagLayout myLayout = new GridBagLayout();
	private final GridBagConstraints myConstraints = new GridBagConstraints();
	
	protected ZLSwingDialogContent(ZLResource resource) {
		super(resource);
		myContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		myContentPanel.setLayout(myLayout);
		myConstraints.fill = GridBagConstraints.HORIZONTAL;
		myConstraints.anchor = GridBagConstraints.WEST;
		myConstraints.gridx = 0;
		myConstraints.gridy = 0;
		myConstraints.insets = new Insets(5, 5, 0, 5);
		myConstraints.weightx = 1;
		myConstraints.ipady = 0;
	}

	public void addOptionByName(String name, ZLOptionEntry option) {
		createViewByEntry(name, option);
	}

	public void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1) {
		myAddTwoOptions = true;
		createViewByEntry(name0, option0);
		createViewByEntry(name1, option1);
		myAddTwoOptions = false;
		myConstraints.gridx = 0;
		myConstraints.gridy++;
	}
	
	public JPanel getContentPanel() {
		return myContentPanel;
	}
	
	public void insertWidget(JComponent comp) {
		myContentPanel.add(comp, myConstraints);	
		if (myAddTwoOptions) {
			myConstraints.gridx = 1;
		} else {
			myConstraints.gridy++;
		}
	}
	
	private void createViewByEntry(String name, ZLOptionEntry option) {
		if (option == null) {
			return;
		}

		ZLOptionView view = null;

		switch (option.getKind()) {
			case ZLOptionKind.BOOLEAN:
				view = new ZLBooleanOptionView(name, (ZLBooleanOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.BOOLEAN3:
				view = new ZLBoolean3OptionView(name, (ZLBoolean3OptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.STRING:
				view = new ZLStringOptionView(name, (ZLStringOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.CHOICE:
				view = new ZLChoiceOptionView(name, (ZLChoiceOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.SPIN:
				view = new ZLSpinOptionView(name, (ZLSpinOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.COMBO:
				view = new ZLComboOptionView(name, (ZLComboOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.COLOR:
				view = new ZLColorOptionView(name, (ZLColorOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.KEY:
				view = new ZLKeyOptionView(name, (ZLKeyOptionEntry) option, this, myLayout);
				break;
			case ZLOptionKind.ORDER:
				// TODO: implement
				break;
			case ZLOptionKind.MULTILINE:
				// TODO: implement
				break;
		}

		if (view != null) {
			view.setVisible(option.isVisible());
			addView(view);
		}
	}
}
