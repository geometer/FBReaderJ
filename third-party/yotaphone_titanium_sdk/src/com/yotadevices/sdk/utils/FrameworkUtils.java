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
                        IPlatinumCallback callback = (IPlatinumCallback) msg.obj;
                        if (callback != null) {
                            callback.onLockScreenDisabled(service.isLockScreenDisabled());
                        }
                        break;
                    case COMMAND_PERFORM_SINGLE_UPDATE:
                        service.performSingleUpdate(msg.arg1);
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
    public static void isLockScreenDisabled(Context ctx, IPlatinumCallback callback) {
        executeCommand(ctx, Message.obtain(null, COMMAND_IS_LOCK_SCREEN_DISABLED, callback));
    }

    public static void performSingleUpdate(Context ctx, Waveform waveform) {
        executeCommand(ctx, Message.obtain(null, COMMAND_PERFORM_SINGLE_UPDATE, waveform.getInternalValue(), -1));
    }

}
