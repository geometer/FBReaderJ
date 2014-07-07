package com.yotadevices.sdk;

import static org.junit.Assert.*;

import org.junit.Test;

import com.yotadevices.sdk.BSMotionEvent;
import com.yotadevices.sdk.Constants.Gestures;


public class BSMotionEventTest {

	@Test
	public void testGetSetBSAction() {
		BSMotionEvent event = new BSMotionEvent();
		
		event.setBSAction(Gestures.GESTURES_BS_RL);
		assertEquals(Gestures.GESTURES_BS_RL, event.getBSAction());
		
		event.setBSAction(Gestures.GESTURES_BS_DOUBLE_TAP);
		assertEquals(Gestures.GESTURES_BS_DOUBLE_TAP, event.getBSAction());

		event.setBSAction(Gestures.GESTURES_BS_LONG_PRESS);
		assertEquals(Gestures.GESTURES_BS_LONG_PRESS, event.getBSAction());
	}
}
