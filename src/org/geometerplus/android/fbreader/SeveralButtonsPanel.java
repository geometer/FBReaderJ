package org.geometerplus.android.fbreader;

import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ZoomButton;

public abstract class SeveralButtonsPanel extends ControlButtonPanel implements View.OnClickListener {
	class ActionButton extends ZoomButton {
		final String ActionId;
		final boolean IsCloseButton;

		ActionButton(Context context, String actionId, boolean isCloseButton) {
			super(context);
			ActionId = actionId;
			IsCloseButton = isCloseButton;
		}
	}

	private final ArrayList<ActionButton> myButtons = new ArrayList<ActionButton>();

	SeveralButtonsPanel(FBReaderApp fbReader) {
		super(fbReader);
	}

	@Override
	public void createControlPanel(FBReader activity, RelativeLayout root) {
		myControlPanel = new ControlPanel(activity, root, false);

		onAddButtons();

	}
	protected abstract void onAddButtons();

	protected void addButton(String actionId, boolean isCloseButton, int imageId) {
		final ActionButton button = new ActionButton(myControlPanel.getContext(), actionId, isCloseButton);
		button.setImageResource(imageId);
		myControlPanel.addView(button);
		button.setOnClickListener(this);
		myButtons.add(button);
	}

	@Override
	public void updateStates() {
		for (ActionButton button : myButtons) {
			button.setEnabled(Reader.isActionEnabled(button.ActionId));
		}
	}

	public void onClick(View view) {
		final ActionButton button = (ActionButton)view;
		Reader.doAction(button.ActionId);
		if (button.IsCloseButton && myControlPanel != null) {
			storePosition();
			StartPosition = null;
			hide(true);
			//            myVisible = false; // needed for actions, bringing another activites in front of current.
		}
	}
}
