package com.yotadevices.sdk;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import android.os.Bundle;
import android.os.Message;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.sdk.BSMotionEvent;
import com.yotadevices.sdk.Constants;
import com.yotadevices.sdk.Constants.Gestures;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class BSActivityTest {

    @Test
    public void testFullScreenMode() throws Exception {
		BSActivity activity = new BSActivity() {
			{
				Assert.assertFalse(getFullScreenMode());
				setFullScreenMode(true);
				Assert.assertTrue(getFullScreenMode());
			}
		};
    }
}
