package org.zlibrary.ui.swing.dialogs;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;
import org.zlibrary.core.dialogs.ZLChoiceOptionEntry;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionKind;
import org.zlibrary.core.dialogs.ZLOptionView;
import org.zlibrary.core.dialogs.ZLSpinOptionEntry;
import org.zlibrary.core.dialogs.ZLStringOptionEntry;
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

	public void addOption(String name, String tooltip, ZLOptionEntry option) {
		createViewByEntry(name, tooltip, option);
	}

	public void addOptions(String name0, String tooltip0, ZLOptionEntry option0, String name1, String tooltip1, ZLOptionEntry option1) {
		myAddTwoOptions = true;
		myTwoOptionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
		myContentPanel.add(myTwoOptionsPanel);
		createViewByEntry(name0, tooltip0, option0);
		createViewByEntry(name1, tooltip1, option1);
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
	
	private void createViewByEntry(String name, String tooltip, ZLOptionEntry option) {
		if (option == null) {
			return;
		}

		ZLOptionView view = null;

		switch (option.getKind()) {
			case ZLOptionKind.BOOLEAN:
				view = new ZLBooleanOptionView(name, tooltip, (ZLBooleanOptionEntry) option, this);
				break;
			case ZLOptionKind.BOOLEAN3:
//				view = new Boolean3OptionView(name, tooltip, (ZLBoolean3OptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.STRING:
				view = new ZLStringOptionView(name, tooltip, (ZLStringOptionEntry) option, this);
				break;
			case ZLOptionKind.CHOICE:
				view = new ZLChoiceOptionView(name, tooltip, (ZLChoiceOptionEntry) option, this);
				break;
			case ZLOptionKind.SPIN:
				view = new ZLSpinOptionView(name, tooltip, (ZLSpinOptionEntry) option, this);
				break;
			case ZLOptionKind.COMBO:
//				view = new ComboOptionView(name, tooltip, (ZLComboOptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.COLOR:
//				view = new ColorOptionView(name, tooltip, (ZLColorOptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.KEY:
//				view = new KeyOptionView(name, tooltip, (ZLKeyOptionEntry*)option, *this, from, to);
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
