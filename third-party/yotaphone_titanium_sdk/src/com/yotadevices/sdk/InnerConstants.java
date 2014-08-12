package com.yotadevices.sdk;

/**
 * @hide
 */
public final class InnerConstants {

    public static final class RequestFramework {
        public static final int REQUEST_SET_ACTIVE = 0;
        public static final int REQUEST_SET_SYSTEM_UI = 1;
        public static final int REQUEST_SET_FINISH = 2;
        public static final int REQUEST_SET_INTENT = 3;
        public static final int REQUEST_SET_ACTIVITY_RESULT = 4;
    }

    public static final class AnswerFramework {
        /**
         * PM answer that app is active.
         */
        public static final int MESSAGE_ACTIVATED = 0;

        /**
         * PM answer that app is disactive.
         */
        public static final int MESSAGE_DISACTIVATED = 1;

        public static final int MESSAGE_BS_RESUME = 2;
        public static final int MESSAGE_BS_PAUSE = 3;
        public static final int MESSAGE_BS_LOCK = 4;
        public static final int MESSAGE_BS_UNLOCK = 5;
        public static final int MESSAGE_VOLUME_BUTTONS_EVENT = 6;

        public static final int MESSAGE_SYSTEM_UI_CHANGE = 7;
    }

    /**
     * extra service name.
     */
    public static final String EXTRA_SERVICE_NAME = "service_class_name";
    public static final String EXTRA_SYSTEM_BS_UI_FLAG = "service_system_ui_flag";
    public static final String EXTRA_BS_ACTIVITY_INTENT = "bs_activity_intent";
    public static final String EXTRA_REQUEST_CODE = "request_code";
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_DATA = "result_data";

    public static final String META_DATA_BS_ICON = "com.yotadevices.BS_ICON";
    public static final String META_DATA_BS_TITLE = "com.yotadevices.BS_TITLE";

    private InnerConstants() {

    }

}
