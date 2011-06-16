package org.geometerplus.android.fbreader;

import java.util.HashMap;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class SpeakActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	private static final int CHECK_TTS_INSTALLED = 0;
	private static final String PARAGRAPHUTTERANCE = "PARAGRAPHUTTERANCE";

	static final int CURRENTORFORWARD = 0;
	static final int SEARCHFORWARD = 1;
	static final int SEARCHBACKWARD = 2;

	private TextToSpeech myTTS;
	private FBReaderApp myReader;
	private FBView myView;

	private ZLTextParagraphCursor myParagraphCursor;
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
			nextParagraphString(true, false, SEARCHFORWARD);
		}
	};

	private View.OnClickListener backListener = new View.OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			nextParagraphString(true, false, SEARCHBACKWARD);
		}
	};

	private View.OnClickListener pauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (myIsActive) {
				stopTalking();
				myIsActive = false;
			} else {
				myIsActive = true;
				nextParagraphString(true, true, CURRENTORFORWARD);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		myReader = (FBReaderApp)FBReaderApp.Instance();
		myView = myReader.getTextView();

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

		ZLTextWordCursor cursor = myView.getStartCursor();
		myParagraphCursor = cursor.getParagraphCursor();

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, CHECK_TTS_INSTALLED);
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

	private String getParagraphText(ZLTextParagraphCursor paraCursor) {
		StringBuffer sb = new StringBuffer();
		ZLTextWordCursor cursor = new ZLTextWordCursor(paraCursor);
		while (!cursor.isEndOfParagraph()) {
			ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				sb.append(element.toString() + " ");
			}
			cursor.nextWord();
		}
		return(sb.toString());
	}

	private void setActive(boolean active) {
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
	}

	private void showString(String s) {
		myView.gotoPosition(myParagraphCursor.Index, 0, 0);

		myReader.getViewWidget().repaint();
		myReader.showBookTextView();
		//myView.getModel().Book.storePosition(BookTextView.getStartCursor());
	}

	private String lookforValidParagraphString(int direction) {
		String s = "";
		while (s.equals("") && myParagraphCursor != null) {
			switch (direction) {
				case SEARCHFORWARD:
					myParagraphCursor = myParagraphCursor.next();
					break;
				case SEARCHBACKWARD:
					myParagraphCursor = myParagraphCursor.previous();
					break;
				case CURRENTORFORWARD:
					direction = SEARCHFORWARD;
					break;
			}
			s = getParagraphText(myParagraphCursor);
		}
		return s;
	}

	private String nextParagraphString(boolean show, boolean speak, int direction) {
		String s = lookforValidParagraphString(direction);

		if (show) {
			showString(s);
		}
		if (speak) {
			speakString(s);
		}

		return s;
	}

	@Override
	protected void onDestroy() {
		myReader.onWindowClosing(); // save the position
		setActive(false);
		myTTS.shutdown();
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
		myReader.onWindowClosing(); // save the position
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
		nextParagraphString(true, true, CURRENTORFORWARD);
	}

	@Override
	public void onUtteranceCompleted(String uttId) {
		if (myIsActive && uttId.equals(PARAGRAPHUTTERANCE)) {
			nextParagraphString(true, true, SEARCHFORWARD);
		} else {
			setActive(false);
		}
	}
}
