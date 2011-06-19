/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

public class ApiException extends Exception {
	ApiException(String message) {
		super(message);
	}

	ApiException(Exception parent) {
		super(parent);
	}
}
