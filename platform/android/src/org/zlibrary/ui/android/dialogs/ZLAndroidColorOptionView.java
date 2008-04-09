package org.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.util.ZLColor;
import org.zlibrary.core.dialogs.ZLColorOptionEntry;
import org.zlibrary.core.resources.ZLResource;

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

	protected void createItem() {
		final Context context = myTab.getView().getContext();

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(0, 6, 0, 0);

		final ZLColorOptionEntry colorOption = (ZLColorOptionEntry)myOption;
		final ZLColor color = colorOption.initialColor();

		final ZLResource resource = ZLResource.resource(ZLAndroidDialogManager.COLOR_KEY);
		myRedView = new ComponentView(context, resource.getResource("red").getValue(), color.Red);
		myRedView.setPadding(0, 6, 0, 0);
		myGreenView = new ComponentView(context, resource.getResource("green").getValue(), color.Green);
		myBlueView = new ComponentView(context, resource.getResource("blue").getValue(), color.Blue);
		myColorArea = new View(context);
		myColorArea.setBackgroundColor(0xFF000000 + (color.Red << 16) + (color.Green << 8) + color.Blue);

		layout.addView(myColorArea, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(myRedView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(myGreenView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(myBlueView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		myContainer = layout;
		addAndroidView(layout, true);
	}

	private void updateColorArea() {
		if (myColorArea != null) {
			myColorArea.setBackgroundColor(
				0xFF000000 |
				(myRedView.getComponentValue() << 16) |
				(myGreenView.getComponentValue() << 8) |
				myBlueView.getComponentValue()
			);
		}
	}

	protected void reset() {
		if (myColorArea == null) {
			return;
		}
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
		myColorArea.setBackgroundColor(0xFF000000 + (color.Red << 16) + (color.Green << 8) + color.Blue);
	}

	protected void _onAccept() {
		((ZLColorOptionEntry)myOption).onAccept(new ZLColor(
			myRedView.getComponentValue(),
			myGreenView.getComponentValue(),
			myBlueView.getComponentValue()
		));
	}
}
