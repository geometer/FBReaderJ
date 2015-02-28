/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.preferences;

import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class StringPreference extends DialogPreference {
	public static class Constraint {
		public static final Constraint LENGTH = new Constraint(
			"-{0,1}([0-9]*\\.){0,1}[0-9]+(%|em|ex|px|pt)|",
			"length"
		);
		public static final Constraint POSITIVE_LENGTH = new Constraint(
			"([0-9]*\\.){0,1}[0-9]+(%|em|ex|px|pt)|",
			"positiveLength"
		);
		public static final Constraint PERCENT = new Constraint(
			"([1-9][0-9]{1,2}%)|",
			"percent"
		);

		private final Pattern myPattern;
		public final String HintKey;

		public Constraint(String pattern, String hintKey) {
			myPattern = Pattern.compile(pattern);
			HintKey = hintKey;
		}

		public boolean matches(String text) {
			return myPattern.matcher(text).matches();
		}
	}

	private final ZLStringOption myOption;
	private final Constraint myConstraint;
	private EditText myEditor;

	protected StringPreference(Context context, ZLStringOption option, Constraint constraint, ZLResource rootResource, String resourceKey) {
		super(context, null);

		myOption = option;
		myConstraint = constraint;

		final String title = rootResource.getResource(resourceKey).getValue();
		setTitle(title);
		setDialogTitle(title);
		setDialogLayoutResource(R.layout.string_preference_dialog);
		setSummary(option.getValue());

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		setPositiveButtonText(buttonResource.getResource("ok").getValue());
		setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}

	protected void setValue(String value) {
		setSummary(value);
		myOption.setValue(value);
	}

	@Override
	protected void onBindDialogView(View view) {
		myEditor = (EditText)view.findViewById(R.id.string_preference_editor);
		myEditor.setText(myOption.getValue());
		((TextView)view.findViewById(R.id.string_preference_hint)).setText(
			ZLResource.resource("hint").getResource(myConstraint.HintKey).getValue()
		);

		super.onBindDialogView(view);
	}

	private final TextWatcher myWatcher = new TextWatcher() {
		@Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before, int count) {
		}

        @Override
        public void afterTextChanged(Editable s) {
			final AlertDialog dialog = (AlertDialog)getDialog();
			final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			okButton.setEnabled(myConstraint.matches(myEditor.getText().toString()));
        }
	};

	@Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        myEditor.removeTextChangedListener(myWatcher);
        myEditor.addTextChangedListener(myWatcher);
        myWatcher.afterTextChanged(null);
    }

	@Override
	protected void onDialogClosed(boolean result) {
		if (result) {
			setValue(myEditor.getText().toString());
		}
		super.onDialogClosed(result);
	}
}
