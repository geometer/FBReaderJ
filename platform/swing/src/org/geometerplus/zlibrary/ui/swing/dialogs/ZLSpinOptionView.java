package org.geometerplus.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.geometerplus.zlibrary.core.dialogs.ZLSpinOptionEntry;

public class ZLSpinOptionView extends ZLSwingOptionView {
	private JSpinner mySpinner;
	private JLabel myLabel;
	private JPanel myPanel;

	public ZLSpinOptionView(String name, ZLSpinOptionEntry option, ZLSwingDialogContent tab,
			GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _setActive(boolean active) {
		// TODO: implement
		mySpinner.setEnabled(active);
	}

	protected void _onAccept() {
		((ZLSpinOptionEntry) myOption).onAccept((Integer) mySpinner.getValue());
	}

	protected void createItem() {
		final ZLSpinOptionEntry option = (ZLSpinOptionEntry) myOption;
		mySpinner = new JSpinner(new SpinnerNumberModel(option.initialValue(), option.minValue(),
				option.maxValue(), option.getStep()));
		if (myName == null  || "".equals(myName)) {
			myLabel = null;
			myTab.insertWidget(mySpinner);
		} else {
			JPanel panel1 = new JPanel();
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
			mySpinner.setMaximumSize(new Dimension(mySpinner.getMaximumSize().width, mySpinner.getPreferredSize().height));
			panel1.add(mySpinner);
			myLabel = new JLabel(myName);
			JPanel panel2 = new JPanel(new BorderLayout());
			panel2.add(myLabel, BorderLayout.LINE_END);
			myPanel = new JPanel(new GridLayout(1, 2, 10, 0));
			myPanel.add(panel2);
			myPanel.add(panel1);
			myTab.insertWidget(myPanel);
		}
	}

	protected void hide() {
		if (myPanel != null) {
			hide(myPanel);
		} else {
			hide(mySpinner);
		}
	}

	protected void show() {
		if (myPanel != null) {
			show(myPanel);
		} else {
			show(mySpinner);
		}
	}
}
