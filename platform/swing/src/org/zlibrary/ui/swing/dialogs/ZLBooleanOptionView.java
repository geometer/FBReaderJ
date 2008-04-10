package org.zlibrary.ui.swing.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;

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
