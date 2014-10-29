package com.yotadevices.sdk.exception;

import android.util.AndroidRuntimeException;

/**
 * This exception will be thrown if super class of BSActiviy's functions is not called
 */
public final class SuperNotCalledException extends AndroidRuntimeException {
	public SuperNotCalledException(String msg) {
		super(msg);
	}
}
