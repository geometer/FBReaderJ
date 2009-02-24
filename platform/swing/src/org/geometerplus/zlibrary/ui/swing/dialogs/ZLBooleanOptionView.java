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

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geometerplus.zlibrary.core.dialogs.ZLBooleanOptionEntry;

public class ZLBooleanOptionView extends ZLSwingOptionView {
	private JCheckBox myCheckBox;	
	
	public ZLBooleanOptionView(String name, ZLBooleanOptionEntry option,
			ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _onAccept() {
		((ZLBooleanOptionEntry)myOption).onAccept(myCheckBox.isSelected());
	}

	protected void _setActive(boolean active) {
		// TODO: implement
		myCheckBox.setEnabled(active);
	}

	protected void createItem() {
		final ZLBooleanOptionEntry booleanEntry = (ZLBooleanOptionEntry)myOption;
		myCheckBox = new JCheckBox(myName);
		myCheckBox.setSelected(booleanEntry.initialState());
		myCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				booleanEntry.onStateChanged(myCheckBox.isSelected());
			}});
		myTab.insertWidget(myCheckBox);
	}

	protected void hide() {
		hide(myCheckBox);
	}

	protected void show() {
		show(myCheckBox);
	}
}
