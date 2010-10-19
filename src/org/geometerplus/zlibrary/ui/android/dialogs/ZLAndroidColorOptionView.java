/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;
import android.graphics.Color;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.dialogs.ZLColorOptionEntry;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

class ZLAndroidColorOptionView extends ZLAndroidOptionView {
	private View myContainer;
	private ComponentView myRedView;
	private ComponentView myGreenView;
	private ComponentView myBlueView;
	private View myColorArea;

	protected ZLAndroidColorOptionView(ZLAndroidDialogContent tab, String name, ZLColorOptionEntry option) {
		super(tab, name, option);
	}

	private class ComponentView extends LinearLayout {
		private int myValue;
		private TextView myValueView;
		private Button myLeftLeftButton;
		private Button myLeftButton;
		private Button myRightButton;
		private Button myRightRightButton;

		ComponentView(Context context, String labelText, int initialValue) {
			super(context);
			setOrientation(LinearLayout.HORIZONTAL);
			setHorizontalGravity(0x05);
    
			TextView label = new TextView(context);
			label.setText(labelText + ":");
			label.setPadding(0, 6, 0, 6);
			label.setTextSize(16);
    
			myLeftLeftButton = new Button(context) {
				public boolean onTouchEvent(MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (myValue > 0) {
							myValue -= 20;
							if (myValue < 0) {
								myValue = 0;
							}
							setComponentValue(myValue);
						}
					}
					return true;
				}
			};
			myLeftLeftButton.setText("<<");
			myLeftLeftButton.setTextSize(14);
    
			myLeftButton = new Button(context) {
				public boolean onTouchEvent(MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (myValue > 0) {
							setComponentValue(--myValue);
						}
					}
					return true;
				}
			};
			myLeftButton.setText("<");
			myLeftButton.setTextSize(14);
    
			myValueView = new TextView(context);
			myValueView.setPadding(2, 6, 2, 6);
			myValueView.setTextSize(16);
    
			myRightButton = new Button(context) {
				public boolean onTouchEvent(MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (myValue < 255) {
							setComponentValue(++myValue);
						}
					}
					return true;
				}
			};
			myRightButton.setText(">");
			myRightButton.setTextSize(14);
    
			myRightRightButton = new Button(context) {
				public boolean onTouchEvent(MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (myValue < 255) {
							myValue += 20;
							if (myValue > 255) {
								myValue = 255;
							}
							setComponentValue(myValue);
						}
					}
					return true;
				}
			};
			myRightRightButton.setText(">>");
			myRightRightButton.setTextSize(14);

			myValue = initialValue;
			setComponentValue(initialValue);
    
			addView(label, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			addView(myLeftLeftButton, new LayoutParams(40, 40));
			addView(myLeftButton, new LayoutParams(40, 40));
			addView(myValueView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			addView(myRightButton, new LayoutParams(40, 40));
			addView(myRightRightButton, new LayoutParams(40, 40));
		}

		int getComponentValue() {
			return myValue;
		}

		void setComponentValue(int value) {
			myValue = value;
			String txtValue = "" + value;
			switch (txtValue.length()) {
				case 1:
					txtValue = "  " + txtValue;
					break;
				case 2:
					txtValue = " " + txtValue;
					break;
			}
			myValueView.setText(txtValue);
			myLeftLeftButton.setEnabled(value > 0);
			myLeftButton.setEnabled(value > 0);
			myRightButton.setEnabled(value < 255);
			myRightRightButton.setEnabled(value < 255);
			updateColorArea();
		}
	};

	void addAndroidViews() {
		if (myContainer == null) {
			final Context context = myTab.getContext();
    
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setPadding(0, 6, 0, 0);
    
			final ZLColorOptionEntry colorOption = (ZLColorOptionEntry)myOption;
			final ZLColor color = colorOption.initialColor();
    
			final ZLResource resource = ZLResource.resource(ZLAndroidDialogManager.COLOR_KEY);
			myRedView = new ComponentView(context, resource.getResource("red").getValue(), color.Red);
			myGreenView = new ComponentView(context, resource.getResource("green").getValue(), color.Green);
			myBlueView = new ComponentView(context, resource.getResource("blue").getValue(), color.Blue);
    
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(myRedView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(myGreenView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(myBlueView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    
			myColorArea = new View(context);
			layout.setPadding(20, 0, 20, 0);
			myColorArea.setMinimumHeight(60);
			myColorArea.setBackgroundColor(ZLAndroidColorUtil.rgb(color));
			layout.addView(myColorArea, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
    
			myContainer = layout;
		}

		myTab.addAndroidView(myContainer, true);
	}

	private void updateColorArea() {
		if (myColorArea != null) {
			myColorArea.setBackgroundColor(Color.rgb(
				myRedView.getComponentValue(),
				myGreenView.getComponentValue(),
				myBlueView.getComponentValue()
			));
		}
	}

	protected void reset() {
		if (myContainer != null) {
			final ZLColorOptionEntry colorOption = (ZLColorOptionEntry)myOption;
			final ZLColor color = colorOption.getColor();
			colorOption.onReset(new ZLColor(
				myRedView.getComponentValue(),
				myGreenView.getComponentValue(),
				myBlueView.getComponentValue()
			));
			myRedView.setComponentValue(color.Red);
			myGreenView.setComponentValue(color.Green);
			myBlueView.setComponentValue(color.Blue);
			myColorArea.setBackgroundColor(ZLAndroidColorUtil.rgb(color));
		}
	}

	protected void _onAccept() {
		if (myContainer != null) {
			((ZLColorOptionEntry)myOption).onAccept(new ZLColor(
				myRedView.getComponentValue(),
				myGreenView.getComponentValue(),
				myBlueView.getComponentValue()
			));
			myContainer = null;
			myColorArea = null;
			myRedView = null;
			myGreenView = null;
			myBlueView = null;
		}
	}
}
