package com.yotadevices.sdk;

import com.yotadevices.sdk.Constants.SystemBSFlags;
import com.yotadevices.sdk.Constants.VolumeButtonsEvent;
import com.yotadevices.sdk.exception.SuperNotCalledException;
import com.yotadevices.sdk.helper.HelperConstant;
import com.yotadevices.sdk.utils.EinkUtils;

import android.app.Service;
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

public class BSActivity extends Service {

    private String TAG;
    private static final boolean DEBUG_BS_LIFECIRCLE = true;

    /** Standard activity result: operation canceled. */
    public static final int RESULT_CANCELED = 0;
    /** Standard activity result: operation succeeded. */
    public static final int RESULT_OK = -1;
    /** Start of user-defined activity results. */
    public static final int RESULT_FIRST_USER = 1;

    private final Object mLockActive = new Object();

    private Intent mIntent;
    private BSDrawer mDrawer;

    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    boolean mCalled;
    boolean isResumed;
    boolean isFinishing;
    boolean isBSLock;
    boolean dispatchOnHandleIntent;
    private int mSystemUiVisibility = SystemBSFlags.SYSTEM_BS_UI_FLAG_VISIBLE;

    private int mResultCode;
    private Intent mResultData;
    private int mRequestCode;

    /** Record inner state BSActivity */
    private BSRecord mRecord;

    private Handler mIncomingHandler;
    private final Handler h = new Handler(); // for UI thread actions
    private Drawer.Waveform mInitialWaveform = Drawer.Waveform.WAVEFORM_GC_FULL;
    private Drawer.Dithering mInitialDithering = Drawer.Dithering.BLACK_AND_WHITE_ONLY;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private Messenger mMessenger;
    /** Messenger for communicating with service. */
    Messenger mService = null;

    @Override
    public void onCreate() {
        super.onCreate();
        TAG = getClass().getSimpleName();

        mIncomingHandler = new BSAcivityIncomingMessagesHandler(this);
        mMessenger = new Messenger(mIncomingHandler);

        mDrawer = new BSDrawer(this);
        mRecord = new BSRecord(getApplicationContext(), TAG);
        isResumed = false;
        isFinishing = false;
        dispatchOnHandleIntent = false;
        isBSLock = false;
    }

    private void cleanResource() {
        mIncomingHandler = null;
        mMessenger = null;
        mDrawer = null;
    }

    synchronized void doBindService() {
        if (!mIsBound) {
            Log.d(TAG, "Start Binding.");
            mIsBound = bindService(getFrameworkIntent(), mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "Binding..." + mIsBound);
        }
    }

