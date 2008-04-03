package org.zlibrary.ui.swing.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;

import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionView;

public abstract class ZLSwingOptionView extends ZLOptionView {
	protected final ZLSwingDialogContent myTab;
	protected final GridBagLayout myLayout;

	public ZLSwingOptionView(String name, ZLOptionEntry option, ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option);
		myTab = tab;
		myLayout = layout;
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
