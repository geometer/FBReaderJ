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
    private BSActivity mBSActivity;

    /**
     * Last retrieved freeze state.
     */
    private Bundle mInstanceState = new Bundle();

    public BSRecord(BSActivity activity) {
        mBSActivity = activity;
    }

    public Bundle getData() {
        return mInstanceState;
    }

    private SharedPreferences getPreference() {
        Context ctx = mBSActivity.getApplicationContext();
        return ctx.getSharedPreferences("_" + mBSActivity.getClass().getName(), Context.MODE_PRIVATE);
    }

    void saveState() {
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

    void restoreState() {
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