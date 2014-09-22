/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.util;

import java.util.ArrayList;
import java.lang.Integer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class EditSeriesDialogActivity extends Activity {
	public static final int REQ_CODE = 011;

    private final int MAX_SERIES_INDEX = 9999;

	public interface Key {
		final String SERIES_NAME			= "edit_series.name";
		final String SERIES_INDEX			= "edit_series.index";
		final String ACTIVITY_TITLE         = "edit_series.title";
		final String ALL_SERIES				= "edit_series.all_series";
	}

	private final String SERIES_NAME_FILTER = "[\\p{L}0-9_\\-& ]*";
	private AutoCompleteTextView myInputField;
	private EditText myIndexField;

	private ArrayList<String> allSeriesNames;
    private String mySeriesName;
	private int mySeriesIndex;
	private ZLResource myResource;
    private ZLResource buttonResource;
    private ZLResource removeDialogResource;
	private NumberPicker myNumberPicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_series_dialog);

		myResource = ZLResource.resource("dialog").getResource("editSeries");
        buttonResource = ZLResource.resource("dialog").getResource("button");
        removeDialogResource = myResource.getResource("removeDialog");
		
		final Intent intent = getIntent();
		allSeriesNames = intent.getStringArrayListExtra(EditSeriesDialogActivity.Key.ALL_SERIES);
		mySeriesName = intent.getStringExtra(EditSeriesDialogActivity.Key.SERIES_NAME);
		mySeriesIndex = intent.getIntExtra(EditSeriesDialogActivity.Key.SERIES_INDEX, 0);

        if(mySeriesName != null && !mySeriesName.isEmpty()){
            setTitle(myResource.getResource("editSeries").getValue());
        }else{
            setTitle(myResource.getResource("addSeries").getValue());
        }
				
		parseUIElements();
		
		setResult(RESULT_CANCELED);
	}
	
	private void parseUIElements(){
		((TextView) findViewById(R.id.edit_series_name_label)).setText(myResource.getResource("seriesNameLabel").getValue());
		((TextView) findViewById(R.id.edit_series_index_label)).setText(myResource.getResource("seriesIndexLabel").getValue());
        
        //Getting the number picker from an layout for API greater then 11
		myNumberPicker = (NumberPicker) findViewById(R.id.edit_series_index_picker);
		if(myNumberPicker != null){
			myNumberPicker.setMinValue(0);
			myNumberPicker.setMaxValue(MAX_SERIES_INDEX);
			myNumberPicker.setValue(mySeriesIndex);
			myNumberPicker.setOnValueChangedListener(new OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					addIndex(newVal);
				}
			});
		}

        //Getting the edit textfield from an layout for API less then 11
		myIndexField = (EditText)findViewById(R.id.edit_series_index_input_field);
		if(myIndexField != null)
		{
			myIndexField.setHint(myResource.getResource("addSeriesIndexHint").getValue());
            if(mySeriesIndex > 0){
			    myIndexField.setText(Integer.toString(mySeriesIndex));
            }
			myIndexField.setOnEditorActionListener(new TextView.OnEditorActionListener(){
				public boolean onEditorAction (TextView v, int actionId, KeyEvent event){
					if(actionId == EditorInfo.IME_ACTION_DONE){
						addIndex(Integer.parseInt(myIndexField.getText().toString()));
						return false;
					}	
					return true;
				}
			});
		}

		myInputField = (AutoCompleteTextView)findViewById(R.id.edit_series_input_field);
		myInputField.setHint(myResource.getResource("addSeriesNameHint").getValue());
		if(mySeriesName != ""){
			myInputField.setText(mySeriesName);
		}
		myInputField.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction (TextView v, int actionId, KeyEvent event){
				if(actionId == EditorInfo.IME_ACTION_DONE){
					addSeries(myInputField.getText().toString().trim());
					return false;
				}
				return true;
			}
		});
		myInputField.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, allSeriesNames));

		final Button okButton = (Button)findViewById(R.id.edit_dialog_button_ok);
		if(okButton != null){
			okButton.setText(buttonResource.getResource("ok").getValue());
			okButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					addSeries(myInputField.getText().toString().trim());
                    if(myIndexField != null){
                        if(myIndexField.getText().length() > 0){
                            addIndex(Integer.parseInt(myIndexField.getText().toString()));
                        }else{
                            mySeriesIndex = 0;
                        }
                    }
                    if(mySeriesName.isEmpty()){
                        mySeriesIndex = 0;
                    }
					setResult(RESULT_OK, 
                            new Intent()
                            .putExtra(Key.SERIES_NAME, mySeriesName)
                            .putExtra(Key.SERIES_INDEX, mySeriesIndex)
                    );
					finish();
				}
			});
		}
        final Button deleteButton = (Button)findViewById(R.id.edit_dialog_button_delete);
		if(deleteButton != null){
            if(!mySeriesName.isEmpty()){
			    deleteButton.setText(myResource.getResource("deleteSeriesButton").getValue());
			    deleteButton.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
                        onDelete();
				    }
			    });
            }else{
                deleteButton.setVisibility(View.GONE);
            }
		}
		final Button cancelButton = (Button)findViewById(R.id.edit_dialog_button_cancel);
		if(cancelButton != null){
			cancelButton.setText(buttonResource.getResource("cancel").getValue());
			cancelButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					setResult(RESULT_CANCELED);
					finish();
				}
			});
		}
	}
    
    private void onDelete(){
        if(mySeriesName.isEmpty())
            return;

        new AlertDialog.Builder(EditSeriesDialogActivity.this)
			.setCancelable(false)
			.setTitle(removeDialogResource.getValue())
			.setMessage(removeDialogResource.getResource("message").getValue().replace("%s", mySeriesName))
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
                    mySeriesName = "";
                    mySeriesIndex = 0;
					setResult(RESULT_OK, 
                            new Intent()
                            .putExtra(Key.SERIES_NAME, mySeriesName)
                            .putExtra(Key.SERIES_INDEX, mySeriesIndex)
                    );
                    finish();
				}
			})
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), null)
			.create().show();
    }

	private void addSeries(String seriesName){
		mySeriesName = seriesName;
	}
	
	private void addIndex(int index){
		if(index > 0){
			mySeriesIndex = index;
		}
	}
}
