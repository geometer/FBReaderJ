package com.yotadevices.sdk.utils;

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

/**
 * @hide
 */
public interface IPowerCallback {

    /**
     * Forces the device to go to sleep. Overrides all the wake locks that are
     * held. This is what happens when the power key is pressed to turn off the
     * screen.
     */
    public void goToSleep();

    /**
     * Forces the device to wake up from sleep. If the device is currently
     * asleep, wakes it up, otherwise does nothing. This is what happens when
     * the power key is pressed to turn on the screen.
     * 
     * @param ctx
     */
    public void wakeUp();

    public void lockOn();

    public void lockOff();

    public void lockBackScreen();

    public void unlockBackScreen();

}
