package org.omnirom.device;

import android.content.ActivityNotFoundException;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;
import com.android.internal.util.ArrayUtils;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = KeyHandler.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final int GESTURE_REQUEST = 1;
    private static final int GESTURE_WAKELOCK_DURATION = 3000;

    private static final String KEY_GESTURE_HAPTIC_FEEDBACK =
            "touchscreen_gesture_haptic_feedback";

    private static final String KEY_TORCH_LAUNCH_INTENT = "touchscreen_gesture_torch_launch_intent";  
    private static final String KEY_PLAY_PAUSE_LAUNCH_INTENT = 
			"touchscreen_gesture_play_pause_launch_intent";   
    private static final String KEY_PREVIOUS_LAUNCH_INTENT = 
			"touchscreen_gesture_previous_launch_intent"; 
    private static final String KEY_NEXT_LAUNCH_INTENT = "touchscreen_gesture_next_launch_intent";
    
    private static final String KEY_TORCH_FEEDBACK  = "touchscreen_gesture_torch_feedback";    
    private static final String KEY_PLAY_PAUSE_FEEDBACK  = 
			"touchscreen_gesture_play_pause_feedback";  
    private static final String KEY_PREVIOUS_FEEDBACK  = 
			"touchscreen_gesture_previous_feedback";
    private static final String KEY_NEXT_FEEDBACK  = "touchscreen_gesture_next_feedback";

    private static final String ACTION_DISMISS_KEYGUARD =
    "com.android.keyguard.action.DISMISS_KEYGUARD_SECURELY";

    private static final String BUTTON_DISABLE_FILE = "/proc/touchpanel/keypad_enable";

    // Supported scancodes
    private static final int GESTURE_CIRCLE_SCANCODE = 250;
    private static final int GESTURE_SWIPE_DOWN_SCANCODE = 251;
    private static final int GESTURE_V_SCANCODE = 252;
    private static final int GESTURE_LTR_SCANCODE = 253;
    private static final int GESTURE_GTR_SCANCODE = 254;
    private static final int KEY_DOUBLE_TAP = 255;

    private static final int[] sSupportedGestures = new int[]{
        GESTURE_CIRCLE_SCANCODE,
        GESTURE_SWIPE_DOWN_SCANCODE,
        GESTURE_LTR_SCANCODE,
        GESTURE_GTR_SCANCODE,
        GESTURE_V_SCANCODE,
        KEY_DOUBLE_TAP
    };

    private static final int[] sHandledGestures = new int[]{
        GESTURE_SWIPE_DOWN_SCANCODE,
        GESTURE_LTR_SCANCODE,
        GESTURE_GTR_SCANCODE,
        GESTURE_V_SCANCODE,
        KEY_DOUBLE_TAP
    };

    private final Context mContext;
    private final PowerManager mPowerManager;
    private EventHandler mEventHandler;
    private Vibrator mVibrator;
    private boolean mTorchEnabled;
    private CameraManager mCameraManager;
    private String mRearCameraId;

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    WakeLock mProximityWakeLock;
    WakeLock mGestureWakeLock;
    Message msg;
    private int mProximityTimeOut;
    private boolean mProximityWakeSupported;
    private Handler mHandler = new Handler();
    private KeyguardManager mKeyguardManager;
    private SettingsObserver mSettingsObserver;

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(
                    Settings.System.HARDWARE_KEYS_DISABLE),
                    false, this);
            update();
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
        }

        public void update() {
            setButtonDisable(mContext);
        }
    }

    private class MyTorchCallback extends CameraManager.TorchCallback {
        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (!cameraId.equals(mRearCameraId))
                return;
            mTorchEnabled = enabled;
        }

        @Override
        public void onTorchModeUnavailable(String cameraId) {
            if (!cameraId.equals(mRearCameraId))
                return;
            mTorchEnabled = false;
        }
    }

    public KeyHandler(Context context) {
        mContext = context;
        mEventHandler = new EventHandler();
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mGestureWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "GestureWakeLock");
        mSettingsObserver = new SettingsObserver(mHandler);
        mSettingsObserver.observe();

        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mCameraManager.registerTorchCallback(new MyTorchCallback(), mEventHandler);  

        final Resources resources = mContext.getResources();
        //mProximityTimeOut = resources.getInteger(
        //        com.android.internal.R.integer.config_proximityCheckTimeout);
        //mProximityWakeSupported = resources.getBoolean(
        //        com.android.internal.R.bool.config_proximityCheckOnWake);

        //if (mProximityWakeSupported) {
        //    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //    mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //    mProximityWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        //            "ProximityWakeLock");
        //}

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            mVibrator = null;
        }
    }

    private class EventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
            case GESTURE_SWIPE_DOWN_SCANCODE:
		if(!launchIntentFromKey(KEY_PLAY_PAUSE_LAUNCH_INTENT)){
        	    dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
		}
                doHapticFeedback(KEY_PLAY_PAUSE_FEEDBACK);
                break;
            case GESTURE_LTR_SCANCODE:
                if(!launchIntentFromKey(KEY_PREVIOUS_LAUNCH_INTENT)){
                    dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                }
                doHapticFeedback(KEY_PREVIOUS_FEEDBACK);
                break;
            case GESTURE_GTR_SCANCODE:
                if(!launchIntentFromKey(KEY_NEXT_LAUNCH_INTENT)){
                    dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT);
                }
                doHapticFeedback(KEY_NEXT_FEEDBACK); 
                break;
            case GESTURE_V_SCANCODE:
                String rearCameraId = getRearCameraId();
                if (rearCameraId != null) {
                    mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
                    try {
                        mCameraManager.setTorchMode(rearCameraId, !mTorchEnabled);
                        mTorchEnabled = !mTorchEnabled;
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                doHapticFeedback(KEY_TORCH_FEEDBACK);
                break;
            }
        }
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        int scanCode = event.getScanCode();
        boolean isKeySupported = ArrayUtils.contains(sHandledGestures, scanCode);
        if (!isKeySupported) {
            return false;
        }

        if (event.getAction() != KeyEvent.ACTION_UP) {
            return true;
        }

        if (!mEventHandler.hasMessages(GESTURE_REQUEST)) {
            Message msg = getMessageForKeyEvent(scanCode);
            //boolean defaultProximity = mContext.getResources().getBoolean(
            //    com.android.internal.R.bool.config_proximityCheckOnWakeEnabledByDefault);
            //boolean proximityWakeCheckEnabled = Settings.System.getInt(mContext.getContentResolver(),
            //        Settings.System.PROXIMITY_ON_WAKE, defaultProximity ? 1 : 0) == 1;
            //if (mProximityWakeSupported && proximityWakeCheckEnabled && mProximitySensor != null) {
            //    mEventHandler.sendMessageDelayed(msg, mProximityTimeOut);
            //    processEvent(scanCode);
            //} else {
                mEventHandler.sendMessage(msg);
        }
        return true;
    }

    private Message getMessageForKeyEvent(int scancode) {
        Message msg = mEventHandler.obtainMessage(GESTURE_REQUEST);
        msg.arg1 = scancode;
        return msg;
    }

    private void processEvent(final int scancode) {
        //mProximityWakeLock.acquire();
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
         //       mProximityWakeLock.release();
                mSensorManager.unregisterListener(this);
                if (!mEventHandler.hasMessages(GESTURE_REQUEST)) {
                    // The sensor took to long, ignoring.
                    return;
                }
                mEventHandler.removeMessages(GESTURE_REQUEST);
                if (event.values[0] == mProximitySensor.getMaximumRange()) {
                    Message msg = getMessageForKeyEvent(scancode);
                    mEventHandler.sendMessage(msg);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        }, mProximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void dispatchMediaKeyWithWakeLockToMediaSession(int keycode) {
        MediaSessionLegacyHelper helper = MediaSessionLegacyHelper.getHelper(mContext);
        if (helper != null) {
            KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keycode, 0);
            helper.sendMediaButtonEvent(event, true);
            event = KeyEvent.changeAction(event, KeyEvent.ACTION_UP);
            helper.sendMediaButtonEvent(event, true);
        } else {
            Log.w(TAG, "Unable to send media key event");
        }
    }

    @Override
    public boolean isCameraLaunchEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        return event.getScanCode() == GESTURE_CIRCLE_SCANCODE;
    }

        private void doHapticFeedback(String key) {
            boolean enabled = Settings.System.getInt(mContext.getContentResolver(), key, 0) != 0;
            if(enabled){
                doHapticFeedback();
            }
        }

    private void doHapticFeedback() {
        if (mVibrator == null) {
            return;
        }
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                KEY_GESTURE_HAPTIC_FEEDBACK, 1) != 0;
        if (enabled) {
            mVibrator.vibrate(50);
        }
    }

    @Override
    public boolean canHandleKeyEvent(KeyEvent event) {
        return ArrayUtils.contains(sSupportedGestures, event.getScanCode());
    }

    @Override
    public boolean isDisabledKeyEvent(KeyEvent event) {
        return false;
    }

    public static void setButtonDisable(Context context) {
        final boolean disableButtons = Settings.System.getInt(
                context.getContentResolver(), Settings.System.HARDWARE_KEYS_DISABLE, 0) == 1;
        if (DEBUG) Log.i(TAG, "setButtonDisable=" + disableButtons);
        Utils.writeValue(BUTTON_DISABLE_FILE, disableButtons ? "0" : "1");
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            UserHandle user = new UserHandle(UserHandle.USER_CURRENT);
            mContext.startActivityAsUser(intent, null, user);
        } catch (ActivityNotFoundException e) {
            // Ignore
        }
    }

    @Override
    public boolean isWakeEvent(KeyEvent event){
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        return event.getScanCode() == KEY_DOUBLE_TAP;
    }

    private boolean launchIntentFromKey(String key){
        String packageName = Settings.System.getString(mContext.getContentResolver(), key);
        Intent intent = null;
        if(packageName != null && !packageName.equals("")){
            intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        }
        if(intent != null){
            mGestureWakeLock.acquire(GESTURE_WAKELOCK_DURATION);
            mPowerManager.wakeUp(SystemClock.uptimeMillis());
            mContext.sendBroadcastAsUser(new Intent(ACTION_DISMISS_KEYGUARD), UserHandle.CURRENT);
            startActivitySafely(intent);
            return true;
        }
        return false;
    }

    private String getRearCameraId() {
        if (mRearCameraId == null) {
            try {
                for (final String cameraId : mCameraManager.getCameraIdList()) {
	            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(cameraId);
	            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
	            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
	            if (flashAvailable != null && flashAvailable
	                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        mRearCameraId = cameraId;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                // Ignore
            }
        }
        return mRearCameraId;
    }
}
