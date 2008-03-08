package org.zlibrary.ui.swing.dialogs;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.zlibrary.core.dialogs.ZLChoiceOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionView;

public class ZLChoiceOptionView extends ZLOptionView {
	private final ButtonGroup myButtonGroup = new ButtonGroup();
	private final JPanel myButtonPanel = new JPanel(new GridLayout(0, 1, 10, 5));
	private final ArrayList<ButtonModel> myButtonModels = new ArrayList<ButtonModel>();
	
	public ZLChoiceOptionView(String name, String tooltip, ZLChoiceOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, tooltip, option);
		myButtonPanel.setBorder(new TitledBorder(name));
		final int choiceNumber = option.choiceNumber();
		for (int i = 0; i < choiceNumber; i++) {
			JRadioButton button = new JRadioButton(option.getText(i));
			myButtonGroup.add(button);
			myButtonPanel.add(button);
			myButtonModels.add(button.getModel());
		}
		myButtonGroup.setSelected(myButtonModels.get(((ZLChoiceOptionEntry) myOption).initialCheckedIndex()), true);
		tab.insertWidget(myButtonPanel);
	}

	protected void _onAccept() {
		((ZLChoiceOptionEntry) myOption).onAccept(myButtonModels.indexOf(myButtonGroup.getSelection()));
	}

	protected void createItem() {}

	protected void hide() {
		myButtonPanel.setVisible(false);
	}

	protected void show() {
		myButtonPanel.setVisible(true);
	}
}
