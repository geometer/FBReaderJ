package org.geometerplus.zlibrary.ui.android.dialogs;

import android.preference.*;
import android.os.Bundle;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.core.dialogs.*;

public class OptionsActivity extends PreferenceActivity {
	static final Object DIALOG_KEY = new Object();

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		//addPreferencesFromResource(R.xml.preferences);
		PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		ZLAndroidOptionsDialog dialog = (ZLAndroidOptionsDialog)application.getData(DIALOG_KEY);
		for (Object tab : dialog.tabs()) {
			PreferenceCategory category = new PreferenceCategory(this);
			ZLAndroidOptionsTab optionsTab = (ZLAndroidOptionsTab)tab;
			category.setTitle(optionsTab.getDisplayName());
			preferenceScreen.addPreference(category);

			for (ZLAndroidOptionsTab.OptionData data : optionsTab.myEntries) {
				Preference test;
				if (data.Entry instanceof ZLBooleanOptionEntry) {
					test = new CheckBoxPreference(this);
				} else if (data.Entry instanceof ZLStringOptionEntry) {
					test = new EditTextPreference(this);
					((EditTextPreference)test).setText("Hello, World!");
				} else {
					continue;
				}
				test.setTitle(data.Name);
				test.setSummary("Test summary");
				category.addPreference(test);
			}
		}
		setPreferenceScreen(preferenceScreen);
	}
}
