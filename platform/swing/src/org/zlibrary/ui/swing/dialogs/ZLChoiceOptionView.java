package org.zlibrary.ui.swing.dialogs;

import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.zlibrary.core.dialogs.ZLChoiceOptionEntry;

public class ZLChoiceOptionView extends ZLSwingOptionView {
	private final ButtonGroup myButtonGroup = new ButtonGroup();
	private final JPanel myButtonPanel = new JPanel(new GridLayout(0, 1, 10, 5));
	private final ArrayList<ButtonModel> myButtonModels = new ArrayList<ButtonModel>();
	
	public ZLChoiceOptionView(String name, ZLChoiceOptionEntry option,
			ZLSwingDialogContent tab) {
		super(name, option, tab);
	}

	protected void _onAccept() {
		((ZLChoiceOptionEntry) myOption).onAccept(myButtonModels.indexOf(myButtonGroup.getSelection()));
	}

	protected void _setActive(boolean active) {
		// TODO: implement
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
		myButtonPanel.setVisible(false);
	}

	protected void show() {
		myButtonPanel.setVisible(true);
	}
}
