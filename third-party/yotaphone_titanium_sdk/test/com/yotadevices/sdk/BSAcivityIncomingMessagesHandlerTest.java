package com.yotadevices.sdk;

import static org.junit.Assert.*;

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
public class BSAcivityIncomingMessagesHandlerTest {

    @Test
    public void msgPause() throws Exception {
		BSActivity activity = Mockito.mock(BSActivity.class);
		
		BSAcivityIncomingMessagesHandler handler = new BSAcivityIncomingMessagesHandler(activity);
		Message message = Message.obtain(null, Constants.MESSAGE_BS_PAUSE, 0, 0);
		handler.handleMessage(message);
		
		Mockito.verify(activity, Mockito.atLeastOnce()).performBSPause();
    }
    
    @Test
    public void msgResume() throws Exception {
		BSActivity activity = Mockito.mock(BSActivity.class);
		
		BSAcivityIncomingMessagesHandler handler = new BSAcivityIncomingMessagesHandler(activity);
		Message message = Message.obtain(null, Constants.MESSAGE_ACTIVATED, 0, 0);
		handler.handleMessage(message);
		
		Mockito.verify(activity, Mockito.atLeastOnce()).performBSResume();
    }
    
    @Test
    public void msgStop() throws Exception {
		BSActivity activity = Mockito.mock(BSActivity.class);
		
		BSAcivityIncomingMessagesHandler handler = new BSAcivityIncomingMessagesHandler(activity);
		Message message = Message.obtain(null, Constants.MESSAGE_DISACTIVATED, 0, 0);
		handler.handleMessage(message);
		
		Mockito.verify(activity, Mockito.atLeastOnce()).performBSStop();
    }
    
    @Test
    public void msgMotionEvent() throws Exception {
		BSActivity activity = Mockito.mock(BSActivity.class);
		
		BSAcivityIncomingMessagesHandler handler = new BSAcivityIncomingMessagesHandler(activity);
		Message message = Message.obtain(null, Constants.MESSAGE_MOTION_EVENT, 0, 0);
		handler.handleMessage(message);
		
		Mockito.verify(activity, Mockito.atLeastOnce()).performBSTouchEvent(Mockito.any(BSMotionEvent.class));
    }

}
