package com.yotadevices.sdk.utils;

import com.yotadevices.sdk.helper.IFrameworkService;
import com.yotadevices.sdk.helper.ServiceBSHelper;
import com.yotadevices.sdk.helper.ServiceBSHelper.OnSuccesBinding;

import android.content.Context;
import android.os.RemoteException;

/**
 * @hide
 */
public class FrameworkUtils {

    private final static int IS_LOCK_SCREEN_DISABLED = 1;

    private static void executeCommand(Context ctx, final IPlatinumCallback callback, final int type) {
        final ServiceBSHelper h = new ServiceBSHelper(ctx);
        h.getAsyncService(new OnSuccesBinding() {
            @Override
            public void onError() {

            }

            @Override
            public void onBind(IFrameworkService service) {
                try {
                    switch (type) {
                    case IS_LOCK_SCREEN_DISABLED:
                        if (callback != null) {
                            callback.onLockScreenDisabled(service.isLockScreenDisabled());
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
    public static void isLockScreenDisabled(Context ctx, IPlatinumCallback callback) {
        executeCommand(ctx, callback, IS_LOCK_SCREEN_DISABLED);
    }

}
