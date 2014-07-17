package com.yotadevices.sdk;

import com.yotadevices.sdk.Constants.SystemBSFlags;
import com.yotadevices.sdk.Constants.VolumeButtonsEvent;
import com.yotadevices.sdk.exception.SuperNotCalledException;
import com.yotadevices.sdk.helper.HelperConstant;
import com.yotadevices.sdk.utils.InfiniteIntentService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * The service which handles events to work with e-ink screen.
 */
public abstract class BSActivity extends InfiniteIntentService {

    /**
     * @hide
     */
    public static String TAG = "BSActivity";
    private static final boolean DEBUG_BS_LIFECIRCLE = true;

    private final Object mLockActive = new Object();
    private BSDrawer mDrawer;

    private Intent mStartIntent = null;

    /** Messenger for communicating with service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    boolean mCalled;
    boolean isResumed;
    boolean isFinishing;
    boolean isBSLock;
    boolean dispatchOnHandleIntent;

    /** Record inner state BSActivity */
    private BSRecord mRecord;

    private final Handler mIncomingHandler = new BSAcivityIncomingMessagesHandler(this);
    private final Handler h = new Handler(); // for UI thread actions

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(mIncomingHandler);

    private int mSystemUiVisibility = SystemBSFlags.SYSTEM_BS_UI_FLAG_VISIBLE;

    public BSActivity() {
        super(TAG);
    }

    /**
     * @hide
     */
    @Override
    final public void onCreate() {
        super.onCreate();
        TAG = getClass().getSimpleName();
        mDrawer = new BSDrawer(this);
        mRecord = new BSRecord(this);
        isResumed = false;
        isFinishing = false;
        dispatchOnHandleIntent = false;
        isBSLock = false;

        performBSCreate();
        doBindService();
    };

