package org.zlibrary.ui.swing.dialogs;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.resources.ZLResource;

public class ZLSwingDialogContent extends ZLDialogContent {
	private final JPanel myContentPanel = new JPanel();
	private JPanel myTwoOptionsPanel;
	private boolean myAddTwoOptions = false;
	
	protected ZLSwingDialogContent(ZLResource resource) {
		super(resource);
		myContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		myContentPanel.setLayout(new GridLayout(0, 1, 10, 5));
	}

	public void addOptionByName(String name, ZLOptionEntry option) {
		createViewByEntry(name, option);
	}

	public void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1) {
		myAddTwoOptions = true;
		myTwoOptionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
		myContentPanel.add(myTwoOptionsPanel);
		createViewByEntry(name0, option0);
		createViewByEntry(name1, option1);
		myAddTwoOptions = false;
	}
	
	public JPanel getContentPanel() {
		return myContentPanel;
	}
	
	public void insertWidget(JComponent comp) {
		if (myAddTwoOptions) {
			myTwoOptionsPanel.add(comp);
		} else {
			myContentPanel.add(comp);
		}	
	}
	
	private void createViewByEntry(String name, ZLOptionEntry option) {
		if (option == null) {
			return;
		}

		ZLOptionView view = null;

		switch (option.getKind()) {
			case ZLOptionKind.BOOLEAN:
				view = new ZLBooleanOptionView(name, (ZLBooleanOptionEntry) option, this);
				break;
			case ZLOptionKind.BOOLEAN3:
//				view = new Boolean3OptionView(name, (ZLBoolean3OptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.STRING:
				view = new ZLStringOptionView(name, (ZLStringOptionEntry) option, this);
				break;
			case ZLOptionKind.CHOICE:
				view = new ZLChoiceOptionView(name, (ZLChoiceOptionEntry) option, this);
				break;
			case ZLOptionKind.SPIN:
				view = new ZLSpinOptionView(name, (ZLSpinOptionEntry) option, this);
				break;
			case ZLOptionKind.COMBO:
				view = new ZLComboOptionView(name, (ZLComboOptionEntry) option, this);
				break;
			case ZLOptionKind.COLOR:
//				view = new ColorOptionView(name, (ZLColorOptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.KEY:
				view = new ZLKeyOptionView(name, (ZLKeyOptionEntry) option, this);
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
