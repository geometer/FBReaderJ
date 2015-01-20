package com.yotadevices.sdk.helper;

/**
 * Copyright 2012 Yota Devices LLC, Russia
 
 * This source code is Yota Devices Confidential Proprietary
 * This software is protected by copyright.  All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or
 * reverse engineer the software. Otherwise this violation would be treated by 
 * law and would be subject to legal prosecution.  Legal use of the software 
 * provides receipt of a license from the right holder only.
 
 * */

import android.content.ComponentName;

interface IFrameworkService {

    void goToSleep();
    
    void wakeUp();
    
    void lockOn();
    
    void lockOff();
    
    void lockBackScreen(); 
    
    void unlockBackScreen();
    
    boolean isLockScreenDisabled();
    
    void performSingleUpdate(in int waveform);

    ComponentName getTopBSActivity();

    boolean isMirroringOn();
}