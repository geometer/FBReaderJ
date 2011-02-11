/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geometerplus.android.util;

import android.view.KeyEvent;
import android.view.ViewConfiguration;

public class KeyTracker {

	public enum Stage {
		DOWN,			//!< the key has just been pressed
		SHORT_REPEAT,	//!< repeated key, but duration is under the long-press threshold
		LONG_REPEAT,	//!< repeated key, but duration is over the long-press threshold
		UP				//!< the key is being released
	}

	public enum State {
		KEEP_TRACKING,	//!< return this to continue to track the key
		DONE_TRACKING,	//!< return this if you handled the key, but need not track it anymore
		NOT_TRACKING	//!< return this if you will not handle this key
	}

	public interface OnKeyTracker {

		/** Called whenever there is a key event [down, short/long repeat, up]
			@param keyCode	The current keyCode (see KeyEvent class)
			@param msg		The message associated with the keyCode
			@maram stage	The state the key press is in [down, short/long repeat, up]
			@param duration	The number of milliseconds since this key was initially pressed
			@return your state after seeing the key. If you return DONE_TRACKING or NOT_TRACKING,
					you will not be called again for the lifetime of this key event.
		 */
		public State onKeyTracker(int keyCode, KeyEvent event, Stage stage, int duration);
	}

	public KeyTracker(OnKeyTracker tracker) {
		mTracker = tracker;
	}

	public boolean doKeyDown(int keyCode, KeyEvent event) {
		long now = System.currentTimeMillis();
		Stage stage = null;

		// check if its a new/different key
		if (mKeyCode != keyCode || event.getRepeatCount() == 0) {
			mKeyCode = keyCode;
			mStartMS = now;
			stage = Stage.DOWN;
		}
		else if (mState == State.KEEP_TRACKING) {
			stage = (now - mStartMS) >= LONG_PRESS_DURATION_MS ? Stage.LONG_REPEAT : Stage.SHORT_REPEAT;
		}

		if (stage != null) {
			mEvent = event;        
			callTracker(stage, now);
		}

		return mState != State.NOT_TRACKING;
	}

	public boolean doKeyUp(int keyCode, KeyEvent event) {
		boolean handled = false;

		if (mState == State.KEEP_TRACKING && mKeyCode == keyCode) {
			mEvent = event;
			callTracker(Stage.UP, System.currentTimeMillis());
			handled = mState != State.NOT_TRACKING;
		}
		mKeyCode = NOT_A_KEYCODE;
		return handled;
	}

	private void callTracker(Stage stage, long now) {
		mState = mTracker.onKeyTracker(mKeyCode, mEvent, stage, (int)(now - mStartMS));
	}

	private int				mKeyCode = NOT_A_KEYCODE;
	private KeyEvent		mEvent;
	private long			mStartMS;
	private State			mState;
	private OnKeyTracker	mTracker;

	private static final int LONG_PRESS_DURATION_MS = 
		ViewConfiguration.getLongPressTimeout();
	private static final int NOT_A_KEYCODE = -123456;
}
