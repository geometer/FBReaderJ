package com.yotadevices.sdk;

import java.io.FileDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.IBinder.DeathRecipient;

import static org.robolectric.Robolectric.clickOn;
import static org.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.PowerMockUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.yotadevices.sdk.Constants;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class BSRecordTest {

	TestBSActivity bsActivity;
	
	private static class TestBSActivity extends BSActivity {
		
		public TestBSActivity() {
			
        }
		
		@Override
		void sendRequestToBeActive() {
		    // do nothing.
		}
		
		public Handler getIncomingHandler() {
			return mIncomingHandler;
		}
	}

	@Before
	public void setUp() {
		Robolectric.getShadowApplication().setComponentNameAndServiceForBindService(
		        new ComponentName("com.yotadevices.framework", "com.yotadevices.framework.service.PlatinumManagerService"), 
		        null);

		bsActivity = new TestBSActivity();
		bsActivity.onCreate();
	}

	// @Test
	// public void testThatAppTrackerInfoServiceIsKickedOff() {
	//
	// // applicationContext.startService((new Intent(applicationContext, AppInfoTracker.class)));
	// //
	// // ShadowApplication shadowApplication = (ShadowApplication) Robolectric.shadowOf(testApplication);
	// //
	// // String serviceName = shadowApplication.peekNextStartedService().getComponent().getClassName();
	// // Assert.assertEquals("com.appy.services.AppInfoTracker", serviceName);
	// }

	@Test
	public void testSaveEmptyState() {
		//bsActivity.getIncomingHandler().handleMessage(null);
	}
	
	@Test
	public void testMock() {
		BSActivity a = Mockito.mock(BSActivity.class);
		//Mockito.when(a.performBSResume(a));
		
		BSAcivityIncomingMessagesHandler h = new BSAcivityIncomingMessagesHandler(a);
		Message m = Message.obtain(null, Constants.MESSAGE_ACTIVATED, 0, 0);
		h.handleMessage(m);
		
		Mockito.verify(a, Mockito.times(1)).performBSResume();
	}
}
