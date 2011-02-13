package org.geometerplus.android.util;

import org.geometerplus.android.util.KeyTracker.OnKeyTracker;
import org.geometerplus.android.util.KeyTracker.Stage;
import org.geometerplus.android.util.KeyTracker.State;

import android.view.KeyEvent;

public abstract class LongPressBackKeyTracker implements OnKeyTracker {
	private Stage lastStage = Stage.SHORT_REPEAT;

	public State onKeyTracker(int keyCode, KeyEvent event, Stage stage, int duration) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (stage == KeyTracker.Stage.LONG_REPEAT) {
				lastStage = stage;
				return KeyTracker.State.KEEP_TRACKING;
			} else if (stage == KeyTracker.Stage.UP) {
				if(lastStage == Stage.LONG_REPEAT) {
					lastStage = Stage.SHORT_REPEAT;
					onLongPressBack(keyCode, event, stage, duration);
					return KeyTracker.State.DONE_TRACKING;
				}
				onShortPressBack(keyCode, event, stage, duration);
				return KeyTracker.State.NOT_TRACKING;
			}
			return KeyTracker.State.KEEP_TRACKING;
		}
		return KeyTracker.State.NOT_TRACKING;
	}

	public abstract void onLongPressBack(int keyCode, KeyEvent event, Stage stage, int duration);

	public abstract void onShortPressBack(int keyCode, KeyEvent event, Stage stage, int duration);
}

