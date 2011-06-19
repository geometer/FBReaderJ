/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

import android.os.Parcel;
import android.os.Parcelable;

public final class TextPosition extends ApiObject {
	public final int ParagraphIndex;
	public final int ElementIndex;
	public final int CharIndex;

	public TextPosition(int paragraphIndex, int elementIndex, int charIndex) {
		ParagraphIndex = paragraphIndex;
		ElementIndex = elementIndex;
		CharIndex = charIndex;
	}

	@Override
	protected int type() {
		return Type.TEXT_POSITION;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		super.writeToParcel(parcel, flags);
		parcel.writeInt(ParagraphIndex);
		parcel.writeInt(ElementIndex);
		parcel.writeInt(CharIndex);
	}

	public static final Parcelable.Creator<TextPosition> CREATOR =
		new Parcelable.Creator<TextPosition>() {
			public TextPosition createFromParcel(Parcel parcel) {
				parcel.readInt();
				return new TextPosition(parcel.readInt(), parcel.readInt(), parcel.readInt());
			}

			public TextPosition[] newArray(int size) {
				return new TextPosition[size];
			}
		};
}
