package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.zlibrary.core.dialogs.ZLKeyOptionEntry;
import org.zlibrary.core.resources.ZLResource;

public class ZLKeyOptionView extends ZLSwingOptionView {
	private JComboBox myComboBox;
	private	KeyEditor myKeyEditor;
	private JLabel myLabel;
	private	String myCurrentKey = "";
		
	public ZLKeyOptionView(String name, ZLKeyOptionEntry option, ZLSwingDialogContent tab) {
		super(name, option, tab);
	}
	
	protected void _onAccept() {
		((ZLKeyOptionEntry) myOption).onAccept();
	}

	protected void _setActive(boolean active) {
		// TODO Auto-generated method stub

	}

	protected void createItem() {
		myKeyEditor = new KeyEditor("");
		myKeyEditor.setInputMap(JComponent.WHEN_FOCUSED, null);
		myKeyEditor.setCaretPosition(0);
		myKeyEditor.setMargin(new Insets(0, 5, 0, 0));
		myKeyEditor.addKeyListener(new MyKeyListener());
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
		myKeyEditor.setMaximumSize(new Dimension(myKeyEditor.getMaximumSize().width, myKeyEditor.getPreferredSize().height));
		panel1.add(myKeyEditor);
		myLabel = new JLabel(ZLResource.resource("keyOptionView").getResource("actionFor").getValue());
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(myLabel, BorderLayout.LINE_END);
		JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
		panel.add(panel2);
		panel.add(panel1);
		myTab.insertWidget(panel);
		
		myComboBox = new JComboBox(((ZLKeyOptionEntry) myOption).getActionNames().toArray());
		myComboBox.addItemListener(new MyItemListener());
		myTab.insertWidget(myComboBox);
	}

	protected void hide() {
		myKeyEditor.setVisible(false);
		myLabel.setVisible(false);
		myComboBox.setVisible(false);
	}

	protected void show() {
		myKeyEditor.setVisible(true);
		myLabel.setVisible(true);
		myComboBox.setVisible(! "".equals(myCurrentKey));
	}

	public void reset() {
		myCurrentKey = null;
		myKeyEditor.setText("");
		myComboBox.setVisible(false);
	}
	
	private class MyItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			((ZLKeyOptionEntry) myOption).onValueChanged(myCurrentKey, myComboBox.getSelectedIndex());
		}		
	}

	private class MyKeyListener extends KeyAdapter {

		public void keyPressed(KeyEvent e) {			
			final String modifiers = e.getModifiersExText(e.getModifiersEx());
			final int code = e.getKeyCode();
			String main;
			switch (code) {
				case 27:
					main = "Esc";
					break;
				case 33:
					main = "Page Up";
					break;
				case 34:
					main = "Page Down";
					break;
				case 35:
					main = "End";
					break;
				case 36:
					main = "Home";
					break;
				case 37:
					main = "LeftArrow";
					break;
				case 38:
					main = "UpArrow";
					break;
				case 39:
					main = "RightArrow";
					break;
				case 40:
					main = "DownArrow";
					break;
				default:
					main = e.getKeyText(code);
					break;
			}
			final String keyCode = keyTextModifiersParse(modifiers) + keyTextParse(main);
			if (keyCode.equals("<Ctrl>+<Ctrl>") || keyCode.equals("<Shift>+<Shift>")) {
				myCurrentKey = "";
			} else {
				myCurrentKey = keyCode;
			}
			myKeyEditor.setText(myCurrentKey);
			myComboBox.setVisible(!"".equals(myCurrentKey));
			myComboBox.setSelectedIndex(((ZLKeyOptionEntry) myOption).actionIndex(myCurrentKey));
			((ZLKeyOptionEntry) myOption).onKeySelected(myCurrentKey);
		}
		
		private String keyTextParse(String str) {
			if (str.equals("Left") || str.equals("Down") || 
					str.equals("Right") || str.equals("Up")) {
				str = str + "Arrow";
			} else if (str.equals("Escape")) {
				str = "Esc";
			} else if (str.equals("Equals")) {
				str = "=";
			} else if (str.equals("Minus")) {
				str = "-";
			} else if (str.startsWith("Page")) {
				str = "Page" + str.substring("Page".length() + 1, str.length());
			} else if (str.equals("Enter")) {
				str = "Return";
			}
			
			return "<" + str + ">";
		}
		
		private String keyTextModifiersParse(String str) {
			if (str.equals("")) {
				return "";
			}
			return "<" + str + ">+";
		}

	}
	
	
	
	private static class KeyEditor extends JTextField {
		 
	     public KeyEditor(String string) {
	         super(string);
	     }
	 
	     protected Document createDefaultModel() {
	         return new KeyEditorDocument();
	     }
	 
	     private static class KeyEditorDocument extends PlainDocument {
	 
	         public void insertString(int offs, String str, AttributeSet a) 
	         	throws BadLocationException {
	 
	            if (str == null) {
	                 return;
	            }
	            if (str.startsWith("<") || str.startsWith("+")) {
	            	super.insertString(offs, str, a);
	            } else {
	            	super.insertString(offs, "", a);
	            }
	         }

	     }
	 }
}
