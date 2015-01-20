package com.yotadevices.sdk;

/**
 * Copyright 2012 Yota Devices LLC, Russia
 * 
 * This source code is Yota Devices Confidential Proprietary
 * This software is protected by copyright.  All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or
 * reverse engineer the software. Otherwise this violation would be treated by 
 * law and would be subject to legal prosecution.  Legal use of the software 
 * provides receipt of a license from the right holder only.
 * 
 * */

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