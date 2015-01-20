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
	private final static int COMMAND_IS_MIRRORING_ON = 4;

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
	                case COMMAND_IS_MIRRORING_ON:
		                IMirroringCallback callback = (IMirroringCallback)msg.obj;
		                if (callback != null) {
			                callback.onMirroringStatusChanged(service.isMirroringOn());
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

	public static void isMirroringEnabled(Context ctx, IMirroringCallback callback) {
		executeCommand(ctx, Message.obtain(null, COMMAND_IS_MIRRORING_ON, callback));
	}
}
