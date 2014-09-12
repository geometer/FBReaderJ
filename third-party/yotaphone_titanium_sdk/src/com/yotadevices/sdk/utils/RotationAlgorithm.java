package com.yotadevices.sdk.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.yotadevices.platinum.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

/**
 * RotationAlgorithm - This class is used to catch events when user rotates the phone to switch between front and back screen.
 * Refer to <a href="http://developer.yotaphone.com/docs/getting-started/rotation-algorithm/">Rotation Algorithm</a> for more details.
 */
public class RotationAlgorithm implements SensorEventListener {
    private static final String TAG = "RotationAlgorithm";

    /**
     * OnPhoneRotatedListener - This listener can tell when device is rotated on certain side. Can be
     * used in {@link RotationAlgorithm#turnScreenOffIfRotated}
     */
    public interface OnPhoneRotatedListener {
        /**
         * onPhoneRotatedToFS - Called when device is rotated on front screen
         */
        public void onPhoneRotatedToFS();

        /**
         * onPhoneRotatedToBS - Called when device is rotated on back screen
         */
        public void onPhoneRotatedToBS();

        /**
         * onRotataionCancelled - Called if user didin't do rotation
         */
        public void onRotataionCancelled();
    }

    /**
     * OPTION_START_WITH_BS = 2: Set this flag if rotation algorithm should start with back screen.
     */
    public static final int OPTION_START_WITH_BS = 2;
    /**
     * OPTION_POWER_ON = 4: Set this flag if front screen should be turned on if rotation starts with back screen.
     */
    public static final int OPTION_POWER_ON = 4;
    /**
     * OPTION_NO_UNLOCK = 8: Set this flag if unlock of the back screen is not needed after the rotation.
     */
    public static final int OPTION_NO_UNLOCK = 8;
    /**
     * OPTION_DONT_MONITOR_BACK_ROTATION = 16: Set this flag if monitoring rotation back, after first rotation is happened is not needed.
     */
    public static final int OPTION_DONT_MONITOR_BACK_ROTATION = 16;
    /**
     * OPTION_EXPECT_FIRST_ROTATION_FOR_60SEC = 32: Set this rotation if application needs to wait for 60 seconds for rotation instead of 6.
     */
    public static final int OPTION_EXPECT_FIRST_ROTATION_FOR_60SEC = 32;

    private static final int TIME_60SEC = 60 * 1000;
    private static final int TIME_6SEC = 6 * 1000;

    private Context mContext;
    private static RotationAlgorithm mInstance;
    private OnPhoneRotatedListener mListener = null;

