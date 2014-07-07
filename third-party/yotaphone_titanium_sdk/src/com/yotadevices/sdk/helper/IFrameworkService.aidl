package com.yotadevices.sdk.helper;


interface IFrameworkService {

	void goToSleep();
    
    void wakeUp();
    
    void lockOn();
    
    void lockOff();
    
    void lockBackScreen(); 
    
    void unlockBackScreen();
    
    boolean isLockScreenDisabled();
}