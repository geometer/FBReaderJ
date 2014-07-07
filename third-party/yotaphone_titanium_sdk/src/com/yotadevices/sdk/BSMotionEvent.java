
package com.yotadevices.sdk;

import com.yotadevices.sdk.Constants.Gestures;

/**
 * Motion event that handles on BS.
 * @author asazonov
 *
 */
public class BSMotionEvent {
    private Gestures mAction;

    /**
     * @return action code (e.g. ACTION_SWIPE_LEFT)
     */
    public Gestures getBSAction() {
        return mAction;
    }

    /**
     * Set action to be translated into methods.
     * @param mAction gesture.
     */
    public void setBSAction(Gestures mAction) {
        this.mAction = mAction;
    }
}
