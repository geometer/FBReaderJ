package org.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLChoiceOptionEntry;

class ZLAndroidChoiceOptionView extends ZLAndroidOptionView {
	RadioGroup myGroup;

	protected ZLAndroidChoiceOptionView(ZLAndroidDialogContent tab, String name, ZLChoiceOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		final Context context = myTab.getView().getContext();
		myGroup = new RadioGroup(context);
		myGroup.setOrientation(RadioGroup.VERTICAL);

		final ZLChoiceOptionEntry choiceEntry = (ZLChoiceOptionEntry)myOption;
		final int choiceNumber = choiceEntry.choiceNumber();
		for (int i = 0; i < choiceNumber; i++) {
			final RadioButton button = new RadioButton(context);
			button.setId(i + 1);
			button.setText(choiceEntry.getText(i));
			myGroup.addView(button, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
		}
		myGroup.check(choiceEntry.initialCheckedIndex() + 1);
	}

	void addAndroidViews() {
		myTab.addAndroidView(myGroup, true);
	}

	protected void reset() {
		final ZLChoiceOptionEntry choiceEntry = (ZLChoiceOptionEntry)myOption;
		myGroup.check(choiceEntry.initialCheckedIndex() + 1);
	}

	protected void _onAccept() {
		((ZLChoiceOptionEntry)myOption).onAccept(myGroup.getCheckedRadioButtonId() - 1);
	}
}
