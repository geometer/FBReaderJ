package com.yotadevices.sdk.utils;

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
