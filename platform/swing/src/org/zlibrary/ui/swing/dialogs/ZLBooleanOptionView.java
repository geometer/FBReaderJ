package org.zlibrary.ui.swing.dialogs;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;

public class ZLBooleanOptionView extends ZLSwingOptionView {
	private JCheckBox myCheckBox;	
	
	public ZLBooleanOptionView(String name, ZLBooleanOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, option, tab);
	}

	protected void _onAccept() {
		((ZLBooleanOptionEntry) myOption).onAccept(myCheckBox.isSelected());
	}

	protected void _setActive(boolean active) {
		// TODO: implement
		myCheckBox.setEnabled(active);
	}

	protected void createItem() {
		myCheckBox = new JCheckBox(myName);
		myCheckBox.setSelected(((ZLBooleanOptionEntry) myOption).initialState());
		myCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((ZLBooleanOptionEntry) myOption).onStateChanged(myCheckBox.isSelected());
			}});
		myTab.insertWidget(myCheckBox);
	}

	protected void hide() {
		myCheckBox.setVisible(false);
	}

	protected void show() {
		myCheckBox.setVisible(true);
	}
}
