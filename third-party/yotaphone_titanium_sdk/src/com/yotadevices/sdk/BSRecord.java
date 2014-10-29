package com.yotadevices.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;

/**
 * @hide
 */
class BSRecord {
    private final static String PREF_NAME = "instanceState";
    private Context mContext;
    private String mName;

    /**
     * Last retrieved freeze state.
     */
    private Bundle mInstanceState = new Bundle();

    public BSRecord(Context context, String name) {
        mContext = context;
        mName = name;
    }

    public Bundle getData() {
        return mInstanceState;
    }

    private SharedPreferences getPreference() {
        return mContext.getSharedPreferences("_" + mName, Context.MODE_PRIVATE);
    }

    synchronized void saveState() {
        Parcel parcel = Parcel.obtain();
        try {
            mInstanceState.writeToParcel(parcel, 0);
            String savedState = Base64.encodeToString(parcel.marshall(), 0);
            if (savedState != null) {
                Editor editor = getPreference().edit();
                editor.putString(PREF_NAME, savedState);
                editor.commit();
            }
        } finally {
            parcel.recycle();
        }
    }

    synchronized void restoreState() {
        String savedState = getPreference().getString(PREF_NAME, null);
        if (savedState != null) {
            Parcel parcel = Parcel.obtain();
            try {
                byte[] data = Base64.decode(savedState, 0);
                parcel.unmarshall(data, 0, data.length);
                parcel.setDataPosition(0);
                mInstanceState = parcel.readBundle();
            } finally {
                parcel.recycle();
            }
        }
    }
}