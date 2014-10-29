package com.yotadevices.sdk;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import android.content.Context;
import android.os.Parcel;
import android.os.RemoteException;

import com.yotadevices.sdk.BSMotionEvent;
import com.yotadevices.sdk.Constants.Gestures;
import com.yotadevices.sdk.notifications.BSNotification;
import com.yotadevices.sdk.notifications.BSNotificationManager;
import com.yotadevices.sdk.notifications.IBSNotification;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class BSNotificationManagerTest {

	private static class BSNMForTest extends BSNotificationManager {

		public BSNMForTest(Context context) {
	        super(context);
        }
		
		public void setMockService(IBSNotification service) {
			mBSNotificationService = service;
		}
	}
	
	@Test
	public void testCreate() throws RemoteException {
		BSNMForTest manager = new BSNMForTest(Robolectric.getShadowApplication().getApplicationContext());
		IBSNotification service = Mockito.mock(IBSNotification.class);
		
		manager.setMockService(service);
		
		BSNotification notification = new BSNotification();
		
		manager.notify(notification);
		
		Mockito.verify(service, Mockito.only()).drawNotification(notification);
		assertEquals(0, notification.describeContents());
	}
}
