package org.zlibrary.text.view;

import java.util.ArrayList;

import org.zlibrary.core.optionEntries.ZLFontFamilyOptionEntry;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.view.ZLPaintContext;

public class ZLTextFontFamilyWithBaseOptionEntry extends ZLFontFamilyOptionEntry {
	private static final String KEY_UNCHANGED = "unchanged";
	private static final ArrayList /*<std::string>*/ ourAllFamilies = new ArrayList();
	private final ZLResource myResource;
		
	public ZLTextFontFamilyWithBaseOptionEntry(ZLStringOption option, ZLPaintContext context, ZLResource resource) {
		super(option, context);
		myResource = resource;
	}

	public ArrayList getValues() {
		if (ourAllFamilies.size() == 0) {
			final ArrayList families = super.getValues();
			ourAllFamilies.add(myResource.getResource(KEY_UNCHANGED).getValue());
			ourAllFamilies.addAll(families);
		}
		return ourAllFamilies;
	}

	public String initialValue() {
		final String value = super.initialValue();
		return (value == null || value.equals("")) ? (String) (getValues().get(0)) : value;
	}

	public void onAccept(String value) {
		super.onAccept((value.equals(getValues().get(0))) ? "" : value);
	}
}
