package com.yotadevices.sdk;

import android.os.Bundle;

//for future
/**
 * @hide
 */
public class BSInstrumentation {

    public void callOnBSSaveInstanceState(BSActivity activity, Bundle outState) {
        throw new RuntimeException("Not implemented");
    }

    public void callOnBSRestoreInstanceState(BSActivity activity, Bundle saveInstanceState) {
        throw new RuntimeException("Not implemented");
    }

}