    /**
     * @hide
     */
    @Override
    final public void onDestroy() {
        super.onDestroy();
        if (!isFinishing) {
            // user can stop bsActivity using stopService method
            performFnish(false);
        }

        doUnbindService();
        performBSDestroy();
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        performFnish(false);
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d(TAG, "Attached.");
            sendRequest(InnerConstants.RequestFramework.REQUEST_SET_ACTIVE);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "Disconnected.");
        }
    };

    private Intent getFrameworkIntent() {
        return new Intent(HelperConstant.FRAMEWORK_SDK_ACTION);
    }

    void doBindService() {
        Log.d(TAG, "Start Binding.");
        mIsBound = bindService(getFrameworkIntent(), mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Binding..." + mIsBound);
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            Log.d(TAG, "Unbinding.");
        }
    }

    /**
     * @hide
     */
    @Override
    final public void onStart(Intent intent, int startId) {
        mStartIntent = intent;
        super.onStart(intent, startId);
    }

    /**
     * @hide
     */
    @Override
    final protected boolean canHandleIntent() {
        synchronized (mLockActive) {
            if (!isResumed) {
                if (isFinishing()) {
                    stopSelf();
                } else {
                    dispatchOnHandleIntent = true;
                }
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendRequest(InnerConstants.RequestFramework.REQUEST_SET_INTENT);
        Log.d(TAG, "onHandleIntent");
    }

    private final void performBSCreate() {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSCreate.");
        }
        onBSCreate();
    }

    void performBSStop(boolean stopped) {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSStop.");
        }

        saveInstanceState();

        mCalled = false;
        onBSStop();
        if (!mCalled) {
            throw new SuperNotCalledException("BSActivity " + getClass().getSimpleName() + " did not call through to super.onBSStop()");
        }

        // self
        if (stopped) {
            stopSelf();
        }
    }

    /**
     * save inner state.
     */
    private void saveInstanceState() {
        performBSSaveInstanceState(mRecord);
        mRecord.saveState();
    }

    private void restoreInstanceState() {
        mRecord.restoreState();
        performBSRestoreInstanceState(mRecord);
    }

    void performBSActivated(boolean isBSLocked) {
        restoreInstanceState();
        performBSResume(isBSLocked);

        // ready for onHandleIntent()
        synchronized (mLockActive) {
            if (dispatchOnHandleIntent) {
                onHandleIntent(getIntent());
                dispatchOnHandleIntent = false;
            }
        }
    }

    /** Disactivate current BSActivity : pause() -> stop() -> destroy() */
    void performBSDisActivated() {
        setFinishing(true);
        performBSPause();
        performBSStop(true);
    }

    void performBSResume(boolean isBSLocked) {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSResume.");
        }

        isBSLock = isBSLocked;
        mCalled = false;
        onBSResume();
        if (!mCalled) {
            throw new SuperNotCalledException("BSActivity " + getClass().getSimpleName() + " did not call through to super.onBSResume()");
        }
    }

    private final void performBSDestroy() {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSDestroy.");
        }

        mCalled = false;
        onBSDestroy();
        if (!mCalled) {
            throw new SuperNotCalledException("BSActivity " + getClass().getSimpleName() + " did not call through to super.onBSDestroy()");
        }
    }

    final void performBSSaveInstanceState(BSRecord record) {
        onBSSaveInstanceState(record.getData());
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSSaveInstanceState " + this + " : " + record.getData());
        }
    }

    final void performBSRestoreInstanceState(BSRecord record) {
        onBSRestoreInstanceState(record.getData());
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSRestoreInstanceState " + this + " : " + record.getData());
        }
    }

    void performBSPause() {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSPause.");
        }

        mCalled = false;
        onBSPause();
        if (!mCalled) {
            throw new SuperNotCalledException("BSActivity " + getClass().getSimpleName() + " did not call through to super.onBSPause()");
        }
    }

    void performBSLock() {
        isBSLock = true;
        onBSLock();
    }

    void performBSUnlock() {
        isBSLock = false;
        onBSUnlock();
    }

    final void performVolumeButtonsEvent(VolumeButtonsEvent event) {
        onVolumeButtonsEvent(event);
    }

    final void performSystemUIChange() {
        getBSDrawer().updateViewLayout(mSystemUiVisibility);
    }

    /**
     * onBSCreate - Called when BsDrawer is registered in the Platinum Manager
     * (PM). In this state BsDrawer gains privileges to draw on BS but drawing
     * is not permitted yet
     */
    protected void onBSCreate() {

    }

    /**
     * onBSResume - Called when BsDrawer is ready to draw on BS.
     */
    protected void onBSResume() {
        getBSDrawer().addBSParentView();// show user UI on back screen
        isResumed = true;
        mCalled = true;
    }

    /**
     * onBSStop and onBSPause: Called when BsDrawer loses privileges to draw on
     * BS.
     */
    protected void onBSStop() {
        isFinishing = true;
        mCalled = true;
    }

    /**
     * onBSStop and onBSPause: Called when BsDrawer loses privileges to draw on
     * BS.
     */
    protected void onBSPause() {
        getBSDrawer().removeBSParentView();
        isResumed = false;
        mCalled = true;
    }

    /**
     * @hide
     */
    @Override
    public final IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    /**
     * onBSDestroy - Called when BsDrawer is unregistered from PM.
     */
    protected void onBSDestroy() {
        mCalled = true;
    }

    /**
     * onBSLock - Called if back screen is locked. In this case BSActivity
     * looses input from user.
     */
    protected void onBSLock() {

    }

    /**
     * onBSUnlock - Called if back screen is unlocked and application received
     * the controls back.
     */
    protected void onBSUnlock() {

    }

    /**
     * onVolumeButtonsEvent - Called when Volume button event occurs.
     * 
     * @param event
     *            Volume button event.
     */
    protected void onVolumeButtonsEvent(VolumeButtonsEvent event) {

    }

    /**
     * isBackScreenLocked - to be used to determine whether back screen is
     * locked
     */
    public boolean isBackScreenLocked() {
        return isBSLock;
    }

    /**
     * onBSSaveInstanceState - Save state before the instance is killed
     * 
     * @param outState
     *            instance state.
     */
    protected void onBSSaveInstanceState(Bundle outState) {

    }

    /**
     * onBSRestoreInstanceState - Restores the state
     * 
     * @param savedInstanceState
     *            saved instance state.
     */
    protected void onBSRestoreInstanceState(Bundle savedInstanceState) {

    }

    /**
     * getIntent - Return the intent that started this BSActivity.
     * 
     * @return Intent
     */
    public Intent getIntent() {
        return mStartIntent;
    }

    /**
     * getBSDrawer - Returns instance of BSDrawer that should be used to draw on
     * back screen.
     */
    public BSDrawer getBSDrawer() {
        return mDrawer;
    }

    /**
     * Convenience for calling {@link com.yotadevices.sdk.BSDrawer#addViewToBS}
     * .
     */
    public void setBSContentView(View view) {
        getBSDrawer().addViewToBS(view);
    }

    /**
     * Convenience for calling {@link com.yotadevices.sdk.BSDrawer#addViewToBS}
     * .
     */
    public void setBSContentView(View view, LayoutParams params) {
        getBSDrawer().addViewToBS(view, params);
    }

    /**
     * Set the back screen activity content from a layout resource. The resource
     * will be inflated, adding all top-level views to the back screen activity.
     * 
     * @param layoutResID
     *            Resource ID to be inflated.
     * @see #setBSContentView(android.view.View)
     * @see #setBSContentView(android.view.View,
     *      android.view.ViewGroup.LayoutParams)
     */
    public void setBSContentView(int layoutResID) {
        if (getBSDrawer().getBSLayoutInflater() != null) {
            getBSDrawer().addViewToBS(getBSDrawer().getBSLayoutInflater().inflate(layoutResID, null));
        }
    }

    /**
     * Convenience for calling {@link com.yotadevices.sdk.BSDrawer#findViewById}
     * .
     */
    public View findViewById(int id) {
        return getBSDrawer().findViewById(id);
    }

    /**
     * Return application context
     * 
     * @return getApplicationContext()
     */
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public Context getBaseContext() {
        return getBSDrawer().getBSContext();
    }

    public void setSystemBSUiVisibility(int visibility) {
        if (visibility != mSystemUiVisibility) {
            mSystemUiVisibility = visibility;
            if (isResumed) {
                sendRequest(InnerConstants.RequestFramework.REQUEST_SET_SYSTEM_UI);
            }
        }
    }

    int getSsytemBSUiVisibility() {
        return mSystemUiVisibility;
    }

    /**
     * Runs the specified action on the UI thread. If the current thread is the
     * UI thread, then the action is executed immediately. If the current thread
     * is not the UI thread, the action is posted to the event queue of the UI
     * thread. Parameters: action the action to run on the UI thread
     * 
     * @param action
     */
    public void runOnUiThread(Runnable action) {
        h.post(action);
    }

    /**
     * Check to see whether this BSActivity is in the process of finishing,
     * either because you called finish() on it or someone else has requested
     * that it finished. This is often used in onBSPause() to determine whether
     * the BSActivity is simply pausing or completely finishing.
     * 
     * @return If the BSActivity is finishing, returns true; else returns false.
     */
    public boolean isFinishing() {
        return isFinishing;
    }

    void setFinishing(boolean finish) {
        isFinishing = finish;
    }

    /**
     * Call this when your BSActivity is done and should be closed
     */
    public void finish() {
        performFnish(true);
    }

    void performFnish(boolean stopped) {
        sendRequest(InnerConstants.RequestFramework.REQUEST_SET_FINISH);
        isFinishing = true;
        if (isResumed) {
            performBSPause();
        }
        performBSStop(stopped);
    }

    void sendRequest(int what) {
        Bundle bundle = new Bundle();
        bundle.putString(InnerConstants.EXTRA_SERVICE_NAME, BSActivity.this.getClass().getName());
        bundle.putInt(InnerConstants.EXTRA_SYSTEM_BS_UI_FLAG, mSystemUiVisibility);
        bundle.putParcelable(InnerConstants.EXTRA_BS_ACTIVITY_INTENT, getIntent());
        sendToPlatinum(what, bundle);
    }

    private void sendToPlatinum(int what, Bundle bundle) {
        try {
            Message msg = Message.obtain(null, what);

            msg.arg1 = android.os.Process.myPid();
            msg.arg2 = android.os.Process.myUid();

            msg.setData(bundle);
            msg.replyTo = mMessenger;
            mService.send(msg);
        } catch (Exception e) {
            Log.e(TAG, "Error while send msg", e);
            if (what != InnerConstants.RequestFramework.REQUEST_SET_ACTIVE) {
                performBSPause();
            }
            performBSStop(true);
        }
    }

}
