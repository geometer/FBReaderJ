package org.zlibrary.ui.swing.dialogs;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionView;

public class ZLBooleanOptionView extends ZLOptionView {
	private final JCheckBox myCheckBox;	
	
	public ZLBooleanOptionView(String name, ZLBooleanOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, option);
		myCheckBox = new JCheckBox(name);
		myCheckBox.setSelected(option.initialState());
		myCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((ZLBooleanOptionEntry) myOption).onStateChanged(myCheckBox.isSelected());
			}});
		tab.insertWidget(myCheckBox);
	}

	protected void _onAccept() {
		((ZLBooleanOptionEntry) myOption).onAccept(myCheckBox.isSelected());
	}

	protected void _setActive(boolean active) {
		// TODO: implement
	}

	protected void createItem() {
	}

	protected void hide() {
		myCheckBox.setVisible(false);
	}

	protected void show() {
		myCheckBox.setVisible(true);
	}
}
