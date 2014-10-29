package com.yotadevices.sdk.utils;

import com.yotadevices.sdk.helper.IFrameworkService;
import com.yotadevices.sdk.helper.ServiceBSHelper;
import com.yotadevices.sdk.helper.ServiceBSHelper.OnSuccesBinding;

import android.content.Context;
import android.os.RemoteException;

/**
 * @hide
 */
public class PowerUtils {

    private final static String TAG = PowerUtils.class.getSimpleName();

    private final static int GOTO_SLEEP = 0;
    private final static int WAKE_UP = 1;
    private final static int LOCK_ON = 2;
    private final static int LOCK_OFF = 3;
    private final static int LOCK_BS = 4;
    private final static int UNLOCK_BS = 5;

    private static void executeCommand(Context ctx, final int type) {
        final ServiceBSHelper h = new ServiceBSHelper(ctx);
        h.getAsyncService(new OnSuccesBinding() {
            @Override
            public void onError() {

            }

            @Override
            public void onBind(IFrameworkService service) {
                try {
                    switch (type) {
                    case GOTO_SLEEP:
                        service.goToSleep();
                        break;
                    case WAKE_UP:
                        service.wakeUp();
                        break;
                    case LOCK_ON:
                        service.lockOn();
                        break;
                    case LOCK_OFF:
                        service.lockOff();
                        break;
                    case LOCK_BS:
                        service.lockBackScreen();
                        break;
                    case UNLOCK_BS:
                        service.unlockBackScreen();
                        break;
                    default:
                        break;
                    }
                } catch (RemoteException unused) {
                } finally {
                    h.unbind();
                }
            }
        });
    }

    /**
     * Forces the device to go to sleep. Overrides all the wake locks that are held. This is what happens when the power
     * key is pressed to turn off the screen.
     */
    public static void goToSleep(Context ctx) {
        executeCommand(ctx, GOTO_SLEEP);
    }

    /**
     * Forces the device to wake up from sleep. If the device is currently asleep, wakes it up, otherwise does nothing.
     * This is what happens when the power key is pressed to turn on the screen.
     * 
     * @param ctx
     */
    public static void wakeUp(Context ctx) {
        executeCommand(ctx, WAKE_UP);
    }

    public static void lockOn(Context ctx) {
        executeCommand(ctx, LOCK_ON);
    }

    public static void lockOff(Context ctx) {
        executeCommand(ctx, LOCK_OFF);
    }

    public static void lockBackScreen(Context ctx) {
        executeCommand(ctx, LOCK_BS);
    }

    public static void unlockBackScreen(Context ctx) {
        executeCommand(ctx, UNLOCK_BS);
    }

}
