package org.zlibrary.ui.swing.dialogs;

import javax.swing.*;
import java.awt.*;

import org.zlibrary.ui.swing.util.ZLSwingIconUtil;

class ZLSwingSelectionDialog {
	private JDialog myJDialog;

	ZLSwingSelectionDialog(JFrame frame) {
		myJDialog = new JDialog(frame, true);
	}

	private static class CellRenderer extends JLabel implements ListCellRenderer {
		final static ImageIcon icon = ZLSwingIconUtil.getIcon("icons/filetree/unknown.png");
		final static ImageIcon upicon = ZLSwingIconUtil.getIcon("icons/filetree/upfolder.png");

		public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
		{
			String s = value.toString();
			setText(s);
			setIcon(s.equals("..") ? upicon : icon);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	void run() {
		myJDialog.setLayout(new BorderLayout());

		JTextField textLine = new JTextField();
		textLine.setText("directory name");
		textLine.setEditable(false);
		myJDialog.add(textLine, BorderLayout.NORTH);

		String[] books = new String[] { "..", "Book 0", "Book 1", "Book 2", "Book 3", "Book 4", "Book 5" };
		JList list = new JList(books);
		list.setCellRenderer(new CellRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		myJDialog.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button1 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.OK_BUTTON);
		buttonPanel.add(button1);
		JButton button2 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.CANCEL_BUTTON);
		buttonPanel.add(button2);
		myJDialog.add(buttonPanel, BorderLayout.SOUTH);
		myJDialog.pack();
		myJDialog.setSize(600, 400);
		myJDialog.setLocationRelativeTo(myJDialog.getParent());
		myJDialog.show();
	}
}
