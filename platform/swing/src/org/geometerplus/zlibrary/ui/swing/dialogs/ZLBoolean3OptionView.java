/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.swing.dialogs;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.geometerplus.zlibrary.core.dialogs.ZLBoolean3OptionEntry;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

public class ZLBoolean3OptionView extends ZLSwingOptionView {
	private TristateCheckBox myTristateCheckBox;

	public ZLBoolean3OptionView(String name, ZLBoolean3OptionEntry option, ZLSwingDialogContent tab, GridBagLayout layout) {
		super(name, option, tab, layout);
	}

	protected void _onAccept() {
		((ZLBoolean3OptionEntry) myOption).onAccept(stateToInt(myTristateCheckBox.getState()));
	}

	protected void _setActive(boolean active) {
		// TODO Auto-generated method stub
		myTristateCheckBox.setEnabled(active);
	}

	protected void createItem() {
		myTristateCheckBox = new TristateCheckBox(myName, intToState(((ZLBoolean3OptionEntry) myOption).initialState()));
		myTab.insertWidget(myTristateCheckBox);
		myTristateCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((ZLBoolean3OptionEntry) myOption).onStateChanged(stateToInt(myTristateCheckBox.getState()));
		}});
	}

	protected void hide() {
		hide(myTristateCheckBox);
	}

	protected void show() {
		show(myTristateCheckBox);
	}

	private static int stateToInt(TristateCheckBox.State state) {
		if (state == TristateCheckBox.NOT_SELECTED) {
			return ZLBoolean3.B3_FALSE;
		}
		if (state == TristateCheckBox.SELECTED) {
			return ZLBoolean3.B3_TRUE;
		}
		return ZLBoolean3.B3_UNDEFINED;
	}
	
	private static TristateCheckBox.State intToState(int state) {
		switch (state) {
		case ZLBoolean3.B3_TRUE:
			return TristateCheckBox.SELECTED;
		case ZLBoolean3.B3_FALSE:
			return TristateCheckBox.NOT_SELECTED;
		default:
			return TristateCheckBox.DONT_CARE;
		}
	}
	
	private static class TristateCheckBox extends JCheckBox {
		public static class State {
			private String desc = "";
			
			private State() {
			}
			
			private State(String desc) {
				this.desc = desc;
			}
			
			public String toString() {
				return desc;
			}
		}
	 
		public static final State NOT_SELECTED = new State("NOT_SELECTED");
		public static final State SELECTED = new State("SELECTED");
		public static final State DONT_CARE = new State("DONT_CARE");
	 
		private final TristateModel model;
	 
		public TristateCheckBox(String text, State initial) {
			super(text);
			
			Icon icon = new TristateCheckBoxIcon();
			super.setIcon(icon);
			
			// Add a listener for when the mouse is pressed and released
			super.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					TristateCheckBox.this.mousePressed();
				}
				
				public void mouseReleased(MouseEvent e) {
					TristateCheckBox.this.mouseReleased();				
				}
			});
			// Reset the keyboard action map
			ActionMap map = new ActionMapUIResource();
			map.put("pressed", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					TristateCheckBox.this.mousePressed();
				}
			});
			map.put("released", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					TristateCheckBox.this.mouseReleased();	
				}
			});
			SwingUtilities.replaceUIActionMap(this, map);
			// set the model to the adapted model
			model = new TristateModel(getModel());
			setModel(model);
			setState(initial);
		}
		
		private void mousePressed() {
			grabFocus();
			model.setPressed(true);
			model.setArmed(true);
		}
		
		private void mouseReleased() {
			model.nextState();
			model.setArmed(false);
			model.setPressed(false);
		}
		
		public void doClick() {
			mousePressed();
			mouseReleased();
		}
	 
		public TristateCheckBox(String text) {
			this(text, NOT_SELECTED);
		}
	 
		public TristateCheckBox() {
			this(null);
		}
	 
		/** No one may add mouse listeners, not even Swing! */
		public void addMouseListener(MouseListener l) {
		}
		
		/** No one may set a new icon */
		public void setIcon(Icon icon) {		
		}
	 
		/**
		 * Set the new state to either CHECKED, CROSSED or NOT_SELECTED.
		 */
		public void setState(State state) {
			model.setState(state);
		}
		
		/**
		 * Return the current state, which is determined by the selection status of
		 * the model.
		 */
		public State getState() {
			return model.getState();
		}	
		
		public void setSelected(boolean selected) {
			if (selected) {
				setState(SELECTED);
			} else {
				setState(NOT_SELECTED);
			}
		}	
	 
		private class TristateModel implements ButtonModel {
			private final ButtonModel other;
	 
			private State currentState = NOT_SELECTED;
	 
			private TristateModel(ButtonModel other) {
				this.other = other;
			}
	 
			private State getState() {
				return currentState;
			}
	 
			private void setState(State state) {
				this.currentState = state;
			}
	 
			public boolean isSelected() {
				return (currentState == SELECTED || currentState == DONT_CARE);
			}
	 
			/** We rotate between NOT_SELECTED, SELECTED and DONT_CARE. */
			private void nextState() {
				State current = getState();
				if (current == NOT_SELECTED) {
					setState(SELECTED);
				} else if (current == SELECTED) {
					setState(DONT_CARE);
				} else if (current == DONT_CARE) {
					setState(NOT_SELECTED);
				}
				
				//This is to enforce a call to the fireStateChanged method
				other.setSelected(!other.isSelected());			
			}
			
		
			public void setArmed(boolean b) {
				other.setArmed(b);
			}
	 
			/**
			 * We disable focusing on the component when it is not enabled.
			 */
			public void setEnabled(boolean b) {
				try {
					setFocusable(b);	
				} catch (Exception ex) {
					ex.printStackTrace();
				}//catch
				
				other.setEnabled(b);
			}
	 
			/**
			 * All these methods simply delegate to the "other" model that is being
			 * decorated.
			 */
			public boolean isArmed() {
				return other.isArmed();
			}
	 
			/* public boolean isSelected() { return other.isSelected(); } */
			public boolean isEnabled() {
				return other.isEnabled();
			}
	 
			public boolean isPressed() {
				return other.isPressed();
			}
	 
			public boolean isRollover() {
				return other.isRollover();
			}
	 
			public void setSelected(boolean b) {
				other.setSelected(b);
			}
	 
			public void setPressed(boolean b) {
				other.setPressed(b);
			}
	 
			public void setRollover(boolean b) {
				other.setRollover(b);
			}
	 
			public void setMnemonic(int key) {
				other.setMnemonic(key);
			}
	 
			public int getMnemonic() {
				return other.getMnemonic();
			}
	 
			public void setActionCommand(String s) {
				other.setActionCommand(s);
			}
	 
			public String getActionCommand() {
				return other.getActionCommand();
			}
	 
			public void setGroup(ButtonGroup group) {
				other.setGroup(group);
			}
	 
			public void addActionListener(ActionListener l) {
				other.addActionListener(l);
			}
	 
			public void removeActionListener(ActionListener l) {
				other.removeActionListener(l);
			}
	 
			public void addItemListener(ItemListener l) {
				other.addItemListener(l);
			}
	 
			public void removeItemListener(ItemListener l) {
				other.removeItemListener(l);
			}
	 
			public void addChangeListener(ChangeListener l) {
				other.addChangeListener(l);
			}
	 
			public void removeChangeListener(ChangeListener l) {
				other.removeChangeListener(l);
			}
	 
			public Object[] getSelectedObjects() {
				return other.getSelectedObjects();
			}
		}
	 
		private class TristateCheckBoxIcon implements Icon, UIResource,
				Serializable {
	 
			protected int getControlSize() {
				return 13;
			}
	 
			public void paintIcon(Component c, Graphics g, int x, int y) {
				JCheckBox cb = (JCheckBox) c;
				TristateModel model = (TristateModel) cb.getModel();
				int controlSize = getControlSize();
	 
				boolean drawCheck = model.getState() == SELECTED;
				boolean drawCross = model.getState() == DONT_CARE;
	 
				if (model.isEnabled()) {
					if (model.isPressed() && model.isArmed()) {
						g.setColor(MetalLookAndFeel.getControlShadow());
						g.fillRect(x, y, controlSize - 1, controlSize - 1);
						drawPressed3DBorder(g, x, y, controlSize, controlSize);
					} else {
						drawFlush3DBorder(g, x, y, controlSize, controlSize);
					}
					g.setColor(MetalLookAndFeel.getControlInfo());
				} else {
					g.setColor(MetalLookAndFeel.getControlShadow());
					g.drawRect(x, y, controlSize - 1, controlSize - 1);
				}
	 
				if (drawCross) {
					drawCross(c, g, x, y);
				}
	 
				if (drawCheck) {
					if (cb.isBorderPaintedFlat()) {
						x++;
					}
					drawCheck(c, g, x, y);
				}
	 
			}// paintIcon
	 
			protected void drawCross(Component c, Graphics g, int x, int y) {
				int controlSize = getControlSize();
				g.drawLine(x + (controlSize - 4), y + 2, x + 3, y
						+ (controlSize - 5));
				g.drawLine(x + (controlSize - 4), y + 3, x + 3, y
						+ (controlSize - 4));
				g.drawLine(x + 3, y + 2, x + (controlSize - 4), y
						+ (controlSize - 5));
				g.drawLine(x + 3, y + 3, x + (controlSize - 4), y
						+ (controlSize - 4));
			}
	 
			protected void drawCheck(Component c, Graphics g, int x, int y) {
				int controlSize = getControlSize();
				g.fillRect(x + 3, y + 5, 2, controlSize - 8);
				g.drawLine(x + (controlSize - 4), y + 3, x + 5, y
						+ (controlSize - 6));
				g.drawLine(x + (controlSize - 4), y + 4, x + 5, y
						+ (controlSize - 5));
			}
	 
			private void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
				g.translate(x, y);
				g.setColor(MetalLookAndFeel.getControlDarkShadow());
				g.drawRect(0, 0, w - 2, h - 2);
				g.setColor(MetalLookAndFeel.getControlHighlight());
				g.drawRect(1, 1, w - 2, h - 2);
				g.setColor(MetalLookAndFeel.getControl());
				g.drawLine(0, h - 1, 1, h - 2);
				g.drawLine(w - 1, 0, w - 2, 1);
				g.translate(-x, -y);
			}
	 
			private void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
				g.translate(x, y);
				drawFlush3DBorder(g, 0, 0, w, h);
				g.setColor(MetalLookAndFeel.getControlShadow());
				g.drawLine(1, 1, 1, h - 2);
				g.drawLine(1, 1, w - 2, 1);
				g.translate(-x, -y);
			}
	 
			public int getIconWidth() {
				return getControlSize();
			}
	 
			public int getIconHeight() {
				return getControlSize();
			}
		}
	}
	
