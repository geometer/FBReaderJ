package org.zlibrary.ui.swing.dialogs;

import javax.swing.JCheckBox;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionView;

public class ZLBooleanOptionView extends ZLOptionView {
	private final JCheckBox myCheckBox;	
	
	public ZLBooleanOptionView(String name, String tooltip, ZLBooleanOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, tooltip, option);
		myCheckBox = new JCheckBox(name);
		myCheckBox.setSelected(option.initialState());
		tab.insertWidget(myCheckBox);
	}

	protected void _onAccept() {
		((ZLBooleanOptionEntry) myOption).onAccept(myCheckBox.isSelected());
	}

	protected void createItem() {}

	protected void hide() {
		myCheckBox.setVisible(false);
	}

	protected void show() {
		myCheckBox.setVisible(true);
	}
}
