package org.geometerplus.android.fbreader.fmanager;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FilterView extends Activity{
	// **************************************************************************//
	// Enums //
	// **************************************************************************//

	// **************************************************************************//
	// Members //
	// **************************************************************************//
	
	List<CheckBox> myListChBox = new ArrayList<CheckBox>();
	
	// **************************************************************************//
	// Constructors //
	// **************************************************************************//

	// **************************************************************************//
	// Getters //
	// **************************************************************************//

	// **************************************************************************//
	// Setters //
	// **************************************************************************//

	// **************************************************************************//
	// Publics //
	// **************************************************************************//
	/**
	 * Called when the activity is first created. Responsible for initializing
	 * the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter);

		myListChBox.add((CheckBox) findViewById(R.id.checkboxTypeTXT));
		myListChBox.add((CheckBox) findViewById(R.id.checkboxTypeFB2));
		myListChBox.add((CheckBox) findViewById(R.id.checkboxTypePDF));
		myListChBox.add((CheckBox) findViewById(R.id.checkboxTypeDOC));
		myListChBox.add((CheckBox) findViewById(R.id.checkboxTypeDOCX));
		myListChBox.add((CheckBox) findViewById(R.id.checkboxTypeODT));
		
		setCurrentTypes();
		
		CheckBox checkBoxAll = (CheckBox)findViewById(R.id.checkboxAll);
		checkBoxAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				for (CheckBox ch : myListChBox){
					ch.setChecked(isChecked);
				}
			}
		});
		
		
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		Button okButton = (Button) findViewById(R.id.okButton);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String filterStr = "";
				for (CheckBox ch : myListChBox){
					if (ch.isChecked())
						filterStr += ch.getText().toString() + " ";
				}
				setResult(1, new Intent(filterStr));
				finish();
			}
		});
	}

	// **************************************************************************//
	// Abstracts //
	// **************************************************************************//

	// **************************************************************************//
	// Protected //
	// **************************************************************************//

	// **************************************************************************//
	// Privates //
	// **************************************************************************//
	private void setCurrentTypes(){
		String types = getIntent().getAction();
		if (types.equals(""))
			return;
		for (String type : types.split("[\\s]+")) {
			for(CheckBox chBox : myListChBox){
				if (type.equals(chBox.getText().toString())){
					chBox.setChecked(true);
					break;
				}
			}
		}
	}
	
	// **************************************************************************//
	// Public Statics //
	// **************************************************************************//

	// **************************************************************************//
	// Private Statics //
	// **************************************************************************//

	// **************************************************************************//
	// Internal Classes //
	// **************************************************************************//
	
	
}
