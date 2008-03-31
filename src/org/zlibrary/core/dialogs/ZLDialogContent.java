package org.zlibrary.core.dialogs;

import java.util.*;

import org.zlibrary.core.util.*;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.optionEntries.ZLSimpleBoolean3OptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleBooleanOptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleStringOptionEntry;
import org.zlibrary.core.options.*;

public abstract class ZLDialogContent {
	private final ZLResource myResource;
	private final ArrayList myViews = new ArrayList();
	
	private static ZLOptionEntry createEntryByOption(ZLSimpleOption option) {
		switch (option.getType()) {
		case ZLSimpleOption.Type.BOOLEAN:
			return new ZLSimpleBooleanOptionEntry((ZLBooleanOption) option);
		case ZLSimpleOption.Type.BOOLEAN3:
			return new ZLSimpleBoolean3OptionEntry((ZLBoolean3Option) option);
		case ZLSimpleOption.Type.STRING:
			return new ZLSimpleStringOptionEntry((ZLStringOption) option);
		default:
			return null;
		}
	}

	protected ArrayList getViews() {
		return myViews;
	}
	
	protected ZLDialogContent(ZLResource resource) {
		myResource = resource;
	}

	public final String getKey() {
		return myResource.Name;
	}
		
	public final String getDisplayName() {
		return myResource.getValue();
	}
	
	public final String getValue(String key) {
		return myResource.getResource(key).getValue();
	}
	
	public final ZLResource getResource(String key) {
		return myResource.getResource(key);
	}

	public abstract void addOptionByName(String name, ZLOptionEntry option);
	
	public final void addOption(String key, ZLOptionEntry option) {
		addOptionByName(myResource.getResource(key).getValue(), option);
	}
	
	public final void addOption(String key, ZLSimpleOption option) {
		addOption(key, createEntryByOption(option));
	}
	
	public abstract void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1);
	
	public final void addOptions(String key0, ZLOptionEntry option0, String key1, ZLOptionEntry option1) {
		final ZLResource resource0 = myResource.getResource(key0);
		final ZLResource resource1 = myResource.getResource(key1);
		addOptionsByNames(resource0.getValue(), option0, resource1.getValue(), option1);
	}
	
	public final void addOptions(String key0, ZLSimpleOption option0, String key1, ZLSimpleOption option1) {
		addOptions(key0, createEntryByOption(option0), key1, createEntryByOption(option1));
	}

	final void accept() {
		final int size = myViews.size();
		for (int i = 0; i < size; i++) {
			((ZLOptionView)myViews.get(i)).onAccept();
		}
	}

	protected final void addView(ZLOptionView view) {
		if (view != null) {
			myViews.add(view);
		}
	}
}
