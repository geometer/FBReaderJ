package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.zlibrary.core.dialogs.ZLColorOptionEntry;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.util.ZLColor;
import org.zlibrary.text.view.style.ZLTextStyleCollection;

public class ZLColorOptionView extends ZLSwingOptionView {
	private JComboBox myStandardColorComboBox;
	private JButton myCustomColorButton;
	private JPanel myPanel; 
	private boolean myUseCustomColor = false;
	private static final LinkedHashMap/*<ZLColor,String>*/ ourStandardColors = new LinkedHashMap();
	
	public ZLColorOptionView(String name, ZLColorOptionEntry option,
			ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	public void reset() {
		ZLColorOptionEntry colorEntry = (ZLColorOptionEntry) myOption;
		colorEntry.onReset((ZLColor) myStandardColorComboBox.getSelectedItem());
		setSelectedColor(colorEntry.getColor());
//		myStandardColorComboBox.setSelectedItem(colorEntry.getColor());
	}
	
	protected void _onAccept() {
		((ZLColorOptionEntry) myOption).onAccept((ZLColor) (myStandardColorComboBox.getSelectedItem()));
	}

	protected void _setActive(boolean active) {}

	protected void createItem() {
		initStandardColors();
		myStandardColorComboBox = new JComboBox(ourStandardColors.keySet().toArray());
		setSelectedColor(((ZLColorOptionEntry) myOption).getColor());
		myStandardColorComboBox.setRenderer(new ComboBoxRenderer());
		
		myStandardColorComboBox.setMinimumSize(new Dimension(0, myStandardColorComboBox.getPreferredSize().height));
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
		myStandardColorComboBox.setMaximumSize(new Dimension(myStandardColorComboBox.getMaximumSize().width, myStandardColorComboBox.getPreferredSize().height));
		panel1.add(myStandardColorComboBox);
		myCustomColorButton = new JButton(new CustomColorAction(ZLResource.resource("color").getResource("custom...").getValue()));
		myCustomColorButton.setMinimumSize(new Dimension(0, myCustomColorButton.getPreferredSize().height));
		myCustomColorButton.setMaximumSize(new Dimension(panel1.getMaximumSize().width, myCustomColorButton.getPreferredSize().height));
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
		panel2.add(myCustomColorButton);
		myPanel = new JPanel(new GridLayout(1, 2, 10, 0));
		myPanel.add(panel1);
		myPanel.add(panel2);
		
		myTab.insertWidget(myPanel);
	}

	protected void hide() {
		hide(myPanel);
	}

	protected void show() {
		show(myPanel);
	}

	private static void initStandardColors() {
		if (ourStandardColors.size() == 0) {
			final ZLResource resource = ZLResource.resource(ZLDialogManager.COLOR_KEY);
			ourStandardColors.put(new ZLColor(0, 0, 0), resource.getResource("black").getValue());
			ourStandardColors.put(new ZLColor(255, 255, 255), resource.getResource("white").getValue());
			ourStandardColors.put(new ZLColor(128, 0, 0), resource.getResource("maroon").getValue());
			ourStandardColors.put(new ZLColor(0, 128, 0), resource.getResource("green").getValue());
			ourStandardColors.put(new ZLColor(128, 128, 0), resource.getResource("olive").getValue());
			ourStandardColors.put(new ZLColor(0, 0, 128), resource.getResource("navy").getValue());
			ourStandardColors.put(new ZLColor(128, 0, 128), resource.getResource("purple").getValue());
			ourStandardColors.put(new ZLColor(0, 128, 128), resource.getResource("teal").getValue());
			ourStandardColors.put(new ZLColor(192, 192, 192), resource.getResource("silver").getValue());
			ourStandardColors.put(new ZLColor(128, 128, 128), resource.getResource("gray").getValue());
			ourStandardColors.put(new ZLColor(255, 0, 0), resource.getResource("red").getValue());
			ourStandardColors.put(new ZLColor(0, 255, 0), resource.getResource("lime").getValue());
			ourStandardColors.put(new ZLColor(255, 255, 0), resource.getResource("yellow").getValue());
			ourStandardColors.put(new ZLColor(0, 0, 255), resource.getResource("blue").getValue());
			ourStandardColors.put(new ZLColor(255, 0, 255), resource.getResource("magenta").getValue());
			ourStandardColors.put(new ZLColor(0, 255, 255), resource.getResource("cyan").getValue());
		}
	}
	
	private void setSelectedColor(ZLColor color) {
		initStandardColors();
		if(!ourStandardColors.containsKey(color)) {
			if (! myUseCustomColor) {
				myStandardColorComboBox.insertItemAt(color, 0);
				myUseCustomColor = true;
			} else {
				myStandardColorComboBox.removeItem(myStandardColorComboBox.getItemAt(0));
				myStandardColorComboBox.insertItemAt(color, 0);
			}
		}
		myStandardColorComboBox.setSelectedItem(color);
	}
	
	
	private class ComboBoxRenderer extends JLabel implements ListCellRenderer {

		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			ZLColor color = (ZLColor) value;
			if (ourStandardColors.containsKey(color)) {
				setText((String) ourStandardColors.get(color));
			} else {
				setText("(" + color.Red + "," + color.Green + "," + color.Blue + ")");
			}
			
			setIcon(new ColorIcon(color, this.getPreferredSize().height));
			setHorizontalTextPosition(SwingConstants.RIGHT);
			setIconTextGap(5);
			setFont(list.getFont());
			return this;
		}
		
		private class ColorIcon implements Icon{
		    
		    private int myWidth = 0;
		    private int myHeight = 0;
			private ZLColor myColor;
		    
		    public ColorIcon(ZLColor color, int height) {
		    	myColor = color;
		    	myHeight = height;
		    	myWidth = myHeight*2;
		    }
		    
		    public void paintIcon(Component c, Graphics g, int x, int y) {
		        Graphics2D g2d = (Graphics2D) g.create();
		        
		        g2d.setColor(new Color(myColor.getIntValue()));
		        g2d.fillRect(x + 1, y + 1, myWidth, myHeight -2);
		        
		        g2d.setColor(Color.BLACK);
		        g2d.drawRect(x + 1, y + 1, myWidth, myHeight -2);
		        
		        g2d.dispose();
		    }
		    
		    public int getIconWidth() {
		        return myWidth;
		    }
		    
		    public int getIconHeight() {
		        return myHeight;
		    }
		}
	}
	
	
	private class CustomColorAction extends AbstractAction {
		
		public CustomColorAction(String name) {
			putValue(NAME, name);
		}
		
		public void actionPerformed(ActionEvent e) {
			JColorChooser chooser = new JColorChooser();
			chooser.removeChooserPanel(chooser.getChooserPanels()[0]);
			chooser.removeChooserPanel(chooser.getChooserPanels()[1]);
			Color color = chooser.showDialog(null, ZLResource.resource("dialog").getResource("OptionsDialog").getResource("tab").getResource("Colors").getResource("colorFor").getValue(),
					new Color(((ZLColor) myStandardColorComboBox.getSelectedItem()).getIntValue()));
			if (color != null) {
				setSelectedColor(new ZLColor(color.getRGB()));
			}
		}
		
	}
}
