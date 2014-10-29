package com.yotadevices.sdk;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.os.Parcel;

import com.yotadevices.sdk.BSMotionEvent;
import com.yotadevices.sdk.Constants.Gestures;
import com.yotadevices.sdk.notifications.BSNotification;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class BSNotificationTest {

	@Test
	public void testCreate() {
		BSNotification notification = new BSNotification();
		
		assertEquals(0, notification.describeContents());
	}
	
	@Test
	public void testCreateByBuilder() {
		BSNotification.Builder b = new BSNotification.Builder();
		BSNotification n = b.setNotificationType(3).build();
		
		assertEquals(3, n.BSNotificationType);
	}

	@Test
	public void testCreateFromParcel() {
		Parcel p = Mockito.mock(Parcel.class);

		BSNotification n = new BSNotification();
		n.BSNotificationType = 341;
		
		n.writeToParcel(p, 0);
		
		Mockito.verify(p).writeInt(341);
	}
}