/*	
	private static class TristateCheckBox extends JCheckBox {
		  private final TristateDecorator model;

		  public TristateCheckBox(String text, Icon icon, int initialState){
		    super(text, icon);
		    // Add a listener for when the mouse is pressed
		    super.addMouseListener(new MouseAdapter() {
		      public void mousePressed(MouseEvent e) {
		        grabFocus();
		        model.nextState();
		      }
		    });
		    // Reset the keyboard action map
		    ActionMap map = new ActionMapUIResource();
		    map.put("pressed", new AbstractAction() {
		      public void actionPerformed(ActionEvent e) {
		        grabFocus();
		        model.nextState();
		      }
		    });
		    map.put("released", null);
		    SwingUtilities.replaceUIActionMap(this, map);
		    // set the model to the adapted model
		    model = new TristateDecorator(getModel());
		    setModel(model);
		    setState(initialState);
		  }
		  public TristateCheckBox(String text, int initialState) {
		    this(text, null, initialState);
		  }
		  public TristateCheckBox(String text) {
		    this(text, ZLBoolean3.B3_UNDEFINED);
		  }
		  public TristateCheckBox() {
		    this(null);
		  }

		 
		  public void addMouseListener(MouseListener l) { }
		  
		  public void setState(int state) { model.setState(state); }
		  
		  public int getState() { return model.getState(); }
		  public void setSelected(boolean b) {
		    if (b) {
		      setState(ZLBoolean3.B3_TRUE);
		    } else {
		      setState(ZLBoolean3.B3_FALSE);
		    }
		  }
		 
		  private class TristateDecorator implements ButtonModel {
		    private final ButtonModel other;
		    private TristateDecorator(ButtonModel other) {
		      this.other = other;
		    }
		    private void setState(int state) {
		      if (state == ZLBoolean3.B3_FALSE) {
		        other.setArmed(false);
		        setPressed(false);
		        setSelected(false);
		      } else if (state == ZLBoolean3.B3_TRUE) {
		        other.setArmed(false);
		        setPressed(false);
		        setSelected(true);
		      } else { 
		        other.setArmed(true);
		        setPressed(true);
		        setSelected(true);
		      }
		    }
		   
		    private int getState() {
		      if (isSelected() && !isArmed()) {
		       
		        return ZLBoolean3.B3_TRUE;
		      } else if (isSelected() && isArmed()) {
		       
		        return ZLBoolean3.B3_UNDEFINED;
		      } else {
		       
		        return ZLBoolean3.B3_FALSE;
		      }
		    }
		   
		    private void nextState() {
		      int current = getState();
		      if (current == ZLBoolean3.B3_FALSE) {
		        setState(ZLBoolean3.B3_TRUE);
		      } else if (current == ZLBoolean3.B3_TRUE) {
		        setState(ZLBoolean3.B3_UNDEFINED);
		      } else if (current == ZLBoolean3.B3_UNDEFINED) {
		        setState(ZLBoolean3.B3_FALSE);
		      }
		    }
		    
		    public void setArmed(boolean b) {
		    }
		   
		    public void setEnabled(boolean b) {
		      setFocusable(b);
		      other.setEnabled(b);
		    }
		   
		    public boolean isArmed() { return other.isArmed(); }
		    public boolean isSelected() { return other.isSelected(); }
		    public boolean isEnabled() { return other.isEnabled(); }
		    public boolean isPressed() { return other.isPressed(); }
		    public boolean isRollover() { return other.isRollover(); }
		    public void setSelected(boolean b) { other.setSelected(b); }
		    public void setPressed(boolean b) { other.setPressed(b); }
		    public void setRollover(boolean b) { other.setRollover(b); }
		    public void setMnemonic(int key) { other.setMnemonic(key); }
		    public int getMnemonic() { return other.getMnemonic(); }
		    public void setActionCommand(String s) {
		      other.setActionCommand(s);
		    }
		    public String getActionCommand() {
		      return other.getActionCommand();
		    }
		    public void setGroup(ButtonGroup group) {
		      other.setGroup(group);
		    }
		    public void addActionListener(ActionListener l) {
		      other.addActionListener(l);
		    }
		    public void removeActionListener(ActionListener l) {
		      other.removeActionListener(l);
		    }
		    public void addItemListener(ItemListener l) {
		      other.addItemListener(l);
		    }
		    public void removeItemListener(ItemListener l) {
		      other.removeItemListener(l);
		    }
		    public void addChangeListener(ChangeListener l) {
		      other.addChangeListener(l);
		    }
		    public void removeChangeListener(ChangeListener l) {
		      other.removeChangeListener(l);
		    }
		    public Object[] getSelectedObjects() {
		      return other.getSelectedObjects();
		    }
		  }
		}
	*/	  
	
}
