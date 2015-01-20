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

import android.os.Bundle;

//for future
/**
 * @hide
 */
public class BSInstrumentation {

    public void callOnBSSaveInstanceState(BSActivity activity, Bundle outState) {
        throw new RuntimeException("Not implemented");
    }

    public void callOnBSRestoreInstanceState(BSActivity activity, Bundle saveInstanceState) {
        throw new RuntimeException("Not implemented");
    }

}
