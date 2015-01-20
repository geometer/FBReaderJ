package com.yotadevices.sdk.exception;

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

import android.util.AndroidRuntimeException;

/**
 * This exception will be thrown if super class of BSActiviy's functions is not called
 */
public final class SuperNotCalledException extends AndroidRuntimeException {
	public SuperNotCalledException(String msg) {
		super(msg);
	}
}
