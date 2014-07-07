package com.yotadevices.sdk;

import android.view.MotionEvent;

public final class Constants {

    /**
     * @hide
     */
    public enum CameraEvent {
        CAMERA_PREVIEW_START, CAMERA_PREVIEW_STOP, CAMERA_PHOTOSHUTTER, CAMERA_VIDEORECORDING_START, CAMERA_VIDEORECORDING_STOP, CAMERA_ERROR, CAMERA_CLOSED, UNKNOW;

        public static CameraEvent valueOf(int event) {
            switch (event) {
            case 0:
                return CAMERA_PREVIEW_START;
            case 1:
                return CAMERA_PREVIEW_STOP;
            case 2:
                return CAMERA_PHOTOSHUTTER;
            case 3:
                return CAMERA_VIDEORECORDING_START;
            case 4:
                return CAMERA_VIDEORECORDING_STOP;
            case 5:
                return CAMERA_ERROR;
            case 6:
                return CAMERA_CLOSED;
            default:
                return UNKNOW;
            }
        }
    }

    /**
     * List of volume buttons actions that can be used in
     * {@link BSActivity#onVolumeButtonsEvent(VolumeButtonsEvent)}
     */
    public enum VolumeButtonsEvent {
        /**
         * On "Volume Plus press" event
         */
        VOLUME_PLUS_DOWN,
        /**
         * On "Volume Minus press" event
         */
        VOLUME_MINUS_DOWN,
        /**
         * On "Volume Plus release” event
         */
        VOLUME_PLUS_UP,
        /**
         * On "Volume Minus release" event
         */
        VOLUME_MINUS_UP,
        /**
         * @hide
         */
        VOLUME_PLUS_LONG_PRESS,
        /**
         * @hide
         */
        VOLUME_MINUS_LONG_PRESS,

        /**
         * Unknown volume button action
         */
        UNKNOW;

        /**
         * @hide
         */
        public static VolumeButtonsEvent valueOf(int event) {
            switch (event) {
            case 0:
                return VOLUME_PLUS_DOWN;
            case 1:
                return VOLUME_MINUS_DOWN;
            case 2:
                return VOLUME_PLUS_UP;
            case 3:
                return VOLUME_MINUS_UP;
            case 4:
                return VOLUME_PLUS_LONG_PRESS;
            case 5:
                return VOLUME_MINUS_LONG_PRESS;
            default:
                return UNKNOW;
            }
        }
    }

    public enum Gestures {
        GESTURES_P2B, GESTURES_FS_LOCK, GESTURES_BS_LOCK, GESTURES_BS_UNLOCK, GESTURES_UNKNOW;

        private MotionEvent motionEvent;

        public void setMotionEvent(MotionEvent e) {
            motionEvent = e;
        }

        public MotionEvent getMotionEvent() {
            return motionEvent;
        }

        // not use ordinal();
        public static Gestures valueOf(int gesture) {
            switch (gesture) {
            case 0:
                return GESTURES_P2B;
            case 1:
                return GESTURES_FS_LOCK;
            case 2:
                return GESTURES_BS_LOCK;
            case 3:
                return GESTURES_BS_UNLOCK;
            default:
                return GESTURES_UNKNOW;
            }
        }

    }

    public static final class Notifications {
        private static final int FULL_SCREEN_NOTIFICATION = 65536;
        // Half screen notification
        private static final int HALF_SCREEN_NOTIFICATION = 131072;
        // Bar notification
        private static final int BAR_NOTIFICATION = 262144;
        // Counter notification
        private static final int COUNTER_NOTIFICATION = 524288;

        private static final int UNKNOW = -1;

        public static int valueOf(int event) {
            switch (event) {
            case 65536:
                return FULL_SCREEN_NOTIFICATION;
            case 131072:
                return HALF_SCREEN_NOTIFICATION;
            case 262144:
                return BAR_NOTIFICATION;
            case 524288:
                return COUNTER_NOTIFICATION;
            default:
                return UNKNOW;
            }
        }
    }

    public static final class Settings {

        /**
         * Whether Privacy Mode is on.
         */
        public static final String PRIVACY_MODE = "yotadevices_privacy_mode";

        /**
         * Whether Favorites Mode is on.
         */
        public static final String FAVORITES_MODE = "yotadevices_favorites_mode";

        /**
         * Whether SMSFUN Mode is on.
         */
        public static final String SMS_EMOTIONAL_MODE = "yotadevices_sms_emotional_mode";

        /**
         * Whether notification can show on BS.
         */
        public static final String BS_NOTIFICATION_ON = "yotadevices_bs_notification_on";

        /**
         * Whether Smile for the Camera/Video is on.
         */
        public static final String SMILE_FOR_CAMERA = "yotadevices_smile_camera_on";

        /**
         * Whether preview photo after shutting is on.
         */
        public static final String PHOTO_PREVIEW = "yotadevices_photo_preview_on";

        /**
         * Whether discharged state on BS is on.
         */
        public static final String DISCHARGED_STATE = "yotadevices_discharged_state_on";

        /**
         * Whether discharged state on BS is on.
         */
        public static final String DISMISS_FS_NOTIFICATION = "yotadevices_dismiss_fs_notification";

        /**
         * Whether Task Manager on BS is on.
         */
        public static final String TASK_MANAGER_MODE = "yotadevices_task_manager_mode";

        /**
         * Whether Music Player on BS is on.
         */
        public static final String MUSIC_PLAYER_MODE = "yotadevices_music_player_mode";

    }

    public static class SystemBSFlags {
        public final static int SYSTEM_BS_UI_FLAG_VISIBLE = 0;
        public final static int SYSTEM_BS_UI_FLAG_HIDE_STATUS_BAR = 0x00000001;
        public final static int SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION = 0x00000002;
    }

    private Constants() {

    }
}
