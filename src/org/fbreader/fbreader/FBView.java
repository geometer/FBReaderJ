package org.fbreader.fbreader;


import org.zlibrary.core.options.*;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.view.impl.ZLTextViewImpl;

public abstract class FBView extends ZLTextViewImpl {
	private static ZLIntegerRangeOption ourLeftMarginOption;
	private static ZLIntegerRangeOption ourRightMarginOption;
	private static ZLIntegerRangeOption ourTopMarginOption;
	private static ZLIntegerRangeOption ourBottomMarginOption;

	private String myCaption;
	
	private ZLIntegerRangeOption createMarginOption(String name, int defaultValue) {
		return new ZLIntegerRangeOption(
			ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", name, 0, 1000, defaultValue
		);
	}

	FBView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public final ZLIntegerRangeOption getLeftMarginOption() {
		if (ourLeftMarginOption == null) {
			ourLeftMarginOption = createMarginOption("LeftMargin", 4);
		}
		return ourLeftMarginOption;
	}
	public int getLeftMargin() {
		return getLeftMarginOption().getValue();
	}

	public final ZLIntegerRangeOption getRightMarginOption() {
		if (ourRightMarginOption == null) {
			ourRightMarginOption = createMarginOption("RightMargin", 4);
		}
		return ourRightMarginOption;
	}
	public int getRightMargin() {
		return getRightMarginOption().getValue();
	}

	public final ZLIntegerRangeOption getTopMarginOption() {
		if (ourTopMarginOption == null) {
			ourTopMarginOption = createMarginOption("TopMargin", 0);
		}
		return ourTopMarginOption;
	}
	public int getTopMargin() {
		return getTopMarginOption().getValue();
	}

	public final ZLIntegerRangeOption getBottomMarginOption() {
		if (ourBottomMarginOption == null) {
			ourBottomMarginOption = createMarginOption("BottomMargin", 4);
		}
		return ourBottomMarginOption;
	}
	public int getBottomMargin() {
		return getBottomMarginOption().getValue();
	}

	FBReader getFBReader() {
		return (FBReader)getApplication();
	}
	
	public void setCaption(String caption) {
		myCaption = caption;
	}
}
