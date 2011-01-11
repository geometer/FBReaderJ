package org.geometerplus.android.fbreader;

import java.util.HashMap;
import java.util.Locale;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;



public class SpeakActivity extends Activity implements OnInitListener, OnUtteranceCompletedListener {
	static final int ACTIVE = 1;
	static final int INACTIVE = 0;

	private static final int CHECK_TTS_INSTALLED = 0;
	private static final String PARAGRAPHUTTERANCE="PARAGRAPHUTTERANCE";

	static final int CURRENTORFORWARD = 0;
	static final int SEARCHFORWARD = 1;
	static final int SEARCHBACKWARD = 2;

	private TextToSpeech mTts=null;
	private FBView theView;
	private FBReaderApp Reader;
	private ZLTextParagraphCursor myParaCursor;
	private ImageButton pausebutton;
	private ImageButton forwardbutton;
	private ImageButton backbutton;
	private ImageButton stopbutton;

	private int state = INACTIVE;

	class UpdateControls implements Runnable {
		private int state;
		static final int PAUSE = 0;
		static final int PLAY = 1;

		public UpdateControls(int value) {
			this.state = value;
		}

		public void run() {
			if (state == PLAY) {
				pausebutton.setImageResource(R.drawable.speak_play);
			} else if (state == PAUSE) {
				pausebutton.setImageResource(R.drawable.speak_pause);
			}
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

	private OnClickListener forwardListener = new OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			nextParagraphString(true, false, SEARCHFORWARD);
		}
	};

	private OnClickListener backListener = new OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			nextParagraphString(true, false, SEARCHBACKWARD);
		}
	};

	private OnClickListener pauseListener = new OnClickListener() {
		public void onClick(View v) {
			if (state == ACTIVE) {
				stopTalking();
				setState(INACTIVE);
			} else {
				setState(ACTIVE);
				nextParagraphString(true, true, CURRENTORFORWARD);
			}
		}
	};

	private OnClickListener stopListener = new OnClickListener() {
		public void onClick(View v) {
			stopTalking();
			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		Reader = (FBReaderApp)ZLApplication.Instance();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_spokentext);

		backbutton = (ImageButton)findViewById(R.id.spokentextback);
		backbutton.setOnClickListener(backListener);

		forwardbutton = (ImageButton)findViewById(R.id.spokentextforward);
		forwardbutton.setOnClickListener(forwardListener);

		pausebutton = (ImageButton)findViewById(R.id.spokentextpause);
		pausebutton.setOnClickListener(pauseListener);

		stopbutton = (ImageButton)findViewById(R.id.spokentextstop);
		stopbutton.setOnClickListener(stopListener);

		setState(INACTIVE);

		TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		theView = ((FBReaderApp)FBReaderApp.Instance()).getTextView();

		ZLTextWordCursor cursor = theView.getStartCursor();
		myParaCursor = cursor.getParagraphCursor();

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, CHECK_TTS_INSTALLED);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHECK_TTS_INSTALLED) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, this);
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

	private void setState(int value) {
		state = value;

		if (state == ACTIVE) {
			pausebutton.post(new UpdateControls(UpdateControls.PAUSE));
		} else if (state == INACTIVE) {
			pausebutton.post(new UpdateControls(UpdateControls.PLAY));
		}
	}

	private void speakString(String s) {
		setState(ACTIVE);

		HashMap<String, String> callbackMap = new HashMap<String, String>();
		callbackMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, PARAGRAPHUTTERANCE);

		mTts.speak(s, TextToSpeech.QUEUE_FLUSH, callbackMap);
	}

	private void showString(String s) {
		theView.gotoPosition(myParaCursor.Index, 0, 0);

		Reader.repaintView();
		Reader.showBookTextView();
		//theView.getModel().Book.storePosition(BookTextView.getStartCursor());
	}

	private String lookforValidParagraphString(int direction) {
		String s = "";
		while (s.equals("") && myParaCursor != null) {
			switch (direction) {
				case SEARCHFORWARD:
					myParaCursor = myParaCursor.next();
					break;
				case SEARCHBACKWARD:
					myParaCursor = myParaCursor.previous();
					break;
				case CURRENTORFORWARD:
					direction = SEARCHFORWARD;
					break;
			}
			s = getParagraphText(myParaCursor);
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
		Reader.onWindowClosing(); // save the position
		setState(INACTIVE);
		mTts.shutdown();
		super.onDestroy();
	}

	private void stopTalking() {
		setState(INACTIVE);
		if (mTts != null) {
			if (mTts.isSpeaking()) {
				mTts.stop();
			}
		}
	}

	@Override
	protected void onPause() {
		Reader.onWindowClosing(); // save the position
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		stopTalking();
		super.onBackPressed();
	}

	@Override
	public void onInit(int status) {
		mTts.setOnUtteranceCompletedListener(this);
		setState(ACTIVE);
		nextParagraphString(true, true, CURRENTORFORWARD);
	}

	public void onUtteranceCompleted(String uttId) {
		if (state == ACTIVE && uttId.equals(this.PARAGRAPHUTTERANCE)) {
			nextParagraphString(true, true, SEARCHFORWARD);
		} else {
			setState(INACTIVE);
		}
	}
}
