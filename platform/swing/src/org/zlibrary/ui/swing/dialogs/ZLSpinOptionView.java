package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.zlibrary.core.dialogs.ZLOptionView;
import org.zlibrary.core.dialogs.ZLSpinOptionEntry;

public class ZLSpinOptionView extends ZLOptionView {
	private final JSpinner mySpinner;
	private final JLabel myLabel;

	public ZLSpinOptionView(String name, String tooltip, ZLSpinOptionEntry option, ZLSwingDialogContent tab) {
		super(name, tooltip, option);
		mySpinner = new JSpinner(new SpinnerNumberModel(option.initialValue(), option.minValue(),
				option.maxValue(), option.getStep()));
		if (name == null  || "".equals(name)) {
			myLabel = null;
			tab.insertWidget(mySpinner);
		} else {
/*			myLabel = new JLabel(name);
			JPanel panel = new JPanel(new BorderLayout());
			JPanel panel2 = new JPanel();
			panel2.add(myLabel);
			panel2.add(mySpinner);
			panel.add(panel2, BorderLayout.LINE_END);
			tab.insertWidget(panel);
*/			
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			mySpinner.setMaximumSize(new Dimension(mySpinner.getMaximumSize().width, mySpinner.getPreferredSize().height));
			panel1.add(mySpinner);
			myLabel = new JLabel(name);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add(myLabel, BorderLayout.LINE_END);
			JPanel panel = new JPanel(new GridLayout(1, 2, 5, 0));
			panel.add(panel2);
			panel.add(panel1);
			tab.insertWidget(panel);
		}
	}

	protected void _onAccept() {
		((ZLSpinOptionEntry) myOption).onAccept((Integer) mySpinner.getValue());
	}

	protected void createItem() {}

	protected void hide() {
		mySpinner.setVisible(false);
		if (myLabel != null) {
			myLabel.setVisible(false);
		}
	}

	protected void show() {
		mySpinner.setVisible(true);
		if (myLabel != null) {
			myLabel.setVisible(true);
		}
	}

}