    /**
     * @param context input context
     * @return Instance of RotationAlgorithm
     */
    public static RotationAlgorithm getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RotationAlgorithm(context);
        } else {
            mInstance.mStartWithFS = true;
            mInstance.mPowerOn = false;
            mInstance.mNoUnlock = false;
            mInstance.setContext(context);
        }
        return mInstance;
    }

    private class SensorAttributes {
        public float x;
        public float y;
        public float z;
    }

    private class AvgGyroscope {
        float xAvg = 0f;
        float yAvg = 0f;
        float zAvg = 0f;
    }

    private final static class MyPowerUtils implements IPowerCallback {

        private static MyPowerUtils sInstance;
        private Context ctx;

        public static MyPowerUtils getInstance(Context ctx) {
            if (sInstance == null) {
                sInstance = new MyPowerUtils(ctx);
            }
            return sInstance;
        }

        public void setContext(Context ctx) {
            this.ctx = ctx;
        }

        private MyPowerUtils(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public void goToSleep() {
            PowerUtils.goToSleep(ctx);
        }

        @Override
        public void wakeUp() {
            PowerUtils.wakeUp(ctx);
        }

        @Override
        public void lockOn() {
            PowerUtils.lockOn(ctx);
        }

        @Override
        public void lockOff() {
            PowerUtils.lockOff(ctx);
        }

        @Override
        public void lockBackScreen() {
            PowerUtils.lockBackScreen(ctx);
        }

        @Override
        public void unlockBackScreen() {
            PowerUtils.unlockBackScreen(ctx);
        }
    }

    private boolean mRotationHappened = false;
    private boolean mFirstStep = true;
    private boolean mUserIsLying = false;

    private long p2bClickedTime;

    private LinkedList<SensorAttributes> mGyroscopeArray = new LinkedList<SensorAttributes>();

    private final static int TIME_DELAY = 50; // time is in milliseconds
    private final int MAX_SIZE = 1000 / TIME_DELAY;

    private boolean mStartWithFS = true;
    private boolean mPowerOn = false;
    private boolean mDeviceLockSettingIsNone = false;
    private boolean mNoUnlock = false;
    private boolean mExpectFirstRotationFor60Sec = false;

    private SensorManager mSensorManager;
    private KeyguardManager mKeyguardManager;

    private IPowerCallback mUtils;

    private RotationAlgorithm(Context context) {
        mContext = context;
        mUtils = MyPowerUtils.getInstance(context);
    }

    /**
     * @hide
     */
    public void setPowerCallback(IPowerCallback utils) {
        mUtils = utils;
    }

    /**
     * @hide
     */
    public void setContext(Context context) {
        if (mContext != context) {
            mContext = context;
            ((MyPowerUtils) mUtils).setContext(context);
        }
    }

    /**
     * turnScreenOffIfRotated: Main function to call - starts the rotation algorithm
     *
     * @param options  - bitmask of RotationAlgorithm options that can contain
     *                 {@link RotationAlgorithm#OPTION_START_WITH_BS},
     *                 {@link RotationAlgorithm#OPTION_POWER_ON},
     *                 {@link RotationAlgorithm#OPTION_NO_UNLOCK},
     *                 {@link RotationAlgorithm#OPTION_DONT_MONITOR_BACK_ROTATION},
     *                 {@link RotationAlgorithm#OPTION_EXPECT_FIRST_ROTATION_FOR_60SEC}
     * @param listener - callback when phone rotation happened
     */
    public void turnScreenOffIfRotated(int options, OnPhoneRotatedListener listener) {
        mListener = listener;
        turnScreenOffIfRotated(options);
    }

    /**
     * turnScreenOffIfRotated - Main function to call: starts the rotation algorithm
     *
     * @param options - bitmask of RotationAlgorithm options
     */
    public void turnScreenOffIfRotated(int options) {
        mStartWithFS = !isOptionSet(options, OPTION_START_WITH_BS);
        mPowerOn = isOptionSet(options, OPTION_POWER_ON);
        mNoUnlock = isOptionSet(options, OPTION_NO_UNLOCK);
        mExpectFirstRotationFor60Sec = isOptionSet(options, OPTION_EXPECT_FIRST_ROTATION_FOR_60SEC);

        turnScreenOffIfRotated();
    }

    private boolean isOptionSet(int options, int flag) {
        return (options & flag) != 0;
    }

    /**
     * Main function to call - starts the rotation algorithm
     */
    public void turnScreenOffIfRotated() {
        mRotationHappened = false;
        mFirstStep = true;
        mUserIsLying = false;
        mGyroscopeArray = new LinkedList<SensorAttributes>();
        p2bClickedTime = System.currentTimeMillis();

        FrameworkUtils.isLockScreenDisabled(mContext, new IPlatinumCallback() {

            @Override
            public void onLockScreenDisabled(boolean isLockScreenDisabled) {
                mDeviceLockSettingIsNone = isLockScreenDisabled;
            }
        });

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), TIME_DELAY * 1000);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), TIME_DELAY * 1000);
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
    }

    /**
     * issueStandardToastAndVibration - Use this to issue standard Toast and Vibration that should be used right after user pressed put-to-back button in application. This will prompt the user to rotate the phone.
     */
    public void issueStandardToastAndVibration() {
        Resources res = mContext.getResources();
        //        try {
        //            res = getResourceContext();
        //        } catch (Exception e) {
        //            try {
        //                res = getResourceByPath();
        //            } catch (Exception e1) {
        //            }
        //        }

        try {
            handleToastAndVibro(res.getString(R.string.application_is_updated_on_bs), res.getInteger(R.integer.vibration_time));
        } catch (Exception e) {// null, not found
            String locale = Locale.getDefault().getLanguage();
            String text = RESOURCE.get(locale);
            if (text == null) {
                text = RESOURCE.get("en");
            }
            handleToastAndVibro(text, 18);
        }
    }

    private void handleToastAndVibro(String str, int time) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
        ((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(time);
    }

    //    private Resources getResourceContext() throws NameNotFoundException {
    //        return mContext.getPackageManager().getResourcesForApplication("com.yotadevices.platinum");
    //    }
    //
    //    private Resources getResourceByPath() throws Exception {
    //        final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    //        final Display d = wm.getDefaultDisplay();
    //        Configuration c = new Configuration();
    //        final DisplayMetrics metrics = new DisplayMetrics();
    //        d.getMetrics(metrics);
    //        AssetManager assets = new AssetManager();
    //        mContext.getResources().getAssets().addAssetPath(mContext.getPackageResourcePath());
    //        return new Resources(assets, metrics, c);
    //    }

    private final static Map<String, String> RESOURCE = new HashMap<String, String>();

    {
        RESOURCE.put("en", "Application is updated on Back Screen");
        RESOURCE.put("ar", "تم تحديث التطبيق على الشاشة الخلفية");
        RESOURCE.put("de", "Die Anwendung wird auf dem rückseitigen Bildschirm aktualisiert");
        RESOURCE.put("es", "La aplicación está actualizada en la pantalla trasera");
        RESOURCE.put("fr", "L\'application est mise à jour sur l\'écran arrière");
        RESOURCE.put("it", "Applicazione aggiornata sullo schermo posteriore");
        RESOURCE.put("ru", "Приложение обновлено\nна втором экране");
    }

    @Override
    /**
     * @hide
     */
    public void onSensorChanged(SensorEvent event) {
        SensorAttributes accelerometer = new SensorAttributes();
        SensorAttributes gyroscope = new SensorAttributes();

        setInitialValue(event, accelerometer, gyroscope);
        calculateFlags(accelerometer, getAverageGyroscope(gyroscope));
        handleAction();
    }

    private void setInitialValue(SensorEvent event, SensorAttributes accelerometer, SensorAttributes gyroscope) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometer.x = event.values[0];
            accelerometer.y = event.values[1];
            accelerometer.z = event.values[2];
        } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscope.x = event.values[0];
            gyroscope.y = event.values[1];
            gyroscope.z = event.values[2];
        }
    }

    private AvgGyroscope getAverageGyroscope(SensorAttributes gyroscope) {
        mGyroscopeArray.add(gyroscope);
        if (mGyroscopeArray.size() > MAX_SIZE) {
            mGyroscopeArray.poll();
        }
        AvgGyroscope avgGyroscope = new AvgGyroscope();
        for (SensorAttributes atr : mGyroscopeArray) {
            avgGyroscope.xAvg += atr.x;
            avgGyroscope.yAvg += atr.y;
            avgGyroscope.zAvg += atr.z;
        }

        int size = mGyroscopeArray.size();
        avgGyroscope.xAvg = avgGyroscope.xAvg / size;
        avgGyroscope.yAvg = avgGyroscope.yAvg / size;
        avgGyroscope.zAvg = avgGyroscope.zAvg / size;

        return avgGyroscope;
    }

    private void calculateFlags(SensorAttributes accelerometer, AvgGyroscope avgGyroscope) {
        if (mFirstStep) {
            if (mStartWithFS && accelerometer.z < -3 || !mStartWithFS && accelerometer.z > 3) {
                mUserIsLying = true; //Meaning that user is holding device upside down
            }
            mFirstStep = false;
        }
        if (accelerometer.z < -3 && mStartWithFS && !mUserIsLying
                || accelerometer.z > 3 && !mStartWithFS && !mUserIsLying
                || accelerometer.z > 3 && mStartWithFS && mUserIsLying
                || accelerometer.z < -3 && !mStartWithFS && mUserIsLying
                || Math.abs(avgGyroscope.yAvg) > 2
                || Math.abs(avgGyroscope.xAvg) > 2) {
            mRotationHappened = true;
        }
    }

    private void handleAction() {
        if (System.currentTimeMillis() > (p2bClickedTime + (mExpectFirstRotationFor60Sec ? TIME_60SEC : TIME_6SEC))) {
            mSensorManager.unregisterListener(this);
            if (mListener != null) {
                mListener.onRotataionCancelled();
            }
        }
        if (mRotationHappened) {
            mSensorManager.unregisterListener(this);
            if (mStartWithFS) {
                mUtils.goToSleep();
                if (!mNoUnlock) {
                    mUtils.unlockBackScreen();
                }
                if (mListener != null) {
                    mListener.onPhoneRotatedToBS();
                }
            } else {
                if (mPowerOn || mDeviceLockSettingIsNone) {
                    mUtils.wakeUp();
                }
                mUtils.lockBackScreen();

                new UnlockScreen().execute();

                if (mListener != null) {
                    mListener.onPhoneRotatedToFS();
                }
            }
        }
    }

    @Override
    /**
     * @hide
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // If rotation happens fast then unlock can happen faster than lock is completed.
    // This is why we need to perform another unlock operation after some time.
    private class UnlockScreen extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mUtils.wakeUp();
            mUtils.lockOff();

            int limit = 2000;
            while (!mKeyguardManager.inKeyguardRestrictedInputMode() && mPowerOn) {
                try {
                    mUtils.lockOff();
                    Thread.sleep(50);
                    limit -= 50;
                    if (limit < 0)
                        break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mUtils.lockOff();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

}
