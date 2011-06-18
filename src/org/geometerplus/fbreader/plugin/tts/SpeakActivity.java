package org.geometerplus.fbreader.plugin.tts;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import org.geometerplus.android.fbreader.api.*;

import org.geometerplus.zlibrary.ui.android.R;

public class SpeakActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	private ApiServiceConnection myConnection;

	private static final int CHECK_TTS_INSTALLED = 0;
	private static final String PARAGRAPHUTTERANCE = "PARAGRAPHUTTERANCE";

	static final int CURRENTORFORWARD = 0;
	static final int SEARCHFORWARD = 1;
	static final int SEARCHBACKWARD = 2;

	private TextToSpeech myTTS;

	private int myParagraphIndex = -1;
	private int myParagraphsNumber;
	private ImageButton myPauseButton;

	private boolean myIsActive = false;

	private class UpdateControls implements Runnable {
		private final int myImageResourceId;

		public UpdateControls(int resourceId) {
			myImageResourceId = resourceId;
		}

		public void run() {
			myPauseButton.setImageResource(myImageResourceId);
		}
	}

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state == TelephonyManager.CALL_STATE_RINGING) {
				stopTalking();
				finish();
			}
		}
	};

	private View.OnClickListener forwardListener = new View.OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			nextParagraphString(false, SEARCHFORWARD);
		}
	};

	private View.OnClickListener backListener = new View.OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			nextParagraphString(false, SEARCHBACKWARD);
		}
	};

	private View.OnClickListener pauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (myIsActive) {
				stopTalking();
				myIsActive = false;
			} else {
				setActive(true);
				if (myIsActive) {
					nextParagraphString(true, CURRENTORFORWARD);
				}
			}
		}
	};

	private View.OnClickListener stopListener = new View.OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		myConnection = new ApiServiceConnection(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_spokentext);

		((ImageButton)findViewById(R.id.spokentextback)).setOnClickListener(backListener);
		((ImageButton)findViewById(R.id.spokentextforward)).setOnClickListener(forwardListener);
		((ImageButton)findViewById(R.id.spokentextstop)).setOnClickListener(stopListener);

		myPauseButton = (ImageButton)findViewById(R.id.spokentextpause);
		myPauseButton.setOnClickListener(pauseListener);

		setActive(false);

		TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, CHECK_TTS_INSTALLED);
	}

	@Override
	protected void onResume() {
		myConnection.connect();
		super.onResume();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHECK_TTS_INSTALLED) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				myTTS = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(
					TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	private String getParagraphText(int paragraphIndex) {
		try {
			return myConnection.getParagraphText(paragraphIndex);
		} catch (ApiException e) {
			return "";
		}
	}

	private void setActive(boolean active) {
		if (active && myParagraphIndex == -1) {
			try {
				myParagraphIndex = myConnection.getPageStart().ParagraphIndex;
				myParagraphsNumber = myConnection.getParagraphsNumber();
			} catch (ApiException e) {
			}
			active = myParagraphIndex != -1;
		}

		myIsActive = active;

		if (myIsActive) {
			myPauseButton.post(new UpdateControls(R.drawable.speak_pause));
		} else {
			myPauseButton.post(new UpdateControls(R.drawable.speak_play));
		}
	}

	private void speakString(String s) {
		setActive(true);

		HashMap<String, String> callbackMap = new HashMap<String, String>();
		callbackMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, PARAGRAPHUTTERANCE);

		myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, callbackMap);
		System.err.println(myParagraphIndex);
		System.err.println(s);
	}

	private String lookForValidParagraphString(int direction) {
		String s = "";
		while (s.equals("") && myParagraphIndex != -1) {
			switch (direction) {
				case SEARCHFORWARD:
					++myParagraphIndex;
					break;
				case SEARCHBACKWARD:
					--myParagraphIndex;
					break;
				case CURRENTORFORWARD:
					direction = SEARCHFORWARD;
					break;
			}
			s = getParagraphText(myParagraphIndex);
		}
		return s;
	}

	private void nextParagraphString(boolean speak, int direction) {
		if (myParagraphIndex >= myParagraphsNumber - 1) {
			stopTalking();
			return;
		}

		String s = lookForValidParagraphString(direction);

		try {
			myConnection.setPageStart(new TextPosition(myParagraphIndex, 0, 0));
		} catch (ApiException e) {
		}
		if (speak) {
			speakString(s);
		}
	}

	@Override
	protected void onDestroy() {
		//myReader.onWindowClosing(); // save the position
		setActive(false);
		if (myTTS != null) {
			myTTS.shutdown();
		}
		super.onDestroy();
	}

	private void stopTalking() {
		setActive(false);
		if (myTTS != null) {
			if (myTTS.isSpeaking()) {
				myTTS.stop();
			}
		}
	}

	@Override
	protected void onPause() {
		//myReader.onWindowClosing(); // save the position
		stopTalking();
		myConnection.disconnect();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		stopTalking();
		super.onBackPressed();
	}

	@Override
	public void onInit(int status) {
		myTTS.setOnUtteranceCompletedListener(this);
		setActive(true);
		nextParagraphString(true, CURRENTORFORWARD);
	}

	@Override
	public void onUtteranceCompleted(String uttId) {
		if (myIsActive && uttId.equals(PARAGRAPHUTTERANCE)) {
			nextParagraphString(true, SEARCHFORWARD);
		} else {
			setActive(false);
		}
	}
}
