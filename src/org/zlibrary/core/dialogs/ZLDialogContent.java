package org.zlibrary.core.dialogs;

import java.util.ArrayList;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.options.ZLSimpleOption;
import org.zlibrary.core.util.*;

public abstract class ZLDialogContent {
	private static final String TOOLTIP_KEY = "tooltip";
	
	private final ZLResource myResource;
	private final ArrayList /*<ZLOptionView>*/ myViews = new ArrayList();
	
	protected ZLDialogContent(ZLResource resource){
		myResource = resource;
	}

	public String getKey() {
		return myResource.Name;
	}
		
	public String getDisplayName() {
		return myResource.getValue();
	}
	
	public String getValue(String key) {
		return myResource.getResource(key).getValue();
	}
	
	public ZLResource getResource(String key) {
		return myResource.getResource(key);
	}
/*
	public abstract void addOption(String name, String tooltip, ZLOptionEntry option);
	
	public void addOption(String key, ZLOptionEntry option) {
		
	}
	
	public void addOption(String key, ZLSimpleOption option) {
		
	}
	
	public abstract void addOptions(String name0, String tooltip0, ZLOptionEntry option0,
														String name1, String tooltip1, ZLOptionEntry option1);
	
	public	void addOptions(String key0, ZLOptionEntry option0, String key1, ZLOptionEntry option1) {
		
	}
*/	
	public	void addOptions(String key0, ZLSimpleOption option0, String key1, ZLSimpleOption option1) {
		
	}

	public void accept() {
		
	}

	protected void addView(ZLOptionView view) {
		if (view != null) {
			myViews.add(view);
		}
	}
}
