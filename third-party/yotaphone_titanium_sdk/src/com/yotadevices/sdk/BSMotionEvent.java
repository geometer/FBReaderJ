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

import com.yotadevices.sdk.Constants.Gestures;

/**
 * Motion event that handles on BS.
 * @author asazonov
 *
 */
public class BSMotionEvent {
    private Gestures mAction;

    /**
     * @return action code (e.g. ACTION_SWIPE_LEFT)
     */
    public Gestures getBSAction() {
        return mAction;
    }

    /**
     * Set action to be translated into methods.
     * @param mAction gesture.
     */
    public void setBSAction(Gestures mAction) {
        this.mAction = mAction;
    }
}
