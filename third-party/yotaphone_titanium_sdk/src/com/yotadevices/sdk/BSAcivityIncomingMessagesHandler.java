package com.yotadevices.sdk;

import com.yotadevices.sdk.Constants.VolumeButtonsEvent;
import com.yotadevices.sdk.InnerConstants.AnswerFramework;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Handler of incoming messages from service.
 * 
 * @hide
 */
class BSAcivityIncomingMessagesHandler extends Handler {

    private BSActivity mBSActivity;

    public BSAcivityIncomingMessagesHandler(BSActivity bsActivity) {
        mBSActivity = bsActivity;
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(BSActivity.TAG, "Received from service: " + msg.what);
        switch (msg.what) {
        case AnswerFramework.MESSAGE_ACTIVATED:
            mBSActivity.performBSActivated(msg.arg1 == 1); // isBSLock
            break;
        case AnswerFramework.MESSAGE_DISACTIVATED:
            mBSActivity.performBSDisActivated();
            break;
        case AnswerFramework.MESSAGE_BS_PAUSE:
            mBSActivity.performBSPause();
            break;
        case AnswerFramework.MESSAGE_BS_RESUME:
            mBSActivity.performBSResume(msg.arg1 == 1);
            break;
        case AnswerFramework.MESSAGE_BS_LOCK:
            mBSActivity.performBSLock();
            break;
        case AnswerFramework.MESSAGE_BS_UNLOCK:
            mBSActivity.performBSUnlock();
            break;
        case AnswerFramework.MESSAGE_VOLUME_BUTTONS_EVENT:
            mBSActivity.performVolumeButtonsEvent(VolumeButtonsEvent.valueOf(msg.arg1));
            break;
        case AnswerFramework.MESSAGE_SYSTEM_UI_CHANGE:
            mBSActivity.performSystemUIChange();
            break;
        default:
            super.handleMessage(msg);
        }
    }
}
