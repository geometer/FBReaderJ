package com.yotadevices.sdk.utils;

import com.yotadevices.sdk.Drawer.Waveform;
import com.yotadevices.sdk.helper.IFrameworkService;
import com.yotadevices.sdk.helper.ServiceBSHelper;
import com.yotadevices.sdk.helper.ServiceBSHelper.OnSuccesBinding;

import android.content.Context;
import android.os.Message;
import android.os.RemoteException;

/**
 * @hide
 */
public class FrameworkUtils {

    private final static int COMMAND_IS_LOCK_SCREEN_DISABLED = 1;
    private final static int COMMAND_PERFORM_SINGLE_UPDATE = 2;
    private final static int COMMAND_GET_TOP_BSACTIVITY = 3;

    private static void executeCommand(Context ctx, final Message msg) {
        final ServiceBSHelper h = new ServiceBSHelper(ctx);
        h.getAsyncService(new OnSuccesBinding() {
            @Override
            public void onError() {

            }

            @Override
            public void onBind(IFrameworkService service) {
                try {
                    switch (msg.what) {
                    case COMMAND_IS_LOCK_SCREEN_DISABLED:
                        ILockCallback lockCallback = (ILockCallback) msg.obj;
                        if (lockCallback != null) {
                            lockCallback.onLockScreenDisabled(service.isLockScreenDisabled());
                        }
                        break;
                    case COMMAND_PERFORM_SINGLE_UPDATE:
                        service.performSingleUpdate(msg.arg1);
                        break;
                    case COMMAND_GET_TOP_BSACTIVITY:
                        IBSActivityCallback bsActivityCallback = (IBSActivityCallback) msg.obj;
                        if (bsActivityCallback != null) {
                            bsActivityCallback.onTopBSActivityReturned(service.getTopBSActivity());
                        }
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
     * Return "true" if lock screen is "None"
     */
    public static void isLockScreenDisabled(Context ctx, ILockCallback callback) {
        executeCommand(ctx, Message.obtain(null, COMMAND_IS_LOCK_SCREEN_DISABLED, callback));
    }

    public static void performSingleUpdate(Context ctx, Waveform waveform) {
        executeCommand(ctx, Message.obtain(null, COMMAND_PERFORM_SINGLE_UPDATE, waveform.ordinal(), -1));
    }

    public static void getTopBSActivity(Context ctx, IBSActivityCallback callback) {
        executeCommand(ctx, Message.obtain(null, COMMAND_GET_TOP_BSACTIVITY, callback));
    }

}
