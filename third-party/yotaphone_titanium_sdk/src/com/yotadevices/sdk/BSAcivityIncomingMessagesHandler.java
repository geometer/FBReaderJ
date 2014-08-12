package com.yotadevices.sdk;

import com.yotadevices.sdk.Constants.VolumeButtonsEvent;
import com.yotadevices.sdk.InnerConstants.AnswerFramework;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Handler of incoming messages from service.
 * 
 * @hide
 */
class BSAcivityIncomingMessagesHandler extends Handler {

    private WeakReference<BSActivity> mBSActivity;

    public BSAcivityIncomingMessagesHandler(BSActivity bsActivity) {
        mBSActivity = new WeakReference<BSActivity>(bsActivity);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mBSActivity.get() == null)
            throw new IllegalStateException("BSActivity that received message is null");
        Log.d(mBSActivity.get().getClass().getSimpleName(), "Received from service: " + msg.what);

        switch (msg.what) {
        case AnswerFramework.MESSAGE_ACTIVATED:
            int requestCode = msg.arg2;
            if (requestCode != -1) {
                // TODO: unlocked!!!
                mBSActivity.get().performBSActivated(false, requestCode, msg.arg1, (Intent) msg.obj);
            } else {
                mBSActivity.get().performBSActivated(msg.arg1 == 1); // isBSLock
            }
            break;
        case AnswerFramework.MESSAGE_DISACTIVATED:
            mBSActivity.get().performBSDisActivated();
            break;
        case AnswerFramework.MESSAGE_BS_PAUSE:
            mBSActivity.get().performBSPause();
            break;
        case AnswerFramework.MESSAGE_BS_RESUME:
            mBSActivity.get().performBSResume(msg.arg1 == 1);
            break;
        case AnswerFramework.MESSAGE_BS_LOCK:
            mBSActivity.get().performBSLock();
            break;
        case AnswerFramework.MESSAGE_BS_UNLOCK:
            mBSActivity.get().performBSUnlock();
            break;
        case AnswerFramework.MESSAGE_VOLUME_BUTTONS_EVENT:
            mBSActivity.get().performVolumeButtonsEvent(VolumeButtonsEvent.valueOf(msg.arg1));
            break;
        case AnswerFramework.MESSAGE_SYSTEM_UI_CHANGE:
            mBSActivity.get().performSystemUIChange();
            break;
        default:
            super.handleMessage(msg);
        }
    }
}