    synchronized void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
            Log.d(TAG, "Unbinding.");
        }
    }

    private Intent getFrameworkIntent() {
        return new Intent(HelperConstant.FRAMEWORK_SDK_ACTION);
    }

    @Override
    @Deprecated
    public final void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mIntent = intent;
        if (isResumed) {
            handleCommand();
        } else {
            doBindService();
        }
        return Service.START_NOT_STICKY;
    }

    protected void onStartCommand(Intent intent) {

    }

    private void handleCommand() {
        onStartCommand(mIntent);
        onHandleIntent(mIntent);
        sendRequest(InnerConstants.RequestFramework.REQUEST_SET_INTENT);
    }

    @Override
    public final IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        performFnishWithRequest(false);
        doUnbindService();
        performBSDestroy();
        cleanResource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isFinishing) {
            // user can stop bsActivity using stopService method
            performFnishWithRequest(false);
        }
        doUnbindService();
        performBSDestroy();
        cleanResource();
    }

    /**
     * Method is deprecated. Please to use {@link #onStartCommand}.
     * 
     * @param intent
     */
    @Deprecated
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
    }

    private void checkBSActivityRunning() {
        if (mDrawer == null) {
            throw new IllegalStateException("Is your BSActivity running?");
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

    final void performBSSaveInstanceState(BSRecord record) {
        onBSSaveInstanceState(record.getData());
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSSaveInstanceState " + TAG + " : " + record.getData());
        }
    }

    final void performBSRestoreInstanceState(BSRecord record) {
        onBSRestoreInstanceState(record.getData());
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSRestoreInstanceState " + TAG + " : " + record.getData());
        }
    }

    private final void performBSCreate() {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSCreate.");
        }
        mCalled = false;
        onBSCreate();
        if (!mCalled) {
            throw new SuperNotCalledException("BSActivity " + TAG + " did not call through to super.onBSCreate()");
        }
    }

    void performBSActivated(boolean isBsLock) {
        performBSActivated(isBsLock, -1, RESULT_CANCELED, null);
    }

    void performBSActivated(boolean isBsLock, int requestCode, int resultCode, Intent data) {
        if (!isFinishing) {
            restoreInstanceState();
            if (requestCode != -1) {
                performOnBSActivityResult(requestCode, resultCode, data);
            }
            performBSResume(isBsLock);
            handleCommand();
        }
    }

    /** Disactivate current BSActivity : pause() -> stop() -> destroy() */
    void performBSDisActivated() {
        setFinishing(true);
        performBSPause();
        performBSStop(true);
    }

    void performOnBSActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG_BS_LIFECIRCLE) {
            Log.v(TAG, "onBSActivityResult");
        }
        onBSActivityResult(requestCode, resultCode, data);
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
        mDrawer.updateViewLayout(mSystemUiVisibility);
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
     * onBSCreate - Called when BsDrawer is registered in the YotaPhoneitanium
     * Manager (PM). In this state BsDrawer gains privileges to draw on BS but
     * drawing is not permitted yet
     */
    protected void onBSCreate() {
        mCalled = true;
    }

    /**
     * onBSResume - Called when BsDrawer is ready to draw on BS.
     */
    protected void onBSResume() {
        checkBSActivityRunning();
        mDrawer.addBSParentView(mInitialWaveform, mInitialDithering);// show user UI on back screen
        isResumed = true;
        mCalled = true;
    }

    /**
     * onBSStop and onBSPause: Called when BsDrawer loses privileges to draw on
     * BS.
     */
    protected void onBSPause() {
        checkBSActivityRunning();
        mDrawer.removeBSParentView();
        isResumed = false;
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
     * getIntent - Return the intent that started this BSActivity.
     * 
     * @return Intent
     */
    public Intent getIntent() {
        return mIntent;
    }

    /**
     * getBSDrawer - Returns instance of BSDrawer that should be used to draw on
     * back screen.
     */
    public BSDrawer getBSDrawer() {
        return mDrawer;
    }

    public void setInitialWaveform(Drawer.Waveform initialWaveform) {
        mInitialWaveform = initialWaveform;
    }

    public void setInitialDithering(Drawer.Dithering initialDithering) {
        mInitialDithering = initialDithering;
    }

    /**
     * Convenience for calling {@link com.yotadevices.sdk.BSDrawer#addViewToBS}
     * .
     */
    public void setBSContentView(View view) {
        checkBSActivityRunning();
        mDrawer.getParentView().removeAllViews();
        mDrawer.addViewToBS(view);
    }

    /**
     * Convenience for calling {@link com.yotadevices.sdk.BSDrawer#addViewToBS}
     * .
     */
    public void setBSContentView(View view, LayoutParams params) {
        checkBSActivityRunning();
        mDrawer.getParentView().removeAllViews();
        mDrawer.addViewToBS(view, params);
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
        checkBSActivityRunning();
        mDrawer.getParentView().removeAllViews();
        if (mDrawer.getBSLayoutInflater() != null) {
            mDrawer.addViewToBS(getBSDrawer().getBSLayoutInflater().inflate(layoutResID, null));
        }
    }

    /**
     * Convenience for calling {@link com.yotadevices.sdk.BSDrawer#findViewById}
     * .
     */
    public View findViewById(int id) {
        checkBSActivityRunning();
        return mDrawer.findViewById(id);
    }

    /**
     * Return application context
     * 
     * @return getApplicationContext()
     */
    public Context getContext() {
        return getApplicationContext();
    }

    public void setSystemBSUiVisibility(int visibility) {
        if (visibility != mSystemUiVisibility) {
            mSystemUiVisibility = visibility;
            if (isResumed) {
                sendRequest(InnerConstants.RequestFramework.REQUEST_SET_SYSTEM_UI);
            }
        }
    }

    int getSytemBSUiVisibility() {
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
        if (!isFinishing) {
            performFnishWithRequest(true);
        }
    }

    /**
     * Call this to set the result that your bsactivity will return to its
     * caller.
     * 
     * @param resultCode
     *            The result code to propagate back to the originating activity,
     *            often RESULT_CANCELED or RESULT_OK
     */
    public void setResult(int resultCode) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = null;
        }
    }

    public void setResult(int resultCode, Intent data) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = data;
        }
    }

    /**
     * Same as {@link #startBSActivityForResult(Intent, int)} with no options
     * specified.
     * 
     * @param intent
     *            The intent to start.
     */

    public void startBSActivity(Intent intent) {
        startBSActivityForResult(intent, -1);
    }

    /**
     * Launch an activity for which you would like a result when it finished.
     * When this activity exits, your onBSActivityResult() method will be called
     * with the given requestCode. Using a negative requestCode is the same as
     * calling {@link #startBSActivity} (the activity is not launched as a
     * sub-activity).
     * 
     * @param intent
     *            The intent to start.
     * 
     * @param requestCode
     *            If >= 0, this code will be returned in onBSActivityResult()
     *            when the activity exits.
     */
    public void startBSActivityForResult(Intent intent, int requestCode) {
        checkBSActivityRunning();
        synchronized (this) {
            mRequestCode = requestCode;
        }
        sendRequest(InnerConstants.RequestFramework.REQUEST_SET_ACTIVITY_RESULT);
        startService(intent);
    }

    /**
     * Called when an bs-activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional data
     * from it. The <var>resultCode</var> will be {@link #RESULT_CANCELED} if
     * the activity explicitly returned that, didn't return any result, or
     * crashed during its operation.
     * 
     * <p>
     * You will receive this call immediately before onResume() when your
     * bs-activity is re-starting.
     * 
     * @param requestCode
     *            The integer request code originally supplied to
     *            startBSActivityForResult(), allowing you to identify who this
     *            result came from.
     * @param resultCode
     *            The integer result code returned by the child activity through
     *            its setResult().
     * @param data
     *            An Intent, which can return result data to the caller (various
     *            data can be attached to Intent "extras").
     * 
     * @see #startBSActivityForResult
     * @see #setResult(int)
     */
    protected void onBSActivityResult(int requestCode, int resultCode, Intent data) {

    }

    void performFnishWithRequest(boolean stopped) {
        sendRequest(InnerConstants.RequestFramework.REQUEST_SET_FINISH);
        performFnish(stopped);
    }

    void performFnish(boolean stopped) {
        isFinishing = true;
        if (isResumed) {
            performBSPause();
        }
        performBSStop(stopped);
    }

    void sendRequest(int what) {
        Bundle bundle = new Bundle();
        bundle.putString(InnerConstants.EXTRA_SERVICE_NAME, getClass().getName());
        bundle.putString(InnerConstants.EXTRA_PACKAGE_NAME, getPackageName());
        bundle.putInt(InnerConstants.EXTRA_SYSTEM_BS_UI_FLAG, mSystemUiVisibility);
        sendToFramework(what, bundle);
    }

    private void sendToFramework(int what, Bundle bundle) {
        if (mService == null) {
            return;
        }

        try {
            Message msg = Message.obtain(null, what);

            msg.arg1 = android.os.Process.myPid();
            msg.arg2 = android.os.Process.myUid();

            switch (what) {
            case InnerConstants.RequestFramework.REQUEST_SET_ACTIVE:
                msg.replyTo = mMessenger;
                break;
            case InnerConstants.RequestFramework.REQUEST_SET_INTENT:
                bundle.putParcelable(InnerConstants.EXTRA_BS_ACTIVITY_INTENT, getIntent());
                break;
            case InnerConstants.RequestFramework.REQUEST_SET_ACTIVITY_RESULT:
                int requestCode;
                synchronized (this) {
                    requestCode = mRequestCode;
                }
                bundle.putInt(InnerConstants.EXTRA_REQUEST_CODE, requestCode);
                break;
            case InnerConstants.RequestFramework.REQUEST_SET_FINISH:
                int resultCode;
                Intent resultData;
                synchronized (this) {
                    resultCode = mResultCode;
                    resultData = mResultData;
                }
                bundle.putInt(InnerConstants.EXTRA_RESULT_CODE, resultCode);
                if (resultData != null) {
                    bundle.putParcelable(InnerConstants.EXTRA_RESULT_DATA, resultData);
                }
                break;
            default:
                break;
            }

            msg.setData(bundle);
            mService.send(msg);
        } catch (Exception e) {
            Log.d(TAG, "Error while send msg", e);
            if (!isFinishing) {
                performFnish(true);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (mService == null) {
                Log.d(TAG, "Attached.");
                mService = new Messenger(service);
                performBSCreate();
                sendRequest(InnerConstants.RequestFramework.REQUEST_SET_ACTIVE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.d(TAG, "Disconnected.");
        }
    };

}
