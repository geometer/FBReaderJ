/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.resources.ZLResource;
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
	private List<CheckBox> myListChBox = new ArrayList<CheckBox>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter);

		myListChBox.add(getCheckBox(R.id.checkboxTypeTXT, "txt"));
		myListChBox.add(getCheckBox(R.id.checkboxTypeFB2, "fb2"));
		myListChBox.add(getCheckBox(R.id.checkboxTypePDF, "pdf"));
		myListChBox.add(getCheckBox(R.id.checkboxTypeDOC, "doc"));
		myListChBox.add(getCheckBox(R.id.checkboxTypeDOCX, "docx"));
		myListChBox.add(getCheckBox(R.id.checkboxTypeODT, "odt"));
		
		setCurrentTypes();
		
		CheckBox checkBoxAll = getCheckBox(R.id.checkboxAll, "all");
		checkBoxAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				for (CheckBox ch : myListChBox){
					ch.setChecked(isChecked);
				}
			}
		});
		
		Button okButton = FileUtils.getOkBtn(this, R.id.okButton);
		Button cancelButton = FileUtils.getCancelBtn(this, R.id.cancelButton);

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

	private void setCurrentTypes(){
//		String types = getIntent().getExtras().getString(FileManager.FILE_MANAGER_TYPE);
//		types = types != null ? types : "";
		String types = "";
		
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
	
	private CheckBox getCheckBox(int id, String keyName){
		ZLResource resource = ZLResource.resource("fmanagerView").getResource("types");
		CheckBox chBox =  (CheckBox) findViewById(id);
		chBox.setText(resource.getResource(keyName).getValue());
		return chBox;
	}
}
