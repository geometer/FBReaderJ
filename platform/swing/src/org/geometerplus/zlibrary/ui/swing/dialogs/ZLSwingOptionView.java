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

import javax.swing.JComponent;

import org.geometerplus.zlibrary.core.dialogs.ZLOptionEntry;
import org.geometerplus.zlibrary.core.dialogs.ZLOptionView;

public abstract class ZLSwingOptionView extends ZLOptionView {
	protected final ZLSwingDialogContent myTab;
	protected final GridBagLayout myLayout;

	public ZLSwingOptionView(String name, ZLOptionEntry option, ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option);
		myTab = tab;
		myLayout = layout;
	}
	
	// TODO: remove
	protected void reset() {
	}

	protected void hide(JComponent component) {
		component.setVisible(false);
		final GridBagConstraints constraints = myLayout.getConstraints(component);
		constraints.insets.top = 0;
		constraints.insets.bottom = 0;
		myLayout.setConstraints(component, constraints);
	}
	
	protected void show(JComponent component) {
		final GridBagConstraints constraints = myLayout.getConstraints(component);
		constraints.insets.top = 5;
		constraints.insets.bottom = 5;
		constraints.gridheight = 1;
		myLayout.setConstraints(component, constraints);
		component.setVisible(true);
	}
}
