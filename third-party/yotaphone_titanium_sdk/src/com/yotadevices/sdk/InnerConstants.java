package com.yotadevices.sdk;

/**
 * Copyright 2012 Yota Devices LLC, Russia
 * 
 * This source code is Yota Devices Confidential Proprietary
 * This software is protected by copyright.  All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or
 * reverse engineer the software. Otherwise this violation would be treated by 
 * law and would be subject to legal prosecution.  Legal use of the software 
 * provides receipt of a license from the right holder only.
 * 
 * */

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
        public static final int REQUEST_CAN_START = 5;
        public static final int HANDLE_ON_KEY_PRESS = 6;
        public static final int UNFREEZE_EPD = 7;
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
        public static final int MESSAGE_ON_KEY_PRESS = 8;
        public static final int MESSAGE_ACTIVITY_START = 9;
    }

    /**
     * extra service name.
     */
    public static final String EXTRA_SERVICE_NAME = "service_class_name";
    public static final String EXTRA_PACKAGE_NAME = "service_package_name";
    public static final String EXTRA_SYSTEM_BS_UI_FLAG = "service_system_ui_flag";
    public static final String EXTRA_SYSTEM_FEATURE = "service_system_feature_flag";
    public static final String EXTRA_BS_ACTIVITY_INTENT = "bs_activity_intent";
    public static final String EXTRA_REQUEST_CODE = "request_code";
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_DATA = "result_data";
    public static final String EXTRA_OVERRIDE_KEY_BACK = "key_back";
    public static final String EXTRA_OVERRIDE_KEY_HOME = "key_home";

    public static final String META_DATA_BS_ICON = "com.yotadevices.BS_ICON";
    public static final String META_DATA_BS_TITLE = "com.yotadevices.BS_TITLE";

    private InnerConstants() {

    }

}
